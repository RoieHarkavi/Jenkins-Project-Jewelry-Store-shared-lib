def call(String dockerImage, String imageTag, String nexusCredentialsId, String environment = 'dev') {
    withCredentials([usernamePassword(credentialsId: nexusCredentialsId,
                                     usernameVariable: 'NEXUS_USER',
                                     passwordVariable: 'NEXUS_PASS')]) {
        
        withEnv(["DOCKER_IMAGE=${dockerImage}", "IMAGE_TAG=${imageTag}", "ENVIRONMENT=${environment}"]) {
            sh '''
                echo "$NEXUS_PASS" | docker login -u "$NEXUS_USER" --password-stdin nexus:8082
                docker build -t "$DOCKER_IMAGE:$IMAGE_TAG" .
                docker push "$DOCKER_IMAGE:$IMAGE_TAG"
            '''
        }
    }
}
