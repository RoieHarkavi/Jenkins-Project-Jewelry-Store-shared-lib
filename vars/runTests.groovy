def call(String dockerImage, String imageTag) {
    sh """
        docker run --rm ${dockerImage}:${imageTag} \
        bash -c "pip3 install -r requirements.txt && python3 -m pytest --junitxml results.xml tests/*.py"
    """
    junit allowEmptyResults: true, testResults: 'results.xml'
}
