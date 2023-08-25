// groovylint-disable LineLength
// groovylint-disable NestedBlockDepth
// groovylint-disable CompileStatic
def call(Map vars=[:]) {
    repo = vars.get('repo')

    if (!repo) {
        throw new Exception('getVersion exception: "repo" parameter must be supplied')
    }

    withCredentials([usernameColonPassword(credentialsId: 'github-personal-access-token', variable: 'GITHUB_API_TOKEN')]) {
        latestVersion = sh(
            /* groovylint-disable-next-line LineLength */
            script: 'curl --silent -u ' + GITHUB_API_TOKEN + ' "https://api.github.com/repos/' + repo + '/releases/latest" | grep \'"tag_name":\' | sed -E \'s/.*"([^"]+)".*/\\1/\'',
            returnStdout: true
        ).trim()
    }
    if (!latestVersion) {
        versionParts = [0]
        latestVersion = 'v0'
    } else {
        versionParts = latestVersion.tokenize('v')[0].tokenize('.')
    }
    major = versionParts[0].toInteger()
    try {
        minor = versionParts[1].toInteger()
    } catch (e) {
        minor = 0
    }
    try {
        patch = versionParts[2].toInteger()
    } catch (e) {
        patch = 0
    }
    try {
        revision = versionParts[3].toInteger()
    } catch (e) {
        revision = 0
    }

    return [full: latestVersion, numbers: latestVersion.tokenize('v')[0], major: major, minor: minor, patch: patch, revision: revision]
}
