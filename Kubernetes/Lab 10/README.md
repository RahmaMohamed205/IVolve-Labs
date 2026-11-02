# Lab 10: Node Isolation Using Taints and Labels

## Objective
Dedicate a worker node using a taint and a label.

## Environment
Two-node Kubernetes cluster from Lab 9 (`controlplane`, `node01`).

## Steps

**1. Taint the worker node**
```bash
kubectl taint nodes node01 workload=ivolveApp:NoSchedule
```
Pods without a matching toleration are not scheduled on `node01`.

**2. Label the same node**
```bash
kubectl label nodes node01 project=ivolve
```

## Verification
```bash
kubectl describe node node01 | grep -E "^Taints|project=ivolve"
kubectl get nodes --show-labels
```

| Check | Result |
|---|---|
| Taint | `workload=ivolveApp:NoSchedule` |
| Label | `project=ivolve` |

## Notes
- A taint repels pods. A label identifies the node, so pods can select it with a `nodeSelector`.
- To run a pod on this node it needs the toleration for `workload=ivolveApp` and, to target it, `nodeSelector: project: ivolve`.
