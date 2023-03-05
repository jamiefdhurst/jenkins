def call(Map vars=[:]) {
    version = vars.get('version', [full: '', numbers: '', major: 0, minor: 0, patch: 0, revision: 0])
    release = vars.get('release', '').toLowerCase()

    if (!release) {
        return version
    }

    switch (release) {
        case 'major':
            version.major += 1
            version.minor = 0
            version.patch = 0
            version.revision = 0
            break;
        case 'minor':
            version.minor += 1
            version.patch = 0
            version.revision = 0
            break;
        case 'patch':
            version.patch += 1
            version.revision = 0
            break;
        case 'revision':
            version.revision += 1
    }
    newVersion = 'v' + version.major.toString()
    if (version.minor > 0 || version.patch > 0 || version.revision > 0) {
        newVersion = newVersion + '.' + version.minor.toString()
        if (version.patch > 0 || version.revision > 0) {
            newVersion = newVersion + '.' + version.patch.toString()
            if (version.revision > 0) {
                newVersion = newVersion + '.' + version.revision.toString()
            }
        }
    }
    version.full = newVersion
    version.numbers = version.full.tokenize('v')[0]
    return version
}
