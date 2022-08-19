def call() {
    params.each { key, value -> env[key] = value }
}
