// groovylint-disable LineLength
// groovylint-disable NestedBlockDepth
// groovylint-disable CompileStatic

node {
    /* Presumes the following variables are set:
    env.repository
    env.target
    env.targetImage
    env.targetKey
    env.targetVersion (optional)
    */

    stage('Get Available Versions') {
        library identifier: 'jenkins@main'

        copyParamsToEnv()
        if (!env.targetVersion) {
            availableVersions = getAvailableVersions(repo: env.repository)
            env.targetVersion = input(
                message: 'Choose the target version:',
                parameters: [
                    choice(name: 'version', choices: availableVersions)
                ]
            )
        } else {
            print "Deploying ${env.targetVersion}..."
        }
    }

    stage('Deploy') {
        withCredentials([
            sshUserPrivateKey(credentialsId: env.targetKey, usernameVariable: 'SSH_USER', keyFileVariable: 'SSH_PRIV_KEY'),
            usernamePassword(credentialsId: 'github-personal-access-token', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_PASSWORD')
        ]) {
            sh "ssh -i $SSH_PRIV_KEY -o StrictHostKeyChecking=no $SSH_USER@${env.target} '\
                docker login -u $GITHUB_USERNAME -p $GITHUB_PASSWORD ghcr.io;\
                docker pull ghcr.io/jamiefdhurst/${env.targetImage}:${env.targetVersion};\
                docker tag ghcr.io/jamiefdhurst/${env.targetImage}:${env.targetVersion} ${env.targetImage}:${env.targetVersion};\
                [ -f /etc/versions/${env.targetImage} ] && sudo sh -c \"echo \'${env.targetVersion}\' > /etc/versions/${env.targetImage}\";\
                sudo sed -i \'s/${env.targetImage}:.*\$/${env.targetImage}:${env.targetVersion}/\' /etc/supervisor/conf.d/${env.targetImage}.conf;\
                sudo supervisorctl reread;\
                sudo supervisorctl update'"
        }
    }
}
