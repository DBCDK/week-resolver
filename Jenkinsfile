#!groovy
def workerNode = "devel11"
pipeline {
    agent { label workerNode }
    tools {
        maven "Maven 3"
        jdk 'jdk11'
    }
    environment {
        GITLAB_PRIVATE_TOKEN = credentials("metascrum-gitlab-api-token")
    }
    triggers {
        githubPush()
        upstream('/Docker-payara6-bump-trigger')
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
                junit "service/target/surefire-reports/TEST-*.xml"
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
                      unstableTotalAll: "10",
                      failedTotalAll: "10"])
            }
        }
        stage("build docker container") {
            when {
                branch "master"
            }
            steps {
                script {
                    docker.image("docker-metascrum.artifacts.dbccloud.dk/weekresolver-service:${env.BRANCH_NAME}-${env.BUILD_NUMBER}").push()
                }

            }
        }
        stage("bump docker tag in weekresolver-secrets") {
            agent {
                docker {
                    label workerNode
                    image "docker-dbc.artifacts.dbccloud.dk/build-env:latest"
                    alwaysPull true
                }
            }
            when {
                branch "master"
            }
            steps {
                script {
                    sh """  
                        set-new-version weekresolver-service.yml ${env.GITLAB_PRIVATE_TOKEN} metascrum/weekresolver-secrets  ${env.BRANCH_NAME}-${env.BUILD_NUMBER} -b staging
                    """
                }
            }
        }

    }
}
