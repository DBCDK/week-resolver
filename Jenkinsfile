#!groovy
def workerNode = "devel12"
def teamSlackNotice = 'team-x-notice'
def teamSlackWarning = 'team-x-warning'

pipeline {
    agent { label workerNode }
    tools {
        maven "Maven 3"
    }
    environment {
        GITLAB_PRIVATE_TOKEN = credentials("metascrum-gitlab-api-token")
        SONAR_SCANNER_HOME = tool 'SonarQube Scanner from Maven Central'
        SONAR_SCANNER = "$SONAR_SCANNER_HOME/bin/sonar-scanner"
        SONAR_PROJECT_KEY = "week-resolver"
        SONAR_SOURCES = "service/src"
        SONAR_TESTS = "service/test"
    }
    triggers {
        cron(env.BRANCH_NAME == 'main' ? "H 3 * * 17" : "")
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
        stage("sonarqube") {
            steps {
                withSonarQubeEnv(installationName: 'sonarqube.dbc.dk') {
                    script {
                        def status = 0

                        def sonarOptions = "-Dsonar.branch.name=${BRANCH_NAME}"
                        if (env.BRANCH_NAME != 'master') {
                            sonarOptions += " -Dsonar.newCode.referenceBranch=master"
                        }

                        // Do sonar via maven
                        status += sh returnStatus: true, script: """
                            mvn -B $sonarOptions sonar:sonar
                        """

                        if (status != 0) {
                            error("build failed")
                        }
                    }
                }
            }
        }
        stage("deploy") {
            when {
              branch "master"
            }
            steps {
              sh "mvn jar:jar deploy:deploy"
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
    post {
        success {
            script {
                if (BRANCH_NAME == 'main') {
                    def dockerImageName = readFile(file: 'docker.out')
                    slackSend(channel: teamSlackNotice,
                            color: 'good',
                            message: "${JOB_NAME} #${BUILD_NUMBER} completed, and pushed ${dockerImageName} to artifactory.",
                            tokenCredentialId: 'slack-global-integration-token')
                }
            }
        }
        fixed {
            script {
                if ("${env.BRANCH_NAME}" == 'main') {
                    slackSend(channel: teamSlackWarning,
                            color: 'good',
                            message: "${env.JOB_NAME} #${env.BUILD_NUMBER} back to normal: ${env.BUILD_URL}",
                            tokenCredentialId: 'slack-global-integration-token')
                }
            }
        }
        failure {
            script {
                if ("${env.BRANCH_NAME}".equals('main')) {
                    slackSend(channel: teamSlackWarning,
                        color: 'warning',
                        message: "${env.JOB_NAME} #${env.BUILD_NUMBER} failed and needs attention: ${env.BUILD_URL}",
                        tokenCredentialId: 'slack-global-integration-token')
                }
            }
        }
    }
}
