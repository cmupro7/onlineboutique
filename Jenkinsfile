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
                    steps {
                        dir('onlineboutique') {
                            script {
                                withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                                dir('/var/lib/jenkins/workspace/CMU-POC-CMUPRO7/onlineboutique/src/adservice/') {
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
            }
            
        } // parellel
    }  // stage 01 
    
    
        stage('Trivy Docker Image Scan') {
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

        stage('paymentservice-08_pipeline.groovy') {
                    steps {
                        dir('onlineboutique') {
                        script {
                            echo "Loading paymentservice-08_pipeline.groovy file..."
                            load 'paymentservice-08_pipeline.groovy'
                            echo "Executing paymentservicePipeline() method..."
                            paymentservicePipeline()
                        }
                    }
                }
        }


// More stages here ... 
	
    } // End of stages
} // End of pipeline
   
