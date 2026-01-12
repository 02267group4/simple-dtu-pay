pipeline {
    agent any

    environment {
        BANK_API_KEY = credentials('bank-api-key') 
    }

    stages {
        stage('Build Service') {
            steps {
                dir('simple_dtu_pay_service') {
                    sh 'chmod +x mvnw'
                    sh './mvnw clean package -DskipTests'
                    sh 'docker build -t simple-dtu-pay-service .'
                }
            }
        }

        stage('Start Service') {
            steps {
                sh 'docker rm -f simple-dtu-pay || true'
                sh 'docker run -d --name simple-dtu-pay -p 8080:8080 simple-dtu-pay-service'
                sleep 5
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
            sh 'docker rm -f simple-dtu-pay || true'
            cleanWs()
        }
    }
}