def paymentservicePipeline(){
   stages {
        stage('paymentservice') {
          echo "Executing paymentservicePipeline() method..."
            steps {
                echo "Step: Executing paymentservicePipeline() method..."'
            }
        }//stage
    } //Stages 
}// def activities for paymentservice microservices ends. paymentservice_pipeline.groovy
