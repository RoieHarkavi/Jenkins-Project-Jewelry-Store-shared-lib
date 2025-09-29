def call(String dockerImage, String imageTag) {
    docker.image('roieharkavi/jewelry-agent3:latest')
          .inside('--user root -v /var/run/docker.sock:/var/run/docker.sock') {

        sh """
            docker run --rm ${dockerImage}:${imageTag} bash -c 'pip3 install -r requirements.txt'
            docker run --rm ${dockerImage}:${imageTag} bash -c 'pylint *.py --rcfile=.pylintrc || true'
        """

        junit allowEmptyResults: true, testResults: 'results.xml'
    }
}
