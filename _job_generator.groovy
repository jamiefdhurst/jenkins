// groovylint-disable LineLength
// groovylint-disable NestedBlockDepth
// groovylint-disable CompileStatic
folder('github') {
    displayName('GitHub')
}

folder('github/blog-folder') {
    displayName('Blog')
}

multibranchPipelineJob('github/blog-folder/build') {
    displayName 'Blog - Build'
    description 'Jamie\'s Blog Source'
    branchSources {
        branchSource {
            buildStrategies {
                ignoreCommitterStrategy {
                    ignoredAuthors('jamie,jenkins-ci,jenkins-ci@jamiehurst.co.uk')
                    allowBuildIfNotExcludedAuthor(true)
                }
            }
            source {
                github {
                    id('79704f18-78ba-40de-a16c-61f5730bd86b')
                    credentialsId('github-personal-access-token')
                    configuredByUrl(false)
                    repositoryUrl('https://github.com/jamiefdhurst/blog.git')
                    repoOwner('jamiefdhurst')
                    repository('blog')
                    traits {
                        gitHubBranchDiscovery {
                            strategyId(1)
                        }
                        gitHubPullRequestDiscovery {
                            strategyId(1)
                        }
                    }
                }
            }
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(7)
        }
    }
    triggers {
        periodicFolderTrigger {
            interval('5m')
        }
    }
}

pipelineJob('github/blog-folder/release') {
    displayName 'Blog - Release'
    description 'Release new version of Blog to GitHub'
    environmentVariables {
        env('repository', 'jamiefdhurst/blog')
        env('versionFiles', 'blog/config.py')
        env('releaseBranch', 'main')
        env('automaticRelease', true)
        env('pushRelease', true)
        env('dockerImage', 'blog')
        env('dockerRebuild', true)
    }
    logRotator {
        numToKeep(10)
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/jamiefdhurst/jenkins')
                        credentials('github-personal-access-token')
                    }
                    branch('main')
                }
            }
            scriptPath('release-version.groovy')
        }
    }
}

pipelineJob('github/blog-folder/deploy') {
    displayName 'Blog - Deploy'
    description 'Deploy Blog to jamiehurst.co.uk'
    environmentVariables {
        env('repository', 'jamiefdhurst/blog')
        env('target', 'journal-priv.jamiehurst.co.uk')
        env('targetImage', 'blog')
        env('targetKey', 'hiawatha-ssh-key')
    }
    logRotator {
        numToKeep(10)
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/jamiefdhurst/jenkins')
                        credentials('github-personal-access-token')
                    }
                    branch('main')
                }
            }
            scriptPath('deploy.groovy')
        }
    }
}

folder('github/goldeneye-folder') {
    displayName('GoldenEye')
}

multibranchPipelineJob('github/goldeneye-folder/build') {
    displayName 'GoldenEye - Build'
    description 'GoldenEye Source'
    branchSources {
        branchSource {
            buildStrategies {
                ignoreCommitterStrategy {
                    ignoredAuthors('jamie,jenkins-ci,jenkins-ci@jamiehurst.co.uk')
                    allowBuildIfNotExcludedAuthor(true)
                }
            }
            source {
                github {
                    id('79704f18-78ba-40de-a16c-61f5730bd86c')
                    credentialsId('github-personal-access-token')
                    configuredByUrl(false)
                    repositoryUrl('https://github.com/jamiefdhurst/goldeneye.git')
                    repoOwner('jamiefdhurst')
                    repository('goldeneye')
                    traits {
                        gitHubBranchDiscovery {
                            strategyId(1)
                        }
                        gitHubPullRequestDiscovery {
                            strategyId(1)
                        }
                    }
                }
            }
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(7)
        }
    }
    triggers {
        periodicFolderTrigger {
            interval('5m')
        }
    }
}

pipelineJob('github/goldeneye-folder/release') {
    displayName 'GoldenEye - Release'
    description 'Release new version of GoldenEye to GitHub'
    environmentVariables {
        env('repository', 'jamiefdhurst/goldeneye')
        env('releaseBranch', 'main')
        env('automaticRelease', true)
        env('pushRelease', true)
        env('dockerImage', 'goldeneye')
    }
    logRotator {
        numToKeep(10)
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/jamiefdhurst/jenkins')
                        credentials('github-personal-access-token')
                    }
                    branch('main')
                }
            }
            scriptPath('release-version.groovy')
        }
    }
}

