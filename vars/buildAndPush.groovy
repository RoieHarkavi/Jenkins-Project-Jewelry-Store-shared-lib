def call(String dockerImage, String imageTag, String nexusCredentialsId) {
    docker.image('roieharkavi/jewelry-agent3:latest')
          .inside('--user root -v /var/run/docker.sock:/var/run/docker.sock') {
        withCredentials([usernamePassword(credentialsId: nexusCredentialsId,
                                         usernameVariable: 'NEXUS_USER',
                                         passwordVariable: 'NEXUS_PASS')]) {
            sh """
                echo "\$NEXUS_PASS" | docker login -u "\$NEXUS_USER" --password-stdin nexus:8082
                docker build -t ${dockerImage}:${imageTag} -t ${dockerImage}:latest .
                docker push ${dockerImage}:${imageTag}
                docker push ${dockerImage}:latest
            """
        }
    }
}