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
  }
}