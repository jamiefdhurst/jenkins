// groovylint-disable CompileStatic
def call() {
    params.each { key, value -> env[key] = value }
}
