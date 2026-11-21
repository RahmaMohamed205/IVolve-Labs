# Lab 24: Initial Ansible Configuration and Ad-Hoc Execution

## Objective
Install Ansible on a control node, set up passwordless SSH to a managed node, create an inventory, and run an ad-hoc command to install Docker.

## Environment

| Role | Machine | OS |
|---|---|---|
| Control node | `controlplane` (Killercoda playground) | Ubuntu 24.04 |
| Managed node | `node01` (Killercoda playground) | Ubuntu 24.04 |

Ansible version: core 2.16.3

## Steps

**1. Install Ansible on the control node**
```bash
apt-get update -qq && apt-get install -y ansible
ansible --version | head -2
```

**2. Generate a new SSH key on the control node**
```bash
ssh-keygen -t ed25519 -f ~/.ssh/ansible_key -N "" -C "ansible-control"
```

**3. Copy the public key to the managed node**
```bash
ssh-copy-id -i ~/.ssh/ansible_key.pub root@node01
ssh -i ~/.ssh/ansible_key root@node01 hostname
```
The last command prints `node01` without asking for a password.

**4. Create the inventory and configuration**

`inventory.ini`
```ini
[managed]
node01 ansible_user=root ansible_ssh_private_key_file=~/.ssh/ansible_key
```

`ansible.cfg`
```ini
[defaults]
inventory = ./inventory.ini
host_key_checking = False
interpreter_python = auto_silent
```

```bash
ansible-inventory --graph
ansible managed -m ping
```
The inventory lists `node01` in the `managed` group, and `ping` returns `pong`.

**5. Install Docker with an ad-hoc command**
```bash
ansible managed -m apt -a "name=docker.io state=present update_cache=yes"
ansible managed -m service -a "name=docker state=started enabled=yes"
ansible managed -m command -a "docker --version"
```
Result: `Docker version 29.1.3`.

## Notes
- An ad-hoc command runs a single module on the hosts without a playbook.
- `state=present` makes the `apt` module idempotent: running it again reports no change.
- The course is based on Red Hat Enterprise Linux (RH294), but the lab nodes run Ubuntu, so the `apt` module is used instead of `dnf`.
- The key has no passphrase so Ansible can connect without interaction. This is acceptable for a lab, but production keys should have a passphrase or use an agent.
