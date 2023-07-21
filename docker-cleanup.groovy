node {
    diskUsed = 0

    stage('Get Free Space') {
        diskUsed = sh(
            script: "df -h | grep /dev/root | sed 's/.*\\s\\([0-9]\\+\\)%.*/\\1/'",
            returnStdout: true
        ).trim()
        print 'Disk space used on CI server: ' + diskUsed + '%...'
    }

    if (diskUsed.toInteger() > 85) {
        stage('Cleanup') {
            print 'Cleaning up Docker...'
            sh 'docker system prune -af --volumes'
        }
    }
}