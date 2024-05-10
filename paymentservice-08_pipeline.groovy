def paymentservicePipeline(){
  echo "Executing paymentservicePipeline() method..."

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
} //def call
// activities for paymentservice microservices ends.

