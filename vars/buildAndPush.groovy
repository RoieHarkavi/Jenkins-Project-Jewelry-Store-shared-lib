def call(String dockerImage, String imageTag, String nexusCredentialsId, String environment = 'dev') {
    withCredentials([usernamePassword(credentialsId: nexusCredentialsId,
                                     usernameVariable: 'NEXUS_USER',
                                     passwordVariable: 'NEXUS_PASS')]) {
        
        // Install Docker tools if needed
        sh 'apt-get update && apt-get install -y ca-certificates curl gnupg lsb-release'
        
        sh '''
            if ! command -v docker &> /dev/null; then
                echo "Installing Docker CLI..."
                curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
                echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
                apt-get update
                apt-get install -y docker-ce-cli
            fi
            
            if ! command -v docker-compose &> /dev/null; then
                curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
                chmod +x /usr/local/bin/docker-compose
            fi
        '''
        
        // Use environment variables for security
        withEnv(["DOCKER_IMAGE=${dockerImage}", "IMAGE_TAG=${imageTag}", "ENVIRONMENT=${environment}"]) {
            sh '''
                echo "$NEXUS_PASS" | docker login -u "$NEXUS_USER" --password-stdin nexus:8082
                docker build -t "$DOCKER_IMAGE:$IMAGE_TAG" .
                docker tag "$DOCKER_IMAGE:$IMAGE_TAG" "$DOCKER_IMAGE:latest"
                docker push "$DOCKER_IMAGE:$IMAGE_TAG"
                docker push "$DOCKER_IMAGE:latest"
            '''
        }
    }
}