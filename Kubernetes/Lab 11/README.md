# Lab 11: Namespace Management and Resource Quota Enforcement

## Objective
Isolate resources in a namespace and limit the number of pods with a ResourceQuota.

## Environment
Two-node Kubernetes cluster from Lab 9.

## Steps

**1. Create the namespace**
```bash
kubectl create namespace ivolve
```

**2. Create the ResourceQuota** (`resourcequota.yaml`)
```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: ivolvequota
  namespace: ivolve
spec:
  hard:
    pods: "3"
```
```bash
kubectl apply -f resourcequota.yaml
kubectl describe resourcequota ivolvequota -n ivolve
```

**3. Test the limit**

The pods carry a toleration for the Lab 10 taint, so they can run on `node01`.
```bash
OVR='{"spec":{"tolerations":[{"key":"workload","operator":"Equal","value":"ivolveApp","effect":"NoSchedule"}]}}'
kubectl run pod1 --image=nginx -n ivolve --overrides="$OVR"
kubectl run pod2 --image=nginx -n ivolve --overrides="$OVR"
kubectl run pod3 --image=nginx -n ivolve --overrides="$OVR"
kubectl run pod4 --image=nginx -n ivolve --overrides="$OVR"
kubectl describe resourcequota ivolvequota -n ivolve
```

## Results
- `pod1` to `pod3` were created.
- `pod4` was rejected: `forbidden: exceeded quota: ivolvequota, requested: pods=1, used: pods=3, limited: pods=3`.
- The quota shows `pods 3 / 3`.

## Notes
- Kubernetes names must be lowercase, so `ivolvequota` was used.
- The quota counts all pods in the namespace, so later deployments in `ivolve` must fit within 3 pods.
