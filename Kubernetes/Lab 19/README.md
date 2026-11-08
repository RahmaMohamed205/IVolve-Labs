# Lab 19: Securing Kubernetes with RBAC and Service Accounts

## Description
Implementation of Kubernetes Role-Based Access Control (RBAC) and Service Accounts to secure deployment access in the `ivolve` namespace based on the principle of least privilege.

---

## Commands Executed

```bash
# 1. Create Namespace & Service Account
kubectl create namespace ivolve
kubectl create serviceaccount jenkinssa -n ivolve

# 2. Create Token Secret
kubectl apply -f jenkins-sa-token.yaml

# 3. Apply RBAC Role & RoleBinding
kubectl apply -f rbac.yaml

## Verification & Screenshots
# Test 1: Create Deployment (Allowed)
kubectl auth can-i create deployments --as=system:serviceaccount:ivolve:jenkinssa -n ivolve

# Test 2: Update Deployment (Allowed)
kubectl auth can-i update deployments --as=system:serviceaccount:ivolve:jenkinssa -n ivolve

# Test 3: Delete Deployment (Denied)
kubectl auth can-i delete deployments --as=system:serviceaccount:ivolve:jenkinssa -n ivolve

# Test 4: Access outside namespace (Denied)
kubectl auth can-i create deployments --as=system:serviceaccount:ivolve:jenkinssa -n default
