node {
    stage('Backup') {
        withCredentials([sshUserPrivateKey(credentialsId: 'hiawatha-ssh-key', usernameVariable: 'SSH_USER', keyFileVariable: 'SSH_PRIV_KEY')]) {
        sh """
        ssh -i $SSH_PRIV_KEY -o StrictHostKeyChecking=no $SSH_USER@journal-priv.jamiehurst.co.uk 'aws s3 cp /opt/journal/data/journal.db s3://jamiehurst-backup/journal/`date +%Y%m%d-%H%M%S`'
        """
        }
    }
}