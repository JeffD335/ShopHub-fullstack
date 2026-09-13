pipeline {
  agent any

  stages {
    stage('Backend Test') {
      steps {
        dir('ShopHub-backend') {
          sh 'mvn -B test'
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
  }
}
