// groovylint-disable CompileStatic
// groovylint-disable LineLength
pipeline {
    agent any

    stages {
        stage('Lint') {
            steps {
                sh 'docker run --rm -u "$(id -u):$(id -g)" -v $(pwd):/tmp -w=/tmp nvuillam/npm-groovy-lint --failon warning'
            }
        }

        stage('Package and Release') {
            when {
                branch 'main'
            }
            steps {
                build job: '/github/jenkins-folder/release', wait: true
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
