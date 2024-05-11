pipeline {
    agent any
    
    environment {
        SCANNER_HOME = tool name: 'sonar-scanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
    }

    stages {
        stage('Cleanup Workspace') {
            steps {
                cleanWs()
            }
        }
        stage('Git Checkout ||') {
            parallel {   
                stage('Git Checkout onlineboutique') {
                    steps {
                        dir('onlineboutique') {
                            git branch: 'main', url: 'https://github.com/cmupro7/onlineboutique.git'
                        }
                    }
                }
        
                stage('Git Checkout cmu-artifacts') {
                    steps {
                        dir('cmu-artifacts') {
                            git branch: 'main', url: 'https://github.com/b4shailen/cmu-artifacts.git'
                        }
                    }
                }

            }
        }
       /* 
        stage('SonarQube Analysis & Trivy FS VUL Scan ||') {
            parallel {
                stage('SonarQube Analysis') {
                    when {
                        changeset "**"
                    }
                    steps {
                        dir('onlineboutique') {
                        script {
                            withSonarQubeEnv('sonar') {
                                sh ''' $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectKey=CMU-CAPSTONE-G5P7 -Dsonar.projectName=CMU-CAPSTONE-G5P7 -Dsonar.java.binaries=. '''
                            }
                        }
                    }
                }
            }

                stage('TRIVY FS VULNERABILITY SCAN') {
                    when {
                        changeset "**"
                    }
                    steps {
                        script {
                            sh "trivy fs --security-checks vuln ${env.WORKSPACE}/onlineboutique > trivy-report.txt"
                        }
                    }
                }
            }
        }
*/
// activities for adservice microservices 

        stage('adservice || 01') {
            parallel {
                stage('OWASP SBOM/ Dependency Check(adservice)') {
                    when {
                        changeset "**/adservice/**"
                    }
                    steps {
                        dir('onlineboutique') {
                            script {
                                script {
                            // Create a directory for the reports with permission 777
                            def reportsDir = "${env.WORKSPACE}/onlineboutique/sbom-dependency-check-reports/adservice-sbom"
                            sh "mkdir -m 777 -p ${reportsDir}"
                            sh "touch adservice-sbom-DUMMY"
                            sh "mv adservice-sbom-* ${reportsDir}/"
                            sh "rm -rf ${reportsDir}/adservice-sbom-DUMMY"
                        
                            // Perform OWASP Dependency-Check scan
                            def currentDateTime = new Date().format('yyyy-MM-dd_HH-mm-ss')
                            def sbomFileName = "adservice-sbom-${currentDateTime}.html"
                            def sbomFilePath = "${env.WORKSPACE}/onlineboutique/${sbomFileName}"
                            dependencyCheck(additionalArguments: "--scan **/src/adservice/requirements.txt -f HTML -o ${sbomFilePath}", odcInstallation: 'DC')
                                }
                            }
                        }
                    }
                }
                
                stage('adservice build') {
                     when {
                        changeset "**/adservice/**"
                    }
                    steps {
                        dir('onlineboutique/src/adservice/') {
                            script {
                                withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                                // dir('/var/lib/jenkins/workspace/CMU-POC-CMUPRO7/onlineboutique/src/adservice/') {
                                    // Build the Docker image
                                    sh "docker build -t cmupro7/adservice:${BUILD_NUMBER} ."
                                    // Push the Docker image to Docker Hub
                                    sh "docker push cmupro7/adservice:${BUILD_NUMBER}"
                                    // Remove the local Docker image
                                    sh "docker rmi cmupro7/adservice:${BUILD_NUMBER}"
                                }
                            }   
                        }
                    }
                }
            
        } // parellel
    }  // stage 01 
    
    
        stage('Trivy Docker Image Scan - adservice') {
             when {
                    changeset "**/adservice/**"
                }
            environment {
                DOCKER_IMAGE_NAME = "cmupro7/adservice:${BUILD_NUMBER}"
                TRIVY_REPORT_PATH = "adservice_${BUILD_NUMBER}_trivy_image_scan_report.html"
            }
            steps {
                dir('onlineboutique') {
                    script {
                        script {
                    // Pull the Docker image locally
                    echo "Pulling Docker image: ${DOCKER_IMAGE_NAME}"
                    sh "docker pull ${env.DOCKER_IMAGE_NAME}"
                    
                    // Run Trivy scan and generate HTML report
                    echo "Running Trivy scan on ${DOCKER_IMAGE_NAME}..."
                    
                    sh "trivy image ${DOCKER_IMAGE_NAME} > ${env.TRIVY_REPORT_PATH}.txt"
                    sh "docker rmi cmupro7/adservice:${BUILD_NUMBER}"
                    }
                }
            }
        }
    } // Stage TDIC
   
   stage('adservice - K8s Manifest Update/CD') {
     when {
            changeset "**/adservice/**"
        }
    environment {
        GIT_REPO_NAME = "cmu-artifacts"
        GIT_USER_NAME = "b4shailen"
    }
    steps {
        dir('cmu-artifacts') {
            script {
                def pattern = "image: cmupro7/adservice:[0-9]{1,5}"
                def replacement = "image: cmupro7/adservice:${BUILD_NUMBER}"
                def yamlFile = "Deploy/manifests/adservice_rollout.yaml"
                
                // Read the contents of the YAML file
                def yamlContent = readFile(yamlFile)
                
                // Replace the pattern with the BUILD_NUMBER
                def updatedContent = yamlContent.replaceAll(pattern, replacement)
                
                // Write the updated content back to the file
                writeFile file: yamlFile, text: updatedContent
                
                // Add, commit, and push the changes to the Git repository
                withCredentials([string(credentialsId: 'github1', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        git config user.email "b4shailen@gmail.com"
                        git config user.name "Shailendra Singh"
                        git add Deploy/manifests/adservice_rollout.yaml
                        git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                        git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }
    }
}


// activities for cartservice microservices 

        stage('cartservice || 02') {
            parallel {
                stage('OWASP SBOM/ Dependency Check(cartservice)') {
                    when {
                        changeset "**/cartservice/**"
                    }
                    steps {
                        dir('onlineboutique') {
                            script {

                            // Create a directory for the reports with permission 777
                            def reportsDir = "${env.WORKSPACE}/onlineboutique/sbom-dependency-check-reports/cartservice-sbom"
                            sh "mkdir -m 777 -p ${reportsDir}"
                            sh "touch cartservice-sbom-DUMMY"
                            sh "mv cartservice-sbom-* ${reportsDir}/"
                            sh "rm -rf ${reportsDir}/cartservice-sbom-DUMMY"
                        
                            // Perform OWASP Dependency-Check scan
                            def currentDateTime = new Date().format('yyyy-MM-dd_HH-mm-ss')
                            def sbomFileName = "cartservice-sbom-${currentDateTime}.html"
                            def sbomFilePath = "${env.WORKSPACE}/onlineboutique/${sbomFileName}"
                            dependencyCheck(additionalArguments: "--scan **/src/cartservice/src/cartservice.csproj -f HTML -o ${sbomFilePath}", odcInstallation: 'DC')
                                }
                            }
                        }
                    }
                
                
                stage('cartservice build') {
                     when {
                        changeset "**/cartservice/**"
                    }
                    steps {
                        dir('onlineboutique/src/cartservice/src/') {
                            script {
                                withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                                // dir('/var/lib/jenkins/workspace/CMU-CAPSTONE-G5P7/onlineboutique/src/cartservice/src') {
                                    // Build the Docker image

                                    sh "docker build -t cmupro7/cartservice:${BUILD_NUMBER} ."
                                    // Push the Docker image to Docker Hub
                                    sh "docker push cmupro7/cartservice:${BUILD_NUMBER}"
                                    // Remove the local Docker image
                                    sh "docker rmi cmupro7/cartservice:${BUILD_NUMBER}"
                                }
                            }   
                        }
                    }
                }
            
        } // parellel
    }  // stage 02 
    
    
        stage('Trivy Docker Image Scan - cartservice') {
             when {
                    changeset "**/cartservice/**"
                }
            environment {
                DOCKER_IMAGE_NAME = "cmupro7/cartservice:${BUILD_NUMBER}"
                TRIVY_REPORT_PATH = "cartservice_${BUILD_NUMBER}_trivy_image_scan_report.html"
            }
            steps {
                dir('onlineboutique') {
                    script {
                        script {
                    // Pull the Docker image locally
                    echo "Pulling Docker image: ${DOCKER_IMAGE_NAME}"
                    sh "docker pull ${env.DOCKER_IMAGE_NAME}"
                    
                    // Run Trivy scan and generate HTML report
                    echo "Running Trivy scan on ${DOCKER_IMAGE_NAME}..."
                    
                    sh "trivy image ${DOCKER_IMAGE_NAME} > ${env.TRIVY_REPORT_PATH}.txt"
                    sh "docker rmi cmupro7/cartservice:${BUILD_NUMBER}"
                    }
                }
            }
        }
    } // Stage TDIC
   
   stage('cartservice - K8s Manifest Update/CD') {
     when {
            changeset "**/cartservice/**"
        }
    environment {
        GIT_REPO_NAME = "cmu-artifacts"
        GIT_USER_NAME = "b4shailen"
    }
    steps {
        dir('cmu-artifacts') {
            script {
                def pattern = "image: cmupro7/cartservice:[0-9]{1,5}"
                def replacement = "image: cmupro7/cartservice:${BUILD_NUMBER}"
                def yamlFile = "Deploy/manifests/cartservice_rollout.yaml"
                
                // Read the contents of the YAML file
                def yamlContent = readFile(yamlFile)
                
                // Replace the pattern with the BUILD_NUMBER
                def updatedContent = yamlContent.replaceAll(pattern, replacement)
                
                // Write the updated content back to the file
                writeFile file: yamlFile, text: updatedContent
                
                // Add, commit, and push the changes to the Git repository
                withCredentials([string(credentialsId: 'github1', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        git config user.email "b4shailen@gmail.com"
                        git config user.name "Shailendra Singh"
                        git add Deploy/manifests/cartservice_rollout.yaml
                        git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                        git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }
    }
}

// activities for cartservice microservices ends. 

// activities for checkoutservice microservices starts

        stage('checkoutservice || 03') {
            parallel {
                stage('OWASP SBOM/ Dependency Check(checkoutservice)') {
                    when {
                        changeset "**/checkoutservice/**"
                    }
                    steps {
                        dir('onlineboutique') {
                            script {
                            // Create a directory for the reports with permission 777
                            def reportsDir = "${env.WORKSPACE}/onlineboutique/sbom-dependency-check-reports/checkoutservice-sbom"
                            sh "mkdir -m 777 -p ${reportsDir}"
                            sh "touch checkoutservice-sbom-DUMMY"
                            sh "mv checkoutservice-sbom-* ${reportsDir}/"
                            sh "rm -rf ${reportsDir}/checkoutservice-sbom-DUMMY"
                        
                            // Perform OWASP Dependency-Check scan
                            def currentDateTime = new Date().format('yyyy-MM-dd_HH-mm-ss')
                            def sbomFileName = "checkoutservice-sbom-${currentDateTime}.html"
                            def sbomFilePath = "${env.WORKSPACE}/onlineboutique/${sbomFileName}"
                            dependencyCheck(additionalArguments: "--scan **/src/checkoutservice/go.mod -f HTML -o ${sbomFilePath}", odcInstallation: 'DC')
			    // src/checkoutservice/go.mod    
                                }
                            }
                        }
                    }
                
                
                stage('checkoutservice build') {
                     when {
                        changeset "**/checkoutservice/**"
                    }
                    steps {
                        dir('onlineboutique/src/checkoutservice/') {
                            script {
                                withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                                // dir('/var/lib/jenkins/workspace/CMU-CAPSTONE-G5P7/onlineboutique/src/checkoutservice/') {
                                    // Build the Docker image

                                    sh "docker build -t cmupro7/checkoutservice:${BUILD_NUMBER} ."
                                    // Push the Docker image to Docker Hub
                                    sh "docker push cmupro7/checkoutservice:${BUILD_NUMBER}"
                                    // Remove the local Docker image
                                    sh "docker rmi cmupro7/checkoutservice:${BUILD_NUMBER}"
                                }
                            }   
                        }
                    }
                }
            
        } // parellel
    }  // stage 03 
    
    
        stage('Trivy Docker Image Scan - checkoutservice') {
             when {
                    changeset "**/checkoutservice/**"
                }
            environment {
                DOCKER_IMAGE_NAME = "cmupro7/checkoutservice:${BUILD_NUMBER}"
                TRIVY_REPORT_PATH = "checkoutservice_${BUILD_NUMBER}_trivy_image_scan_report.html"
            }
            steps {
                dir('onlineboutique') {
                    script {
                        script {
                    // Pull the Docker image locally
                    echo "Pulling Docker image: ${DOCKER_IMAGE_NAME}"
                    sh "docker pull ${env.DOCKER_IMAGE_NAME}"
                    
                    // Run Trivy scan and generate HTML report
                    echo "Running Trivy scan on ${DOCKER_IMAGE_NAME}..."
                    
                    sh "trivy image ${DOCKER_IMAGE_NAME} > ${env.TRIVY_REPORT_PATH}.txt"
                    sh "docker rmi cmupro7/checkoutservice:${BUILD_NUMBER}"
                    }
                }
            }
        }
    } // Stage TDIC
   
   stage('checkoutservice - K8s Manifest Update/CD') {
     when {
            changeset "**/checkoutservice/**"
        }
    environment {
        GIT_REPO_NAME = "cmu-artifacts"
        GIT_USER_NAME = "b4shailen"
    }
    steps {
        dir('cmu-artifacts') {
            script {
                def pattern = "image: cmupro7/checkoutservice:[0-9]{1,5}"
                def replacement = "image: cmupro7/checkoutservice:${BUILD_NUMBER}"
                def yamlFile = "Deploy/manifests/checkoutservice_rollout.yaml"
                
                // Read the contents of the YAML file
                def yamlContent = readFile(yamlFile)
                
                // Replace the pattern with the BUILD_NUMBER
                def updatedContent = yamlContent.replaceAll(pattern, replacement)
                
                // Write the updated content back to the file
                writeFile file: yamlFile, text: updatedContent
                
                // Add, commit, and push the changes to the Git repository
                withCredentials([string(credentialsId: 'github1', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        git config user.email "b4shailen@gmail.com"
                        git config user.name "Shailendra Singh"
                        git add Deploy/manifests/checkoutservice_rollout.yaml
                        git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                        git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }
    }
}

// activities for checkoutservice microservices ends.
// activities for currencyservice microservices starts

        stage('currencyservice || 04') {
            parallel {
                stage('OWASP SBOM/ Dependency Check(currencyservice)') {
                    when {
                        changeset "**/currencyservice/**"
                    }
                    steps {
                        dir('onlineboutique') {
                            script {
                            // Create a directory for the reports with permission 777
                            def reportsDir = "${env.WORKSPACE}/onlineboutique/sbom-dependency-check-reports/currencyservice-sbom"
                            sh "mkdir -m 777 -p ${reportsDir}"
                            sh "touch currencyservice-sbom-DUMMY"
                            sh "mv currencyservice-sbom-* ${reportsDir}/"
                            sh "rm -rf ${reportsDir}/currencyservice-sbom-DUMMY"
                        
                            // Perform OWASP Dependency-Check scan
                            def currentDateTime = new Date().format('yyyy-MM-dd_HH-mm-ss')
                            def sbomFileName = "currencyservice-sbom-${currentDateTime}.html"
                            def sbomFilePath = "${env.WORKSPACE}/onlineboutique/${sbomFileName}"
                            dependencyCheck(additionalArguments: "--scan **//src/currencyservice/package.json -f HTML -o ${sbomFilePath}", odcInstallation: 'DC')
			    // src/currencyservice/package.json    
                                }
                            }
                        }
                    }
                
                
                stage('currencyservice build') {
                     when {
                        changeset "**/currencyservice/**"
                    }
                    steps {
                        dir('onlineboutique/src/currencyservice/') {
                            script {
                                withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                                // dir('/var/lib/jenkins/workspace/CMU-CAPSTONE-G5P7/onlineboutique/src/currencyservice/') {
                                    // Build the Docker image

                                    sh "docker build -t cmupro7/currencyservice:${BUILD_NUMBER} ."
                                    // Push the Docker image to Docker Hub
                                    sh "docker push cmupro7/currencyservice:${BUILD_NUMBER}"
                                    // Remove the local Docker image
                                    sh "docker rmi cmupro7/currencyservice:${BUILD_NUMBER}"
                                }
                            }   
                        }
                    }
                }
            
        } // parellel
    }  // stage 04 
    
    
        stage('Trivy Docker Image Scan - currencyservice') {
             when {
                    changeset "**/currencyservice/**"
                }
            environment {
                DOCKER_IMAGE_NAME = "cmupro7/currencyservice:${BUILD_NUMBER}"
                TRIVY_REPORT_PATH = "currencyservice_${BUILD_NUMBER}_trivy_image_scan_report.html"
            }
            steps {
                dir('onlineboutique') {
                    script {
                        script {
                    // Pull the Docker image locally
                    echo "Pulling Docker image: ${DOCKER_IMAGE_NAME}"
                    sh "docker pull ${env.DOCKER_IMAGE_NAME}"
                    
                    // Run Trivy scan and generate HTML report
                    echo "Running Trivy scan on ${DOCKER_IMAGE_NAME}..."
                    
                    sh "trivy image ${DOCKER_IMAGE_NAME} > ${env.TRIVY_REPORT_PATH}.txt"
                    sh "docker rmi cmupro7/currencyservice:${BUILD_NUMBER}"
                    }
                }
            }
        }
    } // Stage TDIC
   
   stage('currencyservice - K8s Manifest Update/CD') {
     when {
            changeset "**/currencyservice/**"
        }
    environment {
        GIT_REPO_NAME = "cmu-artifacts"
        GIT_USER_NAME = "b4shailen"
    }
    steps {
        dir('cmu-artifacts') {
            script {
                def pattern = "image: cmupro7/currencyservice:[0-9]{1,5}"
                def replacement = "image: cmupro7/currencyservice:${BUILD_NUMBER}"
                def yamlFile = "Deploy/manifests/currencyservice_rollout.yaml"
                
                // Read the contents of the YAML file
                def yamlContent = readFile(yamlFile)
                
                // Replace the pattern with the BUILD_NUMBER
                def updatedContent = yamlContent.replaceAll(pattern, replacement)
                
                // Write the updated content back to the file
                writeFile file: yamlFile, text: updatedContent
                
                // Add, commit, and push the changes to the Git repository
                withCredentials([string(credentialsId: 'github1', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        git config user.email "b4shailen@gmail.com"
                        git config user.name "Shailendra Singh"
                        git add Deploy/manifests/currencyservice_rollout.yaml
                        git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                        git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }
    }
}

// activities for currencyservice microservices ends.

// activities for emailservice microservices starts

        stage('emailservice || 05') {
            parallel {
                stage('OWASP SBOM/ Dependency Check(emailservice)') {
                    when {
                        changeset "**/emailservice/**"
                    }
                    steps {
                        dir('onlineboutique') {
                            script {
                            // Create a directory for the reports with permission 777
                            def reportsDir = "${env.WORKSPACE}/onlineboutique/sbom-dependency-check-reports/emailservice-sbom"
                            sh "mkdir -m 777 -p ${reportsDir}"
                            sh "touch emailservice-sbom-DUMMY"
                            sh "mv emailservice-sbom-* ${reportsDir}/"
                            sh "rm -rf ${reportsDir}/emailservice-sbom-DUMMY"
                        
                            // Perform OWASP Dependency-Check scan
                            def currentDateTime = new Date().format('yyyy-MM-dd_HH-mm-ss')
                            def sbomFileName = "emailservice-sbom-${currentDateTime}.html"
                            def sbomFilePath = "${env.WORKSPACE}/onlineboutique/${sbomFileName}"
                            dependencyCheck(additionalArguments: "--scan **/src/emailservice/requirements.txt -f HTML -o ${sbomFilePath}", odcInstallation: 'DC')
			    // src/emailservice/requirements.txt 
                                }
                            }
                        }
                    }

              
                
                stage('emailservice build') {
                     when {
                        changeset "**/emailservice/**"
                    }
                    steps {
                        dir('onlineboutique/src/emailservice/') {
                            script {
                                withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                                // dir('/var/lib/jenkins/workspace/CMU-CAPSTONE-G5P7/onlineboutique/src/emailservice/') {
                                    // Build the Docker image

                                    sh "docker build -t cmupro7/emailservice:${BUILD_NUMBER} ."
                                    // Push the Docker image to Docker Hub
                                    sh "docker push cmupro7/emailservice:${BUILD_NUMBER}"
                                    // Remove the local Docker image
                                    sh "docker rmi cmupro7/emailservice:${BUILD_NUMBER}"
                                }
                            }   
                        }
                    }
                }
            
        } // parellel
    }  // stage 05
    
    
        stage('Trivy Docker Image Scan - emailservice') {
             when {
                    changeset "**/emailservice/**"
                }
            environment {
                DOCKER_IMAGE_NAME = "cmupro7/emailservice:${BUILD_NUMBER}"
                TRIVY_REPORT_PATH = "emailservice_${BUILD_NUMBER}_trivy_image_scan_report.html"
            }
            steps {
                dir('onlineboutique') {
                    script {
                        script {
                    // Pull the Docker image locally
                    echo "Pulling Docker image: ${DOCKER_IMAGE_NAME}"
                    sh "docker pull ${env.DOCKER_IMAGE_NAME}"
                    
                    // Run Trivy scan and generate HTML report
                    echo "Running Trivy scan on ${DOCKER_IMAGE_NAME}..."
                    
                    sh "trivy image ${DOCKER_IMAGE_NAME} > ${env.TRIVY_REPORT_PATH}.txt"
                    sh "docker rmi cmupro7/emailservice:${BUILD_NUMBER}"
                    }
                }
            }
        }
    } // Stage TDIC
   
   stage('emailservice - K8s Manifest Update/CD') {
     when {
            changeset "**/emailservice/**"
        }
    environment {
        GIT_REPO_NAME = "cmu-artifacts"
        GIT_USER_NAME = "b4shailen"
    }
    steps {
        dir('cmu-artifacts') {
            script {
                def pattern = "image: cmupro7/emailservice:[0-9]{1,5}"
                def replacement = "image: cmupro7/emailservice:${BUILD_NUMBER}"
                def yamlFile = "Deploy/manifests/emailservice_rollout.yaml"
                
                // Read the contents of the YAML file
                def yamlContent = readFile(yamlFile)
                
                // Replace the pattern with the BUILD_NUMBER
                def updatedContent = yamlContent.replaceAll(pattern, replacement)
                
                // Write the updated content back to the file
                writeFile file: yamlFile, text: updatedContent
                
                // Add, commit, and push the changes to the Git repository
                withCredentials([string(credentialsId: 'github1', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        git config user.email "b4shailen@gmail.com"
                        git config user.name "Shailendra Singh"
                        git add Deploy/manifests/emailservice_rollout.yaml
                        git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                        git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }
    }
}

// activities for emailservice microservices ends.

// activities for frontend microservices starts

        stage('frontend || 06') {
            parallel {
                stage('OWASP SBOM/ Dependency Check(frontend)') {
                    when {
                        changeset "**/frontend/**"
                    }
                    steps {
                        dir('onlineboutique') {
                            script {
                            // Create a directory for the reports with permission 777
                            def reportsDir = "${env.WORKSPACE}/onlineboutique/sbom-dependency-check-reports/frontend-sbom"
                            sh "mkdir -m 777 -p ${reportsDir}"
                            sh "touch frontend-sbom-DUMMY"
                            sh "mv frontend-sbom-* ${reportsDir}/"
                            sh "rm -rf ${reportsDir}/frontend-sbom-DUMMY"
                        
                            // Perform OWASP Dependency-Check scan
                            def currentDateTime = new Date().format('yyyy-MM-dd_HH-mm-ss')
                            def sbomFileName = "frontend-sbom-${currentDateTime}.html"
                            def sbomFilePath = "${env.WORKSPACE}/onlineboutique/${sbomFileName}"
                            dependencyCheck(additionalArguments: "--scan **/src/frontend/go.mod -f HTML -o ${sbomFilePath}", odcInstallation: 'DC')
			    // /src/frontend/go.mod 
                                }
                            }
                        }
                    }
                
                
                stage('frontend build') {
                     when {
                        changeset "**/frontend/**"
                    }
                    steps {
                        dir('onlineboutique/src/frontend/') {
                            script {
                                withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                                // dir('/var/lib/jenkins/workspace/CMU-CAPSTONE-G5P7/onlineboutique/src/frontend/') {
                                    // Build the Docker image

                                    sh "docker build -t cmupro7/frontend:${BUILD_NUMBER} ."
                                    // Push the Docker image to Docker Hub
                                    sh "docker push cmupro7/frontend:${BUILD_NUMBER}"
                                    // Remove the local Docker image
                                    sh "docker rmi cmupro7/frontend:${BUILD_NUMBER}"
                                }
                            }   
                        }
                    }
                }
            
        } // parellel
    }  // stage 06
    
    
        stage('Trivy Docker Image Scan - frontend') {
             when {
                    changeset "**/frontend/**"
                }
            environment {
                DOCKER_IMAGE_NAME = "cmupro7/frontend:${BUILD_NUMBER}"
                TRIVY_REPORT_PATH = "frontend_${BUILD_NUMBER}_trivy_image_scan_report.html"
            }
            steps {
                dir('onlineboutique') {
                    script {
                        script {
                    // Pull the Docker image locally
                    echo "Pulling Docker image: ${DOCKER_IMAGE_NAME}"
                    sh "docker pull ${env.DOCKER_IMAGE_NAME}"
                    
                    // Run Trivy scan and generate HTML report
                    echo "Running Trivy scan on ${DOCKER_IMAGE_NAME}..."
                    
                    sh "trivy image ${DOCKER_IMAGE_NAME} > ${env.TRIVY_REPORT_PATH}.txt"
                    sh "docker rmi cmupro7/frontend:${BUILD_NUMBER}"
                    }
                }
            }
        }
    } // Stage TDIC
   
   stage('frontend - K8s Manifest Update/CD') {
     when {
            changeset "**/frontend/**"
        }
    environment {
        GIT_REPO_NAME = "cmu-artifacts"
        GIT_USER_NAME = "b4shailen"
    }
    steps {
        dir('cmu-artifacts') {
            script {
                def pattern = "image: cmupro7/frontend:[0-9]{1,5}"
                def replacement = "image: cmupro7/frontend:${BUILD_NUMBER}"
                def yamlFile = "Deploy/manifests/frontend_rollout.yaml"
                
                // Read the contents of the YAML file
                def yamlContent = readFile(yamlFile)
                
                // Replace the pattern with the BUILD_NUMBER
                def updatedContent = yamlContent.replaceAll(pattern, replacement)
                
                // Write the updated content back to the file
                writeFile file: yamlFile, text: updatedContent
                
                // Add, commit, and push the changes to the Git repository
                withCredentials([string(credentialsId: 'github1', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        git config user.email "b4shailen@gmail.com"
                        git config user.name "Shailendra Singh"
                        git add Deploy/manifests/frontend_rollout.yaml
                        git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                        git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }
    }
}

// activities for frontend microservices ends.

// activities for loadgenerator microservices starts

        stage('loadgenerator || 07') {
            parallel {
                stage('OWASP SBOM/ Dependency Check(loadgenerator)') {
                    when {
                        changeset "**/loadgenerator/**"
                    }
                    steps {
                        dir('onlineboutique') {
                            script {
                            // Create a directory for the reports with permission 777
                            def reportsDir = "${env.WORKSPACE}/onlineboutique/sbom-dependency-check-reports/loadgenerator-sbom"
                            sh "mkdir -m 777 -p ${reportsDir}"
                            sh "touch loadgenerator-sbom-DUMMY"
                            sh "mv loadgenerator-sbom-* ${reportsDir}/"
                            sh "rm -rf ${reportsDir}/loadgenerator-sbom-DUMMY"
                        
                            // Perform OWASP Dependency-Check scan
                            def currentDateTime = new Date().format('yyyy-MM-dd_HH-mm-ss')
                            def sbomFileName = "loadgenerator-sbom-${currentDateTime}.html"
                            def sbomFilePath = "${env.WORKSPACE}/onlineboutique/${sbomFileName}"
                            dependencyCheck(additionalArguments: "--scan **/src/loadgenerator/requirements.txt -f HTML -o ${sbomFilePath}", odcInstallation: 'DC')
			    // src/loadgenerator/requirements.txt
                                }
                            }
                        }
                    }
                
                
                stage('loadgenerator build') {
                     when {
                        changeset "**/loadgenerator/**"
                    }
                    steps {
                        dir('onlineboutique/src/loadgenerator/') {
                            script {
                                withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                                // dir('/var/lib/jenkins/workspace/CMU-CAPSTONE-G5P7/onlineboutique/src/loadgenerator/') {
                                    // Build the Docker image

                                    sh "docker build -t cmupro7/loadgenerator:${BUILD_NUMBER} ."
                                    // Push the Docker image to Docker Hub
                                    sh "docker push cmupro7/loadgenerator:${BUILD_NUMBER}"
                                    // Remove the local Docker image
                                    sh "docker rmi cmupro7/loadgenerator:${BUILD_NUMBER}"
                                }
                            }   
                        }
                    }
                }
            
        } // parellel
    }  // stage 07
    
    
        stage('Trivy Docker Image Scan - loadgenerator') {
             when {
                    changeset "**/loadgenerator/**"
                }
            environment {
                DOCKER_IMAGE_NAME = "cmupro7/loadgenerator:${BUILD_NUMBER}"
                TRIVY_REPORT_PATH = "loadgenerator_${BUILD_NUMBER}_trivy_image_scan_report.html"
            }
            steps {
                dir('onlineboutique') {
                    script {
                        script {
                    // Pull the Docker image locally
                    echo "Pulling Docker image: ${DOCKER_IMAGE_NAME}"
                    sh "docker pull ${env.DOCKER_IMAGE_NAME}"
                    
                    // Run Trivy scan and generate HTML report
                    echo "Running Trivy scan on ${DOCKER_IMAGE_NAME}..."
                    
                    sh "trivy image ${DOCKER_IMAGE_NAME} > ${env.TRIVY_REPORT_PATH}.txt"
                    sh "docker rmi cmupro7/loadgenerator:${BUILD_NUMBER}"
                    }
                }
            }
        }
    } // Stage TDIC
   
   stage('loadgenerator - K8s Manifest Update/CD') {
     when {
            changeset "**/loadgenerator/**"
        }
    environment {
        GIT_REPO_NAME = "cmu-artifacts"
        GIT_USER_NAME = "b4shailen"
    }
    steps {
        dir('cmu-artifacts') {
            script {
                def pattern = "image: cmupro7/loadgenerator:[0-9]{1,5}"
                def replacement = "image: cmupro7/loadgenerator:${BUILD_NUMBER}"
                def yamlFile = "Deploy/manifests/loadgenerator_rollout.yaml"
                
                // Read the contents of the YAML file
                def yamlContent = readFile(yamlFile)
                
                // Replace the pattern with the BUILD_NUMBER
                def updatedContent = yamlContent.replaceAll(pattern, replacement)
                
                // Write the updated content back to the file
                writeFile file: yamlFile, text: updatedContent
                
                // Add, commit, and push the changes to the Git repository
                withCredentials([string(credentialsId: 'github1', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        git config user.email "b4shailen@gmail.com"
                        git config user.name "Shailendra Singh"
                        git add Deploy/manifests/loadgenerator_rollout.yaml
                        git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                        git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }
    }
}

// activities for loadgenerator microservices ends.
// activities for paymentservice microservices starts

        stage('paymentservice || 08') {
            parallel {
                stage('OWASP SBOM/ Dependency Check(paymentservice)') {
                    when {
                        changeset "**/paymentservice/**"
                    }
                    steps {
                        dir('onlineboutique') {
                            script {
                            // Create a directory for the reports with permission 777
                            def reportsDir = "${env.WORKSPACE}/onlineboutique/sbom-dependency-check-reports/paymentservice-sbom"
                            sh "mkdir -m 777 -p ${reportsDir}"
                            sh "touch paymentservice-sbom-DUMMY"
                            sh "mv paymentservice-sbom-* ${reportsDir}/"
                            sh "rm -rf ${reportsDir}/paymentservice-sbom-DUMMY"
                        
                            // Perform OWASP Dependency-Check scan
                            def currentDateTime = new Date().format('yyyy-MM-dd_HH-mm-ss')
                            def sbomFileName = "paymentservice-sbom-${currentDateTime}.html"
                            def sbomFilePath = "${env.WORKSPACE}/onlineboutique/${sbomFileName}"
                            dependencyCheck(additionalArguments: "--scan **/src/paymentservice/package.json -f HTML -o ${sbomFilePath}", odcInstallation: 'DC')
			    // src/paymentservice/package.json
                                }
                            }
                        }
                    }
                
                
                stage('paymentservice build') {
                     when {
                        changeset "**/paymentservice/**"
                    }
                    steps {
                        dir('onlineboutique/src/paymentservice/') {
                            script {
                                withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                                // dir('/var/lib/jenkins/workspace/CMU-CAPSTONE-G5P7/onlineboutique/src/paymentservice/') {
                                    // Build the Docker image

                                    sh "docker build -t cmupro7/paymentservice:${BUILD_NUMBER} ."
                                    // Push the Docker image to Docker Hub
                                    sh "docker push cmupro7/paymentservice:${BUILD_NUMBER}"
                                    // Remove the local Docker image
                                    sh "docker rmi cmupro7/paymentservice:${BUILD_NUMBER}"
                                }
                            }   
                        }
                    }
                }
            
        } // parellel
    }  // stage 08
    
    
        stage('Trivy Docker Image Scan - paymentservice') {
             when {
                    changeset "**/paymentservice/**"
                }
            environment {
                DOCKER_IMAGE_NAME = "cmupro7/paymentservice:${BUILD_NUMBER}"
                TRIVY_REPORT_PATH = "paymentservice_${BUILD_NUMBER}_trivy_image_scan_report.html"
            }
            steps {
                dir('onlineboutique') {
                    script {
                        script {
                    // Pull the Docker image locally
                    echo "Pulling Docker image: ${DOCKER_IMAGE_NAME}"
                    sh "docker pull ${env.DOCKER_IMAGE_NAME}"
                    
                    // Run Trivy scan and generate HTML report
                    echo "Running Trivy scan on ${DOCKER_IMAGE_NAME}..."
                    
                    sh "trivy image ${DOCKER_IMAGE_NAME} > ${env.TRIVY_REPORT_PATH}.txt"
                    sh "docker rmi cmupro7/paymentservice:${BUILD_NUMBER}"
                    }
                }
            }
        }
    } // Stage TDIC
   
   stage('paymentservice - K8s Manifest Update/CD') {
     when {
            changeset "**/paymentservice/**"
        }
    environment {
        GIT_REPO_NAME = "cmu-artifacts"
        GIT_USER_NAME = "b4shailen"
    }
    steps {
        dir('cmu-artifacts') {
            script {
                def pattern = "image: cmupro7/paymentservice:[0-9]{1,5}"
                def replacement = "image: cmupro7/paymentservice:${BUILD_NUMBER}"
                def yamlFile = "Deploy/manifests/paymentservice_rollout.yaml"
                
                // Read the contents of the YAML file
                def yamlContent = readFile(yamlFile)
                
                // Replace the pattern with the BUILD_NUMBER
                def updatedContent = yamlContent.replaceAll(pattern, replacement)
                
                // Write the updated content back to the file
                writeFile file: yamlFile, text: updatedContent
                
                // Add, commit, and push the changes to the Git repository
                withCredentials([string(credentialsId: 'github1', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        git config user.email "b4shailen@gmail.com"
                        git config user.name "Shailendra Singh"
                        git add Deploy/manifests/paymentservice_rollout.yaml
                        git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                        git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }
    }
}

// activities for paymentservice microservices ends.





// More stages here ... 
	
    } // End of stages
} // End of pipeline
