# Lab 21: Jenkins Pipeline for Application Deployment

## Automation Stages
1. Code Checkout from GitHub repository (`https://github.com/Ibrahim-Adel15/DemoApp.git`).
2. Application Build.
3. Docker Image packaging.
4. Container Vulnerability Scanning with Trivy.
5. Push tagged image to DockerHub.
6. Local runner image cleanup (`docker rmi`).
7. Update image tag in deployment manifest and apply to `ivolve` namespace.

## Verification Screenshots
- `lab21-01-stageview.png`: Pipeline 7-stage execution view.
- `lab21-02-trivy-scan.png`: Security scan log in Console Output.
- `lab21-03-dockerhub.png`: DockerHub repository tags page.
- `lab21-04-k8s-deploy.png`: Output of `kubectl get pods -n ivolve`.