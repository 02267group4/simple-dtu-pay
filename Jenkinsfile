pipeline {
    agent any

    environment {
        BANK_API_KEY = credentials('bank-api-key') 
    }

    stages {
        stage('Start Services') {
            steps {
                sh 'docker-compose down --remove-orphans || true'
                sh 'docker-compose up -d --build'
                // Wait for services to be ready
                sh 'sleep 30'
            }
        }

        stage('Test Client') {
            steps {
                dir('simple_dtu_pay_client') {
                    sh 'mvn test'
                }
            }
        }
    }

    post {
        always {
            sh 'docker-compose down --remove-orphans || true'
            cleanWs()
        }
    }
}