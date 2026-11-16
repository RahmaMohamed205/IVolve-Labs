def call() {
    dir('app') {
        echo "Running Unit Tests..."
        sh 'mvn test'
        echo "Building Application..."
        sh 'mvn package -DskipTests'
    }
}
