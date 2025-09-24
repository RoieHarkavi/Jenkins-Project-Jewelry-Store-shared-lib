def call(String dockerImage, String imageTag, String nexusCredentialsId) {
    withCredentials([usernamePassword(credentialsId: nexusCredentialsId,
                                     usernameVariable: 'NEXUS_USER',
                                     passwordVariable: 'NEXUS_PASS')]) {
        sh """
            echo "$NEXUS_PASS" | docker login -u "$NEXUS_USER" --password-stdin nexus:8082
            docker pull ${dockerImage}:${imageTag}
            docker pull ${dockerImage}:latest
            docker-compose -f docker-compose.yml up -d
        """
    }
}
