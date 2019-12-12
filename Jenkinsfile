#!groovy
def workerNode = "devel9"
pipeline {
    agent { label workerNode }
    tools {
        // refers to the name set in manage jenkins -> global tool configuration
        maven "Maven 3"
    }
    environment {
        GITLAB_PRIVATE_TOKEN = credentials("metascrum-gitlab-api-token")
    }
    triggers {
        githubPush()
        // This project uses the docker.dbc.dk/payara5-micro container
        upstream('/Docker-payara5-bump-trigger')
    }
    options {
        timestamps()
    }
    stages {
        stage("clear workspace") {
            steps {
                deleteDir()
                checkout scm
            }
        }
        stage("verify") {
            steps {
                sh "mvn -D sourcepath=src/main/java verify pmd:pmd javadoc:aggregate"
                junit "target/surefire-reports/TEST-*.xml"
            }
        }
        stage("warnings") {
            agent {label workerNode}
            steps {
                warnings consoleParsers: [
                        [parserName: "Java Compiler (javac)"],
                        [parserName: "JavaDoc Tool"]
                ],
                        unstableTotalAll: "0",
                        failedTotalAll: "0"
            }
        }
        stage("pmd") {
            agent {label workerNode}
            steps {
                step([$class: 'hudson.plugins.pmd.PmdPublisher',
                      pattern: '**/target/pmd.xml',
                      unstableTotalAll: "0",
                      failedTotalAll: "0"])
            }
        }
        stage("build docker container") {
            when {
                branch "master"
            }
            steps {
                script {
                    def image = docker.build("docker-io.dbc.dk/weekresolver:${env.BRANCH_NAME}-${env.BUILD_NUMBER}",
                            "-f src/main/docker/Dockerfile --pull --no-cache .")
                    image.push()
                }

            }
        }
        stage("bump docker tag in weekresolver-secrets") {
            agent {
                docker {
                    label workerNode
                    image "docker.dbc.dk/build-env:latest"
                    alwaysPull true
                }
            }
            when {
                branch "master"
            }
            steps {
                script {
                    sh """  
                        set-new-version weekresolver.yml ${env.GITLAB_PRIVATE_TOKEN} metascrum/week-resolver-secrets  ${env.BRANCH_NAME}-${env.BUILD_NUMBER} -b staging
                    """
                }
            }
        }

    }
}
