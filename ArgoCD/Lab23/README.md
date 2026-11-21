# Lab 23: GitOps Workflow with Jenkins and Argo CD

## Objective
Deploy an application with Argo CD, keep it in sync with a GitHub repository, and let a Jenkins pipeline trigger new deployments by committing a new image tag to Git.

## Architecture

```text
Jenkins -> builds image -> Docker Hub (rahmaelballat/ivolve-app:build-N)
Jenkins -> commits new tag into deployment.yaml -> GitHub
Argo CD (in the cluster) watches GitHub -> syncs the cluster automatically
```

Jenkins never accesses the cluster. Git is the single source of truth, and Argo CD applies it.

## Environment

| Item | Value |
|---|---|
| Cluster | Killercoda two-node Kubernetes playground (`controlplane`, `node01`) |
| Argo CD | Installed in the `argocd` namespace |
| Jenkins | Docker container (`jenkins/jenkins:lts`), job `DeployOnK8s` |
| Application image | `rahmaelballat/ivolve-app` on Docker Hub |
| Manifests | `ArgoCD/Lab23/manifests` in this repository |

## Project Structure
```text
Lab23/
├── manifests/            # watched by Argo CD
│   ├── namespace.yaml
│   ├── resourcequota.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── storage.yaml
│   ├── statefulset.yaml
│   └── deployment.yaml   # the file Jenkins updates
├── application.yaml      # Argo CD Application
├── Jenkinsfile           # DeployOnK8s pipeline
└── README.md
```

## Step 1: Install Argo CD
```bash
kubectl create namespace argocd
kubectl apply -n argocd --server-side --force-conflicts -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl get pods -n argocd
```

## Step 2: Access the UI and log in
```bash
kubectl patch configmap argocd-cmd-params-cm -n argocd --type merge -p '{"data":{"server.insecure":"true"}}'
kubectl rollout restart deployment argocd-server -n argocd
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo
kubectl port-forward svc/argocd-server -n argocd --address 0.0.0.0 8080:80
```
Logged in as `admin` with the initial password.

## Step 3: Connect the GitHub repository
Settings -> Repositories -> Connect Repo, HTTPS, `https://github.com/RahmaMohamed205/IVolve-Labs.git` (public repository, no credentials). The connection status was **Successful**.

## Step 4: Create the Application with automatic sync
```bash
kubectl apply -f application.yaml
```

| Setting | Value |
|---|---|
| Name | `ivolve` |
| Repository / branch | `IVolve-Labs` / `main` |
| Path | `ArgoCD/Lab23/manifests` |
| Destination | `https://kubernetes.default.svc`, namespace `ivolve` |
| Sync policy | automated, `prune: true`, `selfHeal: true` |

Result: the application is **Synced** and **Healthy**, with MySQL (`ivolvedb-0`) and two `ivolveapp` pods running on `node01`.

## Step 5: Jenkins pipeline (`DeployOnK8s`)
The job is defined in `Jenkinsfile` with four stages:

| Stage | Purpose |
|---|---|
| Checkout | Get the repository |
| Build image | Build `rahmaelballat/ivolve-app:build-N` from `Docker/Lab8/iVolveApp` |
| Push image | Push it to Docker Hub (credential `dockerhub-creds`) |
| Update manifest in Git | Replace the image tag in `deployment.yaml` and push the commit to GitHub (credential `github-creds`) |

The commit message ends with `[skip ci]` so the push does not start another build.

## Step 6: Trigger the pipeline and verify the sync
1. Ran **Build Now** on `DeployOnK8s`. A new image tag was pushed and a new commit changed the image in `deployment.yaml`.
2. Argo CD detected the commit and synced automatically (refresh forced with `kubectl annotate application ivolve -n argocd argocd.argoproj.io/refresh=hard`).

```bash
kubectl get application ivolve -n argocd
kubectl get pods -n ivolve -o wide
kubectl get deployment ivolveapp -n ivolve -o jsonpath='{.spec.template.spec.containers[0].image}'
```

Result: `Synced` / `Healthy`, three pods `Running`, image `rahmaelballat/ivolve-app:build-2`.

## Notes
- The lab text mentions `ivovle` and a Jenkins job `DeployOnK8s` to modify. The Application is named `ivolve` (Kubernetes names must be lowercase), and `DeployOnK8s` did not exist, so it was created for this lab.
- The Deployment uses `maxSurge: 0` and `maxUnavailable: 1`, because the `ivolve` namespace has a quota of 3 pods and a normal rolling update would need a fourth pod.
- Images are tagged with the build number, never `latest`, so every build changes the manifest and Argo CD sees it.
- The Secret holds lab-only passwords in Base64. Base64 is encoding, not encryption, so real secrets should not be stored in Git.
- Argo CD checks Git every 3 minutes by default, and a manual refresh was used to save time.
