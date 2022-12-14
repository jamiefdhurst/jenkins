/* groovylint-disable LineLength */
import groovy.json.JsonSlurperClassic

@NonCPS
def readJson(jsonText) {
    return new JsonSlurperClassic().parseText(jsonText)
}

def getEnvironment() {
    // Get base security group
    env.baseSG = sh(
        script: '''
            aws ec2 describe-security-groups \
                --region eu-west-1 \
                --group-names base \
                --output text \
                --query "SecurityGroups[*].GroupId"
        ''',
        returnStdout: true
    ).trim()
    env.datetime = sh(script: "date '+%Y%m%d%H%M%S'", returnStdout: true).trim()
}

def buildAmi() {
    // Check if needed
    def String existingAmi = sh(
        script: '''
            aws ec2 describe-images \
                --region eu-west-1 \
                --filters "Name=tag:Purpose,Values=infra-update" \
                --owners self \
                --output text \
                --query "Images[0].ImageId"
        ''',
        returnStdout: true
    ).trim()
    if (existingAmi != null && existingAmi != '' && existingAmi != 'None') {
        print "Found existing AMI '${existingAmi}', skipping build..."
        env.amiId = existingAmi
        return
    }

    // Get latest Ubuntu 18.04 image
    def String ubuntuAmi = sh(
        script: '''
            aws ec2 describe-images \
                --region eu-west-1 \
                --filters "Name=name,Values=ubuntu/images/hvm-ssd/ubuntu-bionic-18.04-amd64-server-*" \
                --owners 099720109477 \
                --output text \
                --query "Images[*].[ImageId,CreationDate,Name] | sort_by(@, &[1]) | reverse(@) [0][0]"
        ''',
        returnStdout: true
    ).trim()
    print "Found Ubuntu 18.04 AMI: ${ubuntuAmi}"

    // Launch instance with user data to install information
    def String userData = '''#!/bin/bash
apt-get update
apt-get -yq upgrade
apt-get -yq install ca-certificates curl gnupg lsb-release git awscli make
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
apt-get update
apt-get -yq install docker-ce docker-ce-cli containerd.io
cat << EOF > /root/run.sh
#!/bin/bash
cd /root
export HOME=/root
cp /home/ubuntu/.ssh/authorized_keys /root/.ssh/hd-root.pub
aws ssm --region eu-west-1 get-parameter --name root_private_key --with-decryption --query "Parameter.Value" --output text > /root/.ssh/hd-root
export TEMP_AWS_ACCESS_KEY_ID=`aws ssm --region eu-west-1 get-parameter --name access_key_id --with-decryption --query "Parameter.Value" --output text`
export TEMP_AWS_SECRET_ACCESS_KEY=`aws ssm --region eu-west-1 get-parameter --name secret_access_key --with-decryption --query "Parameter.Value" --output text`
export GITHUB_TOKEN=`aws ssm --region eu-west-1 get-parameter --name github_token --with-decryption --query "Parameter.Value" --output text`
export AWS_ACCESS_KEY_ID=\\$TEMP_AWS_ACCESS_KEY_ID
export AWS_SECRET_ACCESS_KEY=\\$TEMP_AWS_SECRET_ACCESS_KEY
git clone https://jamiefdhurst:\\$GITHUB_TOKEN@github.com/jamiefdhurst/infrastructure.git
cd infrastructure
make build
make decrypt
make init
make apply-force
shutdown -h now
EOF
chmod +x /root/run.sh
echo 'Installed' > /root/.installed
echo 'Installation complete'
    '''
    writeFile file: 'userdata.sh', text: userData
    def instanceDetails = readJson(sh(
        script: """
            aws ec2 run-instances \
                --region eu-west-1 \
                --image-id ${ubuntuAmi} \
                --count 1 \
                --instance-type t3a.micro \
                --key-name root \
                --security-group-ids ${env.baseSG} \
                --tag-specification 'ResourceType=instance,Tags=[{Key=Name,Value=infra-update-image-${env.datetime}}]' \
                --iam-instance-profile 'Name=iam-instance-profile-ec2-backup' \
                --user-data file://userdata.sh
        """,
        returnStdout: true
    ).trim())

    print "Waiting for instance '${instanceDetails.Instances[0].InstanceId}' to become available..."
    sh("""
        aws ec2 wait instance-status-ok \
            --region eu-west-1 \
            --instance-ids ${instanceDetails.Instances[0].InstanceId}
    """)

    // Connect and check /root/.installed
    def maxTries = 30
    def tries = 0
    def found = false
    while (!found && tries < maxTries) {
        withCredentials([sshUserPrivateKey(credentialsId: 'hiawatha-ssh-key', usernameVariable: 'SSH_USER', keyFileVariable: 'SSH_PRIV_KEY')]) {
            try {
                def installed = sh(
                    script: """
                        ssh -i $SSH_PRIV_KEY \
                            -o StrictHostKeyChecking=no \
                            ubuntu@${instanceDetails.Instances[0].PrivateIpAddress} \
                            'sudo cat /root/.installed'
                    """,
                    returnStdout: true
                ).trim()
                if (installed == 'Installed') {
                    found = true
                }
            } catch (err) {}
        }
        tries++
        if (!found) {
            echo "Failed to verify instance - try #${tries}/${maxTries}..."
            sleep 5
        } else {
            echo 'Verified instance has completed installation'
        }
    }

    if (!found) {
        withCredentials([sshUserPrivateKey(credentialsId: 'hiawatha-ssh-key', usernameVariable: 'SSH_USER', keyFileVariable: 'SSH_PRIV_KEY')]) {
            try {
                def logs = sh(
                    script: """
                        ssh -i $SSH_PRIV_KEY \
                            -o StrictHostKeyChecking=no \
                            ubuntu@${instanceDetails.Instances[0].PrivateIpAddress} \
                            'sudo cat /var/log/cloud-init.log && echo "======" && sudo cat /var/log/cloud-init-output.log'
                    """,
                    returnStdout: true
                ).trim()
                print logs
            } catch (err) {}
        }
    }

    // Stop instance
    sh("""
        aws ec2 stop-instances \
            --region eu-west-1 \
            --instance-ids ${instanceDetails.Instances[0].InstanceId}
    """)
    print "Waiting for instance '${instanceDetails.Instances[0].InstanceId}' to shutdown..."
    sh("""
        aws ec2 wait instance-stopped \
            --region eu-west-1 \
            --instance-ids ${instanceDetails.Instances[0].InstanceId}
    """)

    // Create AMI and tag it
    if (found) {
        def amiId = readJson(sh(
            script: """
                aws ec2 create-image \
                    --region eu-west-1 \
                    --instance-id ${instanceDetails.Instances[0].InstanceId} \
                    --name infra-update-${datetime}
            """,
            returnStdout: true
        ).trim())
        sh("""
            aws ec2 create-tags \
                --region eu-west-1 \
                --resources ${amiId.ImageId} \
                --tags 'Key=Name,Value=infra-update-${datetime}' 'Key=Purpose,Value=infra-update'
        """)
        env.amiId = amiId.ImageId
    }

    // Terminate temporary instance
    sh("""
        aws ec2 terminate-instances \
            --region eu-west-1 \
            --instance-ids ${instanceDetails.Instances[0].InstanceId}
    """)

    if (!found) {
        error("Could not verify instance ${instanceDetails.Instances[0].InstanceId} so AMI can't be created...")
    }

    // Wait for AMI to become ready (not pending)
    print "Waiting for image '${amiId}' to become avaiilable..."
    sh("""
        aws ec2 wait image-available \
            --region eu-west-1 \
            --image-ids ${amiId}
    """)
}

