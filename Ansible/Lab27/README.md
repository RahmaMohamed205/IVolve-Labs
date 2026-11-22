# Lab 27: Securing Sensitive Data with Ansible Vault

## Objective
Create an Ansible role that installs MySQL, creates a database and a user with full privileges on it, and keep the user's password encrypted with Ansible Vault.

## Environment
Control node `controlplane` and managed node `node01` (Killercoda playground, Ubuntu 24.04), Ansible core 2.16.3.

## Project Structure
```text
Lab27/
├── ansible.cfg
├── inventory.ini
├── site.yml
├── vars/
│   └── vault.yml            # encrypted with Ansible Vault
└── roles/
    └── mysql/
        ├── defaults/main.yml
        └── tasks/main.yml
```

## The MySQL role

`roles/mysql/defaults/main.yml`
```yaml
---
mysql_database: ivolve
mysql_user: ivolveuser
mysql_user_password: "{{ vault_ivolveuser_password }}"
mysql_socket: /run/mysqld/mysqld.sock
```

`roles/mysql/tasks/main.yml`
```yaml
---
- name: Install MySQL server and the Python MySQL library
  ansible.builtin.apt:
    name:
      - mysql-server
      - python3-pymysql
    state: present
    update_cache: true

- name: Start and enable MySQL
  ansible.builtin.service:
    name: mysql
    state: started
    enabled: true

- name: Create the ivolve database
  community.mysql.mysql_db:
    name: "{{ mysql_database }}"
    state: present
    login_unix_socket: "{{ mysql_socket }}"

- name: Create the user with all privileges on the ivolve database
  community.mysql.mysql_user:
    name: "{{ mysql_user }}"
    password: "{{ mysql_user_password }}"
    priv: "{{ mysql_database }}.*:ALL"
    host: localhost
    state: present
    login_unix_socket: "{{ mysql_socket }}"
  no_log: true
```

- The password is not stored in the role. It comes from the variable `vault_ivolveuser_password`, defined in the encrypted file.
- The modules connect to MySQL as `root` through the Unix socket, so no root password is needed.
- `no_log: true` keeps the password out of the task output.

## Ansible Vault

The variable file `vars/vault.yml` holds `vault_ivolveuser_password` and was encrypted with:
```bash
ansible-vault encrypt vars/vault.yml --vault-password-file .vault_pass
```

The committed file starts with the Vault header and an unreadable block:
```text
$ANSIBLE_VAULT;1.1;AES256
<encrypted data>
```

The vault password is stored in `.vault_pass`, which is excluded from Git. Without it, the playbook cannot run:
```text
ERROR! Attempting to decrypt but no vault secrets found
```

## Playbook (`site.yml`)
```yaml
---
- name: Install MySQL and create the ivolve database and user
  hosts: managed
  vars_files:
    - vars/vault.yml
  roles:
    - mysql
  post_tasks:
    - name: Connect as ivolveuser and list the databases
      community.mysql.mysql_query:
        login_user: "{{ mysql_user }}"
        login_password: "{{ mysql_user_password }}"
        login_unix_socket: "{{ mysql_socket }}"
        query: SHOW DATABASES
      register: dbs

    - name: Show the databases
      ansible.builtin.debug:
        msg: "{{ dbs.query_result[0] | map(attribute='Database') | list }}"
```

## Run
```bash
ansible-playbook site.yml --syntax-check --vault-password-file .vault_pass
ansible-playbook site.yml --vault-password-file .vault_pass
```
A second run ended with `changed=0`, so the role is idempotent.

## Validation on the managed node
```bash
ssh -t node01 'mysql -u ivolveuser -p -e "SHOW DATABASES;"'
ssh -t node01 'mysql -u ivolveuser -p -e "SHOW GRANTS FOR CURRENT_USER();"'
```

Databases visible to `ivolveuser`:
```text
information_schema
ivolve
performance_schema
```

Grants:
```text
GRANT USAGE ON *.* TO `ivolveuser`@`localhost`
GRANT ALL PRIVILEGES ON `ivolve`.* TO `ivolveuser`@`localhost`
```

The user can connect with the vault-managed password and has all privileges on `ivolve` only.

## Notes
- The lab text says `ivovle`, which is a typo, so the database is named `ivolve`.
- Vault encrypts the file with AES-256, unlike Base64, which is only an encoding.
- The encrypted `vault.yml` is safe to commit. The vault password file `.vault_pass` is not, so it is listed in `.gitignore`.
- To run without a password file, use `--ask-vault-pass`.
- The nodes run Ubuntu, so the role uses `apt` and the `mysql-server` package instead of the `dnf` packages used in the RHEL-based course.
