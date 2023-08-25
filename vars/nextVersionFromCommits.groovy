// groovylint-disable LineLength
// groovylint-disable NestedBlockDepth
// groovylint-disable CompileStatic
import groovy.json.JsonSlurperClassic

def call(Map vars=[:]) {
    repo = vars.get('repo')
    lastVersion = vars.getOrDefault('lastVersion', '')

    if (!repo) {
        throw new Exception('getVersion exception: "repo" parameter must be supplied')
    }

    url = "https://api.github.com/repos/${repo}/commits"
    if (lastVersion && lastVersion != 'v0') {
        url = "https://api.github.com/repos/${repo}/compare/${lastVersion}...HEAD"
    }
    withCredentials([usernameColonPassword(credentialsId: 'github-personal-access-token', variable: 'GITHUB_API_TOKEN')]) {
        responseJson = sh(script: "curl --silent -u ${GITHUB_API_TOKEN} '${url}'", returnStdout: true)
    }
    JsonSlurperClassic slurper = new JsonSlurperClassic()
    details = slurper.parseText(responseJson)

    // Parse the commits to find anything of value
    countMajorKeywords = 0
    countMinorKeywords = 0
    countPatchKeywords = 0
    countSkips = 0
    commits = details.clone()
    // groovylint-disable-next-line Instanceof
    if (!(details instanceof List)) {
        commits = details.commits.clone()
    }

    release = [changeLog: [], release: '']

    commits.each{ commit ->
        if (commit.commit.message.toLowerCase() =~ /^(break|release)/) {
            countMajorKeywords += 1
            release.changeLog.push(commit.commit.message)
        }
        if (commit.commit.message.toLowerCase() =~ /^(feature|new|add|update)/) {
            countMinorKeywords += 1
            release.changeLog.push(commit.commit.message)
        }
        if (commit.commit.message.toLowerCase() =~ /^(fix|rework|improve|correct|cleanup)/) {
            countPatchKeywords += 1
            release.changeLog.push(commit.commit.message)
        }
        if (commit.commit.message.toLowerCase() =~ /^(skip ci)/) {
            countSkips += 1
        }
    }

    if (countMajorKeywords) {
        release.release = 'major'
        return release
    }
    if (countMinorKeywords) {
        release.release = 'minor'
        return release
    }
    if (countPatchKeywords) {
        release.release = 'patch'
        return release
    }
    if (countSkips) {
        return release
    }
    release.release = 'revision'
    return release
}
