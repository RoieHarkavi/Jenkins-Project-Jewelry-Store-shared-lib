def call(String dockerImage, String imageTag, String nexusCredentialsId) {
    docker.image('roieharkavi/jewelry-agent3:latest')
          .inside('--user root -v /var/run/docker.sock:/var/run/docker.sock') {
        withCredentials([usernamePassword(credentialsId: nexusCredentialsId,
                                         usernameVariable: 'NEXUS_USER',
                                         passwordVariable: 'NEXUS_PASS')]) {
            
            // פתרון אבטחה - שימוש בקובץ זמני
            writeFile file: 'docker-login.sh', text: '''#!/bin/bash
echo "$NEXUS_PASS" | docker login -u "$NEXUS_USER" --password-stdin nexus:8082
docker build -t ''' + dockerImage + ''':''' + imageTag + ''' -t ''' + dockerImage + ''':latest .
docker push ''' + dockerImage + ''':''' + imageTag + '''
docker push ''' + dockerImage + ''':latest
'''
            
            sh '''
                # בדיקת docker CLI והתקנה במידת הצורך
                if ! command -v docker &> /dev/null; then
                    echo "Installing Docker CLI..."
                    apt-get update
                    apt-get install -y ca-certificates curl gnupg lsb-release
                    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
                    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
                    apt-get update
                    apt-get install -y docker-ce-cli
                fi
                
                chmod +x docker-login.sh
                ./docker-login.sh
                rm docker-login.sh
            '''
        }
    }
}