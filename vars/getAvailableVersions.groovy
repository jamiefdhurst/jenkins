/* groovylint-disable LineLength */
import groovy.json.JsonSlurperClassic

def call(Map vars=[:]) {
    repo = vars.get('repo')

    if (!repo) {
        throw new Exception('getVersion exception: "repo" parameter must be supplied')
    }

    withCredentials([usernameColonPassword(credentialsId: 'github-personal-access-token', variable: 'GITHUB_API_TOKEN')]) {
        tags = sh(
            script: 'curl --silent -u ' + GITHUB_API_TOKEN + ' "https://api.github.com/repos/' + repo + '/releases" | grep \'"tag_name":\' | sed -E \'s/.*"([^"]+)".*/\\1/\'',
            returnStdout: true
        ).tokenize("\n")
    }

    return tags + ['latest']
}
