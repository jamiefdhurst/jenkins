/* groovylint-disable NestedBlockDepth */
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
        github {
            id('79704f18-78ba-40de-a16c-61f5730bd86b')
            scanCredentialsId('github-personal-access-token')
            repoOwner('jamiefdhurst')
            repository('blog')
            buildForkPRHead(false)
            buildForkPRMerge(false)
            buildOriginBranchWithPR(false)
            buildOriginPRHead(false)
            buildOriginPRMerge(true)
            orphanedItemStrategy {
                discardOldItems {
                    daysToKeep(7)
                }
            }
        }
    }
    triggers {
        cron('@daily')
    }
}

pipelineJob('github/blog-folder/release') {
    displayName 'Blog - Release'
    description 'Release new version of Blog to GitHub'
    environmentVariables {
        env('repository', 'jamiefdhurst/blog')
        env('releaseBranch', 'main')
        env('automaticRelease', true)
        env('pushRelease', true)
        env('dockerImage', 'blog')
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
    description 'Jamie\'s GoldenEye Source'
    branchSources {
        github {
            id('79704f18-78ba-40de-a16c-61f5730bd86b')
            scanCredentialsId('github-personal-access-token')
            repoOwner('jamiefdhurst')
            repository('goldeneye')
            buildForkPRHead(false)
            buildForkPRMerge(false)
            buildOriginBranchWithPR(false)
            buildOriginPRHead(false)
            buildOriginPRMerge(true)
            orphanedItemStrategy {
                discardOldItems {
                    daysToKeep(7)
                }
            }
        }
    }
    triggers {
        cron('@daily')
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

folder('github/journal-folder') {
    displayName('Journal')
}

multibranchPipelineJob('github/journal-folder/build') {
    displayName 'Journal - Build'
    description 'Journal OS project'
    branchSources {
        github {
            id('40f73a63-7f0d-4c71-8ac2-0f0b2f65e446')
            scanCredentialsId('github-personal-access-token')
            repoOwner('jamiefdhurst')
            repository('journal')
            buildForkPRHead(false)
            buildForkPRMerge(false)
            buildOriginBranchWithPR(false)
            buildOriginPRHead(false)
            buildOriginPRMerge(true)
            orphanedItemStrategy {
                discardOldItems {
                    daysToKeep(7)
                }
            }
        }
    }
    triggers {
        cron('@daily')
    }
}

pipelineJob('github/journal-folder/release') {
    displayName 'Journal - Release'
    description 'Release new version of Journal to GitHub'
    environmentVariables {
        env('repository', 'jamiefdhurst/journal')
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
    description 'Yendo-mysql OS Golang project'
    branchSources {
        github {
            id('62f9e77a-0ee3-415f-8a87-4078b9eee805')
            scanCredentialsId('github-personal-access-token')
            repoOwner('jamiefdhurst')
            repository('yendo-mysql')
            buildForkPRHead(false)
            buildForkPRMerge(false)
            buildOriginBranchWithPR(false)
            buildOriginPRHead(false)
            buildOriginPRMerge(true)
            orphanedItemStrategy {
                discardOldItems {
                    daysToKeep(7)
                }
            }
        }
    }
    triggers {
        cron('@daily')
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
        cron('H 0 * * *')
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
        cron('H 3 * * *')
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
        cron('H 5 * * *')
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