// pipelineJob('github/goldeneye-folder/deploy') {
//     displayName 'GoldenEye - Deploy'
//     description 'Deploy GoldenEye'
//     environmentVariables {
//         env('repository', 'jamiefdhurst/goldeneye')
//         env('target', 'journal-priv.jamiehurst.co.uk')
//         env('targetImage', 'goldeneye')
//         env('targetKey', 'hiawatha-ssh-key')
//     }
//     logRotator {
//         numToKeep(10)
//     }
//     definition {
//         cpsScm {
//             scm {
//                 git {
//                     remote {
//                         url('https://github.com/jamiefdhurst/jenkins')
//                         credentials('github-personal-access-token')
//                     }
//                     branch('main')
//                 }
//             }
//             scriptPath('deploy.groovy')
//         }
//     }
// }

folder('github/jenkins-folder') {
    displayName('Jenkins Libs')
}

multibranchPipelineJob('github/jenkins-folder/build') {
    displayName 'Jenkins Libs - Build'
    description 'Jenkins libraries for deploy, release, etc.'
    branchSources {
        branchSource {
            buildStrategies {
                ignoreCommitterStrategy {
                    ignoredAuthors('jamie,jenkins-ci,jenkins-ci@jamiehurst.co.uk')
                    allowBuildIfNotExcludedAuthor(true)
                }
            }
            source {
                github {
                    id('40f73a63-7f0d-4c71-8ac2-0f0b2f65e446')
                    credentialsId('github-personal-access-token')
                    configuredByUrl(false)
                    repositoryUrl('https://github.com/jamiefdhurst/jenkins.git')
                    repoOwner('jamiefdhurst')
                    repository('jenkins')
                    traits {
                        gitHubBranchDiscovery {
                            strategyId(1)
                        }
                        gitHubPullRequestDiscovery {
                            strategyId(1)
                        }
                    }
                }
            }
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(7)
        }
    }
    triggers {
        periodicFolderTrigger {
            interval('5m')
        }
    }
}

pipelineJob('github/jenkins-folder/release') {
    displayName 'Jenkins Libs - Release'
    description 'Release new version of Jenkins Libs to GitHub'
    environmentVariables {
        env('repository', 'jamiefdhurst/jenkins')
        env('releaseBranch', 'main')
        env('automaticRelease', true)
        env('pushRelease', true)
    }
    logRotator {
        numToKeep(10)
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/jamiefdhurst/jenkins')
                        credentials('github-personal-access-token')
                    }
                    branch('main')
                }
            }
            scriptPath('release-version.groovy')
        }
    }
}

folder('github/journal-folder') {
    displayName('Journal')
}

multibranchPipelineJob('github/journal-folder/build') {
    displayName 'Journal - Build'
    description 'Journal open-source project'
    branchSources {
        branchSource {
            buildStrategies {
                ignoreCommitterStrategy {
                    ignoredAuthors('jamie,jenkins-ci,jenkins-ci@jamiehurst.co.uk')
                    allowBuildIfNotExcludedAuthor(true)
                }
            }
            source {
                github {
                    id('40f73a63-7f0d-4c71-8ac2-0f0b2f65e446')
                    credentialsId('github-personal-access-token')
                    configuredByUrl(false)
                    repositoryUrl('https://github.com/jamiefdhurst/journal.git')
                    repoOwner('jamiefdhurst')
                    repository('journal')
                    traits {
                        gitHubBranchDiscovery {
                            strategyId(1)
                        }
                        gitHubPullRequestDiscovery {
                            strategyId(1)
                        }
                    }
                }
            }
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(7)
        }
    }
    triggers {
        periodicFolderTrigger {
            interval('5m')
        }
    }
}

