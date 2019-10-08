#!groovy

pipeline {
    agent { label "devel9" }
    tools {
        // refers to the name set in manage jenkins -> global tool configuration
        maven "Maven 3"
    }
    triggers {
        pollSCM("H/03 * * * *")
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
        stage("build") {
            steps {
                script {
                    def status = sh returnStatus: true, script:  """
                        rm -rf \$WORKSPACE/.repo
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo dependency:resolve dependency:resolve-plugins >/dev/null
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo clean
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo --fail-at-end org.jacoco:jacoco-maven-plugin:prepare-agent install -Dsurefire.useFile=false
                    """

                    // We want code-coverage and pmd/findbugs even if unittests fails
                    status += sh returnStatus: true, script:  """
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo pmd:pmd pmd:cpd findbugs:findbugs javadoc:aggregate
                    """

                    junit testResults: '**/target/*-reports/TEST-*.xml'

                    def java = scanForIssues tool: [$class: 'Java']
                    def javadoc = scanForIssues tool: [$class: 'JavaDoc']
                    publishIssues issues:[java, javadoc], unstableTotalAll:1

                    def pmd = scanForIssues tool: [$class: 'Pmd'], pattern: '**/target/pmd.xml'
                    publishIssues issues:[pmd], unstableTotalAll:1

                    def cpd = scanForIssues tool: [$class: 'Cpd'], pattern: '**/target/cpd.xml'
                    publishIssues issues:[cpd]

                    def findbugs = scanForIssues tool: [$class: 'FindBugs'], pattern: '**/target/findbugsXml.xml'
                    publishIssues issues:[findbugs], unstableTotalAll:1

                    step([$class: 'JacocoPublisher',
                          execPattern: 'target/*.exec,**/target/*.exec',
                          classPattern: 'target/classes,**/target/classes',
                          sourcePattern: 'src/main/java,**/src/main/java',
                          exclusionPattern: 'src/test*,**/src/test*,**/*?Request.*,**/*?Response.*,**/*?Request$*,**/*?Response$*,**/*?DTO.*,**/*?DTO$*'
                    ])

                    if ( status != 0 ) {
                        currentBuild.result = Result.FAILURE
                    }

                }
            }
        }
        stage("build docker container") {
            steps {
                script {
                    def image = docker.build("docker-io.dbc.dk/weekresolver:${env.BRANCH_NAME}-${env.BUILD_NUMBER}",
                            "-f src/main/docker/Dockerfile --pull --no-cache .")
                    image.push()
                }

            }
        }
    }
}
