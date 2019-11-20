pipeline {
  options {
      disableConcurrentBuilds()
  }
  agent {
    label "jenkins-maven-java11"
  }
  environment {
    ORG = 'activiti'
    APP_NAME = 'activiti-cloud-acceptance-scenarios'

    GATEWAY_HOST = "activiti-cloud-gateway.jx-staging.35.228.195.195.nip.io"
    SSO_HOST = "activiti-keycloak.jx-staging.35.228.195.195.nip.io"
    REALM = "activiti"
    
    PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
    PREVIEW_NAMESPACE = "scenarios-$BRANCH_NAME-$BUILD_NUMBER".toLowerCase()
    HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
   
  }
  stages {
    stage('CI Build and push snapshot') {
      when {
        branch 'PR-*'
      }
      steps {
        container('maven') {
          sh "mvn clean install -DskipTests"
        }
      }
    }

     stage('Build Release') {
       when {
         branch 'master'
       }
       steps {
         container('maven') {
           // ensure we're not on a detached head
           sh "echo \$(jx-release-version) > VERSION"
           sh "git checkout master"
           sh "git config --global credential.helper store"
           sh "jx step git credentials"
           sh "mvn clean install -DskipTests"
           sh "make tag"
           sh "updatebot push-version --kind  make  ACTIVITI_CLOUD_ACCEPTANCE_SCENARIOUS_VERSION \$(cat VERSION)"
          }
      }
     }
  }
  post {
        failure {
           slackSend(
             channel: "#activiti-community-builds",
             color: "danger",
             message: "activiti-cloud-acceptance-scenarios branch=$BRANCH_NAME is failed http://jenkins.jx.35.228.195.195.nip.io/job/Activiti/job/activiti-cloud-acceptance-scenarios/"
           )
        } 
        always {
          cleanWs()
        }
  }
}
