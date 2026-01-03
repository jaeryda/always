pipeline {
    agent any
    
    environment {
        // 프로젝트 경로 (Jenkins 작업공간 기준)
        PROJECT_DIR = "${WORKSPACE}/always-svc"
        JAVA_HOME = tool 'JDK-17'  // Jenkins에서 JDK 도구 이름 설정 필요
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '소스 코드 체크아웃 중...'
                // Git 저장소가 설정되어 있으면 자동으로 체크아웃됨
                // 또는 수동으로: checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo 'Maven 빌드 중...'
                dir("${PROJECT_DIR}") {
                    bat '''
                        echo 현재 디렉토리: %CD%
                        call mvnw.cmd clean package -DskipTests
                    '''
                }
            }
        }
        
        stage('Stop Server') {
            steps {
                echo '기존 서버 종료 중...'
                dir("${PROJECT_DIR}") {
                    bat '''
                        powershell -ExecutionPolicy Bypass -File jenkins-stop.ps1
                    '''
                }
            }
        }
        
        stage('Start Server') {
            steps {
                echo '서버 시작 중...'
                dir("${PROJECT_DIR}") {
                    bat '''
                        powershell -ExecutionPolicy Bypass -File jenkins-restart.ps1 -Profile mysql
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo '배포 성공!'
        }
        failure {
            echo '배포 실패!'
        }
    }
}

