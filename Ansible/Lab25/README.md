# Lab 25: Automated Server Configuration Using Ansible Playbooks

## Objective
Write one playbook that installs Docker, kubectl and Jenkins on the managed node, then verify the result.

## Environment
Same two nodes as Lab 24: control node `controlplane` and managed node `node01` (Ubuntu 24.04).

## Playbook (`playbook.yml`)
```yaml
---
- name: Configure managed node (Docker, kubectl, Jenkins)
  hosts: managed
  tasks:
    # ---------- Docker ----------
    - name: Install Docker
      ansible.builtin.apt:
        name: docker.io
        state: present
        update_cache: true

    - name: Start and enable Docker
      ansible.builtin.service:
        name: docker
        state: started
        enabled: true

    # ---------- kubectl ----------
    - name: Get the latest stable kubectl version
      ansible.builtin.uri:
        url: https://dl.k8s.io/release/stable.txt
        return_content: true
      register: kubectl_version

    - name: Install kubectl
      ansible.builtin.get_url:
        url: "https://dl.k8s.io/release/{{ kubectl_version.content | trim }}/bin/linux/amd64/kubectl"
        dest: /usr/local/bin/kubectl
        mode: "0755"

    # ---------- Jenkins ----------
    - name: Install Jenkins prerequisites (Java 21)
      ansible.builtin.apt:
        name:
          - ca-certificates
          - curl
          - gnupg
          - fontconfig
          - openjdk-21-jre-headless
        state: present

    - name: Ensure the keyrings directory exists
      ansible.builtin.file:
        path: /etc/apt/keyrings
        state: directory
        mode: "0755"

    - name: Add the Jenkins signing key
      ansible.builtin.get_url:
        url: https://pkg.jenkins.io/debian-stable/jenkins.io-2026.key
        dest: /etc/apt/keyrings/jenkins-keyring.asc
        mode: "0644"
        force: true

    - name: Add the Jenkins repository
      ansible.builtin.apt_repository:
        repo: "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/"
        state: present
        filename: jenkins

    - name: Install Jenkins
      ansible.builtin.apt:
        name: jenkins
        state: present
        update_cache: true

    - name: Start and enable Jenkins
      ansible.builtin.service:
        name: jenkins
        state: started
        enabled: true

    - name: Wait for Jenkins on port 8080
      ansible.builtin.wait_for:
        port: 8080
        timeout: 180
```

## Run
```bash
ansible-playbook playbook.yml --syntax-check
ansible-playbook playbook.yml
```

## Verification on the managed node
```bash
ansible managed -m command -a "docker --version"
ansible managed -m command -a "/usr/local/bin/kubectl version --client"
ansible managed -m shell -a "systemctl is-active docker jenkins"
ansible managed -m shell -a "curl -sI http://localhost:8080 | head -1"
ansible managed -m shell -a "ls -l /var/lib/jenkins/secrets/initialAdminPassword"
```

| Check | Result |
|---|---|
| Docker | `Docker version 29.1.3` |
| kubectl | client `v1.37.2` |
| Docker and Jenkins services | both `active` |
| Jenkins on port 8080 | `HTTP/1.1 403 Forbidden` (Jenkins is running and waiting for the unlock step) |
| `initialAdminPassword` | file exists |

## Idempotence
A second run of the playbook ended with:
```text
ok=12  changed=0  unreachable=0  failed=0
```
Nothing was changed, which shows the playbook is idempotent.

## Problem found during the first run
The first run failed at the Jenkins repository task:
```text
NO_PUBKEY 7198F4B714ABFC68 ... The repository 'https://pkg.jenkins.io/debian-stable binary/ Release' is not signed.
```
**Cause:** the Jenkins project replaced its repository signing key (the old key was `jenkins.io-2023.key`).

**Fix:** download the new key `jenkins.io-2026.key`, and add `force: true` to the `get_url` task. Without it, `get_url` does not replace a key file that already exists, so the old key would have stayed in place.

## Notes
- The course is based on Red Hat Enterprise Linux (RH294), but the nodes run Ubuntu, so `apt` modules are used instead of `dnf`.
- Docker comes from the Ubuntu `docker.io` package.
- kubectl is downloaded as a binary, using the version in `stable.txt`.
- Jenkins needs Java, so Java 21 is installed first.
