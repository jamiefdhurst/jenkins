/* groovylint-disable LineLength */
import groovy.json.JsonOutput

/* groovylint-disable-next-line CompileStatic */
node {
    // Presumes env.repository is available
    stage('Get Latest Version') {
        library identifier: 'jenkins@main'

        copyParamsToEnv()
        print 'Getting latest version for: ' + env.repository
        version = getVersion(repo: env.repository)
        print 'Latest version is: ' + version.full
    }

    stage('Determine Next Version') {
        if (env.automaticRelease) {
            releaseDetails = nextVersionFromCommits(repo: env.repository, lastVersion: version.full)
            release = releaseDetails.release
        } else {
            release = input(
                message: 'Choose the next release type:',
                parameters: [
                    choice(name: 'release', choices: ['', 'Revision', 'Patch', 'Minor', 'Major'])
                ]
            )
        }
        print 'Releasing a ' + release + ' version'

        nextVersion = calculateNextVersion(version: version.clone(), release: release)
        print 'Calculated new version to be: ' + nextVersion.full

        releaseText = "Release ${nextVersion.full}"
        if (releaseDetails.changeLog.size()) {
            releaseText += ', including the following commits: \n- ' + releaseDetails.changeLog.join('\n- ')
        }
    }
    stage('Checkout & Commit Changes') {
        if (nextVersion.full != version.full && env.versionFiles) {
            checkout([
                $class: 'GitSCM',
                branches: [[name: 'refs/heads/' + (env.releaseBranch ?: 'main')]],
                userRemoteConfigs: [
                    [credentialsId: 'github-personal-access-token', url: "https://github.com/${env.repository}.git"]
                ]
            ])

            for (versionFile in env.versionFiles.tokenize(',')) {
                print 'Updating version in file: ' + versionFile
                String file = readFile(file: versionFile)
                print 'Before: ' + file
                file = file.replace(version.full, nextVersion.full)
                print 'After:' + file
                writeFile(file: versionFile, text: file)
            }

            sh 'git config --global user.name "Jenkins CI"'
            sh 'git config --global user.email "jenkins-ci@jamiehurst.co.uk"'
            sh 'git commit -am "Skip CI: updated version number"'
            withCredentials([usernamePassword(credentialsId: 'github-personal-access-token', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_PASSWORD')]) {
                sh "git push 'https://${GIT_USERNAME}:${GITHUB_PASSWORD}@github.com/${env.repository}.git'"
            }
        }
    }
    stage('Push Tag') {
        if (nextVersion.full != version.full) {
            print "Releasing ${nextVersion.full}..."

            releaseData = JsonOutput.toJson([
                tag_name: nextVersion.full,
                target_commitish: env.releaseBranch ?: 'main',
                name: nextVersion.full,
                body: releaseText,
                draft: false,
                prerelease: false
            ])

            print 'Changelog: \n' + releaseText

            if (env.pushRelease) {
                withCredentials([usernameColonPassword(credentialsId: 'github-personal-access-token', variable: 'GITHUB_API_TOKEN')]) {
                    sh "curl -u $GITHUB_API_TOKEN -X POST -H 'Accept: application/vnd.github.v3+json' 'https://api.github.com/repos/${env.repository}/releases' -d '$releaseData'"
                }
            } else {
                print 'Skipping GitHub push...'
            }
        } else {
            print 'Not releasing, no new version has been calculated...'
        }
    }
    stage('Push Docker Image') {
        if (env.dockerImage) {
            print 'Pushing Docker image...'
            withCredentials([usernamePassword(credentialsId: 'github-personal-access-token', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_PASSWORD')]) {
                sh "docker login -u $GITHUB_USERNAME -p $GITHUB_PASSWORD ghcr.io"
                sh """
                docker tag ${env.dockerImage} ghcr.io/jamiefdhurst/${env.dockerImage}:latest
                docker tag ${env.dockerImage} ghcr.io/jamiefdhurst/${env.dockerImage}:${nextVersion.full}
                docker push ghcr.io/jamiefdhurst/${env.dockerImage}:latest
                docker push ghcr.io/jamiefdhurst/${env.dockerImage}:${nextVersion.full}
                """
            }
        }
    }
}