def requestSpotInstance() {
    print "Requesting spot instance..."
    def userData = sh(
        script: '''
        cat <<EOF | base64 -w 0
#!/bin/bash
/root/run.sh
EOF''',
        returnStdout: true
    ).trim()
    env.spotRequest = readJson(sh(
        script: """
            aws ec2 request-spot-instances \
                --region eu-west-1 \
                --launch-specification '{"ImageId":"${env.amiId}","KeyName":"root","InstanceType":"t3a.micro","SecurityGroupIds":["${env.baseSG}"],"IamInstanceProfile":{"Name":"iam-instance-profile-ec2-backup"},"UserData":"${userData}"}' \
                --type 'one-time'
        """,
        returnStdout: true
    ).trim())
    print env.spotRequest
    print 'Instance scheduled'
}

pipeline {
    agent any

    environment {
        AWS                   = credentials('aws-credentials')
        AWS_ACCESS_KEY_ID     = "${AWS_USR}"
        AWS_SECRET_ACCESS_KEY = "${AWS_PSW}"
    }

    stages {
        stage('Get Environment')        { steps { getEnvironment() } }
        stage('Build AMI')              { steps { buildAmi() } }
        stage('Request Spot Instance')  { steps { requestSpotInstance() } }
    }

    post {
        always {
            cleanWs()
        }
    }
}
