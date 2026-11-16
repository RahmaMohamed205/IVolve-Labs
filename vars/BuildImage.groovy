def call(String imageName, String imageTag, String credentialsId) {
    dir('app') {
        sh "docker build -t ${imageName}:${imageTag} ."
    }
    sh "trivy image --pkg-types os --scanners vuln --severity HIGH,CRITICAL ${imageName}:${imageTag} || true"
    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'USER', passwordVariable: 'PASS')]) {
        sh "echo \$PASS | docker login -u \$USER --password-stdin"
        sh "docker push ${imageName}:${imageTag}"
    }
    sh "docker rmi ${imageName}:${imageTag}"
}
