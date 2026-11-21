# Lab 26: Structured Configuration Management with Ansible Roles

## Objective
Convert the Lab 25 playbook into three reusable roles (Docker, kubectl, Jenkins) and run them from a single playbook.

## Project Structure
```text
Lab26/
├── ansible.cfg
├── inventory.ini
├── site.yml
└── roles/
    ├── docker/
    │   └── tasks/main.yml
    ├── kubectl/
    │   └── tasks/main.yml
    └── jenkins/
        ├── defaults/main.yml
        └── tasks/main.yml
```

## Playbook (`site.yml`)
```yaml
---
- name: Apply roles to the managed node
  hosts: managed
  roles:
    - docker
    - kubectl
    - jenkins
```

## Roles

| Role | What it does |
|---|---|
| `docker` | Installs `docker.io` and enables the service |
| `kubectl` | Downloads the latest stable kubectl to `/usr/local/bin/kubectl` |
| `jenkins` | Installs Java, adds the Jenkins repository and key, installs and starts Jenkins |

The `jenkins` role keeps its settings in `defaults/main.yml`, so they can be changed without editing the tasks:
```yaml
---
jenkins_java_package: openjdk-21-jre-headless
jenkins_key_url: https://pkg.jenkins.io/debian-stable/jenkins.io-2026.key
jenkins_repo: "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/"
```

## Run
```bash
ansible managed -m ping
ansible-playbook site.yml --syntax-check
ansible-playbook site.yml --list-tasks
ansible-playbook site.yml
```

## Results

| Check | Result |
|---|---|
| Connection | `ping` returned `pong` from `node01` |
| Syntax check | passed |
| Run | `ok=12  changed=0  unreachable=0  failed=0` |
| Docker | `Docker version 29.1.3` |

The tools were already installed by the Lab 25 playbook, so this run changed nothing. This confirms that the roles work and are idempotent.

## Why roles
- Each tool has its own folder, so it can be reused in other playbooks.
- Variables in `defaults/` can be overridden without touching the tasks.
- The top-level playbook stays short and readable.

## Notes
- The roles use the Jenkins 2026 repository signing key. See the Lab 25 README for the key rotation problem.
- The nodes run Ubuntu, so the roles use `apt` instead of the `dnf` used in the RHEL-based course.
