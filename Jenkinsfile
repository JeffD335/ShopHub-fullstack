pipeline {
  agent {
    label 'shophub-build'
  }

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }

  environment {
    CI = 'true'
  }

  stages {
    stage('Backend Test') {
      steps {
        dir('ShopHub-backend') {
          sh 'mvn -B clean verify'
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'ShopHub-backend/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Frontend Build') {
      steps {
        dir('ShopHub-frontend') {
          sh 'npm ci'
          sh 'npm run build'
        }
      }
    }

    stage('Archive Artifacts') {
      steps {
        archiveArtifacts artifacts: 'ShopHub-backend/target/*.jar, ShopHub-frontend/dist/**', fingerprint: true
      }
    }
  }
}
