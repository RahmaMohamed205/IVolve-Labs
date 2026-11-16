def call(String deploymentPath, String imageName, String imageTag, String namespace) {
    sh "sed -i 's|image: .*|image: ${imageName}:${imageTag}|g' '${deploymentPath}'"
    sh "kubectl apply -f '${deploymentPath}' -n ${namespace}"
}
