def call(String dockerImage, String imageTag, String nexusCredentialsId, String environment = 'dev') {
    docker.image('roieharkavi/jewelry-agent3:latest')
          .inside('--user root -v /var/run/docker.sock:/var/run/docker.sock') {
        withCredentials([usernamePassword(credentialsId: nexusCredentialsId,
                                         usernameVariable: 'NEXUS_USER',
                                         passwordVariable: 'NEXUS_PASS')]) {
            
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
            
            // פתרון האבטחה - שימוש בenv vars במקום interpolation
            withEnv(["DOCKER_IMAGE=${dockerImage}", "IMAGE_TAG=${imageTag}", "ENVIRONMENT=${environment}"]) {
                sh '''
                    echo "$NEXUS_PASS" | docker login -u "$NEXUS_USER" --password-stdin nexus:8082
                    docker pull "$DOCKER_IMAGE:$IMAGE_TAG"
                    docker pull "$DOCKER_IMAGE:latest"

                    if [ "$ENVIRONMENT" = "dev" ]; then
                        docker-compose -f docker-compose.dev.yml up -d
                    elif [ "$ENVIRONMENT" = "staging" ]; then
                        docker-compose -f docker-compose.staging.yml up -d
                    else
                        docker-compose -f docker-compose.prod.yml up -d
                    fi
                '''
            }
        }
    }
}