pipelineJob('github/journal-folder/release') {
    displayName 'Journal - Release'
    description 'Release new version of Journal to GitHub'
    environmentVariables {
        env('repository', 'jamiefdhurst/journal')
        env('versionFiles', 'journal.go,web/app/package.json')
        env('automaticRelease', true)
        env('pushRelease', true)
        env('dockerImage', 'journal')
        env('dockerRebuild', true)
    }
    logRotator {
        numToKeep(10)
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/jamiefdhurst/jenkins')
                        credentials('github-personal-access-token')
                    }
                    branch('main')
                }
            }
            scriptPath('release-version.groovy')
        }
    }
}

pipelineJob('github/journal-folder/deploy') {
    displayName 'Journal - Deploy'
    description 'Deploy Journal to journal.jamiehurst.co.uk'
    environmentVariables {
        env('repository', 'jamiefdhurst/journal')
        env('target', 'journal-priv.jamiehurst.co.uk')
        env('targetImage', 'journal')
        env('targetKey', 'hiawatha-ssh-key')
    }
    logRotator {
        numToKeep(10)
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/jamiefdhurst/jenkins')
                        credentials('github-personal-access-token')
                    }
                    branch('main')
                }
            }
            scriptPath('deploy.groovy')
        }
    }
}

folder('github/yendo-mysql-folder') {
    displayName('Yendo-mysql')
}

multibranchPipelineJob('github/yendo-mysql-folder/build') {
    displayName 'Yendo-mysql - Build'
    description 'Yendo-mysql open-source Golang project'
    branchSources {
        branchSource {
            buildStrategies {
                ignoreCommitterStrategy {
                    ignoredAuthors('jamie,jenkins-ci,jenkins-ci@jamiehurst.co.uk')
                    allowBuildIfNotExcludedAuthor(true)
                }
            }
            source {
                github {
                    id('62f9e77a-0ee3-415f-8a87-4078b9eee805')
                    credentialsId('github-personal-access-token')
                    configuredByUrl(false)
                    repositoryUrl('https://github.com/jamiefdhurst/yendo-mysql.git')
                    repoOwner('jamiefdhurst')
                    repository('yendo-mysql')
                    traits {
                        gitHubBranchDiscovery {
                            strategyId(1)
                        }
                        gitHubPullRequestDiscovery {
                            strategyId(1)
                        }
                    }
                }
            }
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(7)
        }
    }
    triggers {
        periodicFolderTrigger {
            interval('5m')
        }
    }
}

pipelineJob('github/yendo-mysql-folder/release') {
    displayName 'Yendo-mysql - Release'
    description 'Release new version of Yendo-mysql to GitHub'
    environmentVariables {
        env('repository', 'jamiefdhurst/yendo-mysql')
        env('releaseBranch', 'main')
        env('automaticRelease', false)
        env('pushRelease', false)
    }
    logRotator {
        numToKeep(10)
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/jamiefdhurst/jenkins')
                        credentials('github-personal-access-token')
                    }
                    branch('main')
                }
            }
            scriptPath('release-version.groovy')
        }
    }
}

folder('utilities') {
    displayName('Utilities')
}

pipelineJob('utilities/backup-journal') {
    displayName 'Backup Journal DB'
    description 'Backup Journal DB to S3'
    logRotator {
        numToKeep(10)
    }
    triggers {
        cron('6 5 * * *')
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/jamiefdhurst/jenkins')
                        credentials('github-personal-access-token')
                    }
                    branch('main')
                }
            }
            scriptPath('backup-journal.groovy')
        }
    }
}

pipelineJob('utilities/docker-cleanup') {
    displayName 'Docker Cleanup'
    description 'Daily cleanup of Docker when file space > 85% used'
    logRotator {
        numToKeep(10)
    }
    triggers {
        cron('7 5 * * *')
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/jamiefdhurst/jenkins')
                        credentials('github-personal-access-token')
                    }
                    branch('main')
                }
            }
            scriptPath('docker-cleanup.groovy')
        }
    }
}

pipelineJob('utilities/update-infrastructure') {
    displayName 'Update Infrastructure'
    description 'Periodically update the full infrastructure by running Terraform from a temporary location'
    logRotator {
        numToKeep(10)
    }
    triggers {
        cron('8 5 * * 6')
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/jamiefdhurst/jenkins')
                        credentials('github-personal-access-token')
                    }
                    branch('main')
                }
            }
            scriptPath('update-infrastructure.groovy')
        }
    }
}