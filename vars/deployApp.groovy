def call(String dockerImage, String imageTag, String nexusCredentialsId, String environment = 'dev') {
        withCredentials([usernamePassword(credentialsId: nexusCredentialsId,
                                         usernameVariable: 'NEXUS_USER',
                                         passwordVariable: 'NEXUS_PASS')]) {
            sh '''
                echo "$NEXUS_PASS" | docker login -u "$NEXUS_USER" --password-stdin nexus:8082
                docker pull ${dockerImage}:${imageTag}
                docker pull ${dockerImage}:latest

                if [ "${environment}" = "dev" ]; then
                    docker-compose -f docker-compose.dev.yml up -d
                elif [ "${environment}" = "staging" ]; then
                    docker-compose -f docker-compose.staging.yml up -d
                fi
            '''
        }
    }
