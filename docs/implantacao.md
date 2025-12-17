# 🚀 Guia de Implantação

> Instruções detalhadas para deploy do Sistema de Gestão de Notas e Tarefas em diferentes ambientes.

---

## 📋 Índice

* [Pré-requisitos](#pré-requisitos)
* [Arquivos de Configuração](#arquivos-de-configuração)
* [Ambiente de Desenvolvimento](#ambiente-de-desenvolvimento)
* [Ambiente de Homologação](#ambiente-de-homologação)
* [Ambiente de Produção](#ambiente-de-produção)
* [Deploy com Docker](#deploy-com-docker)
* [Deploy em Kubernetes](#deploy-em-kubernetes)
* [Monitoramento](#monitoramento)
* [Backup e Recuperação](#backup-e-recuperação)
* [Troubleshooting](#troubleshooting)

---

## Pré-requisitos

### Software Obrigatório

| Componente | Versão Mínima | Verificação |
|------------|---------------|-------------|
| Java | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| PostgreSQL | 15+ | `psql --version` |
| Docker (opcional) | 20+ | `docker --version` |

### Requisitos de Hardware

| Ambiente | CPU | RAM | Disco |
|----------|-----|-----|-------|
| Desenvolvimento | 2 cores | 4 GB | 20 GB |
| Homologação | 2 cores | 4 GB | 50 GB |
| Produção | 4 cores | 8 GB | 100 GB |

---

## Arquivos de Configuração

### application.properties (Principal)

```properties
# Localização: src/main/resources/application.properties

# Configuração do Banco de Dados
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USER:postgres}
quarkus.datasource.password=${DB_PASSWORD:postgres}
quarkus.datasource.jdbc.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:quarkus-test}

# Hibernate/JPA
quarkus.hibernate-orm.database.generation=validate
quarkus.hibernate-orm.log.sql=false

# Flyway Migrations
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration

# Segurança
quarkus.http.auth.form.enabled=true
quarkus.http.auth.form.login-page=/login
quarkus.http.auth.form.error-page=/login?error=true
quarkus.http.auth.form.landing-page=/users
quarkus.http.auth.session.encryption-key=${SESSION_KEY:sua-chave-secreta-com-pelo-menos-16-caracteres}

# Servidor HTTP
quarkus.http.port=${HTTP_PORT:8080}
quarkus.http.host=0.0.0.0
```

### Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `DB_HOST` | Host do PostgreSQL | `localhost` |
| `DB_PORT` | Porta do PostgreSQL | `5432` |
| `DB_NAME` | Nome do banco | `quarkus-test` |
| `DB_USER` | Usuário do banco | `postgres` |
| `DB_PASSWORD` | Senha do banco | `postgres` |
| `HTTP_PORT` | Porta HTTP | `8080` |
| `SESSION_KEY` | Chave de criptografia de sessão | - |

---

## Ambiente de Desenvolvimento

### 1. Iniciar Banco de Dados

```bash
# Via Docker Compose
docker compose up -d postgres

# Verificar se está rodando
docker compose ps
```

### 2. Executar em Modo Dev

```bash
# Modo desenvolvimento com hot-reload
./mvnw quarkus:dev

# Com debug remoto (porta 5005)
./mvnw quarkus:dev -Ddebug
```

### 3. Acessar Aplicação

* **URL:** http://localhost:8080
* **Login:** `admin@example.com` / `123`
* **Dev UI:** http://localhost:8080/q/dev

---

## Ambiente de Homologação

### 1. Build da Aplicação

```bash
# Gerar JAR
./mvnw clean package -DskipTests

# JAR gerado em:
# target/quarkus-app/quarkus-run.jar
```

### 2. Configurar Banco de Dados

```sql
-- Criar banco e usuário
CREATE DATABASE quarkus_homolog;
CREATE USER homolog_user WITH ENCRYPTED PASSWORD 'sua_senha_forte';
GRANT ALL PRIVILEGES ON DATABASE quarkus_homolog TO homolog_user;
```

### 3. Executar Aplicação

```bash
# Com variáveis de ambiente
DB_HOST=db-homolog.empresa.com \
DB_NAME=quarkus_homolog \
DB_USER=homolog_user \
DB_PASSWORD=sua_senha_forte \
java -jar target/quarkus-app/quarkus-run.jar
```

---

## Ambiente de Produção

### 1. Build Otimizado

```bash
# Build de produção
./mvnw clean package -DskipTests -Dquarkus.package.type=uber-jar

# Ou JAR nativo (requer GraalVM)
./mvnw clean package -Pnative
```

### 2. Configurações de Produção

Criar arquivo `application-prod.properties` :

```properties
# Desabilitar dev services
%prod.quarkus.datasource.devservices.enabled=false

# Pool de conexões
%prod.quarkus.datasource.jdbc.min-size=5
%prod.quarkus.datasource.jdbc.max-size=20

# Logs
%prod.quarkus.log.level=INFO
%prod.quarkus.log.category."org.hibernate".level=WARN

# Compressão HTTP
%prod.quarkus.http.enable-compression=true

# Timeout
%prod.quarkus.http.idle-timeout=30s
```

### 3. Systemd Service (Linux)

Criar arquivo `/etc/systemd/system/quarkus-app.service` :

```ini
[Unit]
Description=Quarkus Test Application
After=network.target postgresql.service

[Service]
Type=simple
User=appuser
Group=appgroup
WorkingDirectory=/opt/quarkus-app
Environment="DB_HOST=localhost"
Environment="DB_NAME=quarkus_prod"
Environment="DB_USER=prod_user"
Environment="DB_PASSWORD=senha_producao"
Environment="SESSION_KEY=chave-super-secreta-32-caracteres"
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar quarkus-run.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# Ativar e iniciar serviço
sudo systemctl daemon-reload
sudo systemctl enable quarkus-app
sudo systemctl start quarkus-app

# Verificar status
sudo systemctl status quarkus-app
sudo journalctl -u quarkus-app -f
```

---

## Deploy com Docker

### 1. Build da Imagem

```bash
# Usando Dockerfile.jvm (recomendado)
docker build -f src/main/docker/Dockerfile.jvm -t quarkus-app:latest .

# Ou imagem nativa (menor e mais rápida)
docker build -f src/main/docker/Dockerfile.native -t quarkus-app:native .
```

### 2. Docker Compose Completo

Criar arquivo `docker-compose.prod.yml` :

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: quarkus-db
    environment:
      POSTGRES_DB: quarkus_prod
      POSTGRES_USER: prod_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - app-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U prod_user -d quarkus_prod"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    image: quarkus-app:latest
    container_name: quarkus-app
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DB_HOST: postgres
      DB_NAME: quarkus_prod
      DB_USER: prod_user
      DB_PASSWORD: ${DB_PASSWORD}
      SESSION_KEY: ${SESSION_KEY}
    ports:
      - "8080:8080"
    networks:
      - app-network
    restart: unless-stopped

volumes:
  postgres_data:

networks:
  app-network:
    driver: bridge
```

### 3. Executar

```bash
# Criar arquivo .env com segredos
echo "DB_PASSWORD=senha_forte_producao" > .env
echo "SESSION_KEY=chave-secreta-32-caracteres" >> .env

# Iniciar stack
docker compose -f docker-compose.prod.yml up -d

# Verificar logs
docker compose -f docker-compose.prod.yml logs -f app
```

---

## Deploy em Kubernetes

### 1. ConfigMap

```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: quarkus-app-config
data:
  DB_HOST: "postgres-service"
  DB_PORT: "5432"
  DB_NAME: "quarkus_prod"
```

### 2. Secret

```yaml
# k8s/secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: quarkus-app-secrets
type: Opaque
stringData:
  DB_USER: prod_user
  DB_PASSWORD: senha_forte_producao
  SESSION_KEY: chave-secreta-32-caracteres
```

### 3. Deployment

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: quarkus-app
  labels:
    app: quarkus-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: quarkus-app
  template:
    metadata:
      labels:
        app: quarkus-app
    spec:
      containers:
        - name: quarkus-app
          image: quarkus-app:latest
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: quarkus-app-config
            - secretRef:
                name: quarkus-app-secrets
          resources:
            requests:
              memory: "512Mi"
              cpu: "500m"
            limits:
              memory: "1Gi"
              cpu: "1000m"
          livenessProbe:
            httpGet:
              path: /q/health/live
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /q/health/ready
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
```

### 4. Service

```yaml
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: quarkus-app-service
spec:
  selector:
    app: quarkus-app
  ports:
    - port: 80
      targetPort: 8080
  type: ClusterIP
```

### 5. Ingress

```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: quarkus-app-ingress
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  tls:
    - hosts:
        - app.empresa.com
      secretName: tls-secret
  rules:
    - host: app.empresa.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: quarkus-app-service
                port:
                  number: 80
```

### 6. Aplicar Configurações

```bash
# Aplicar todos os recursos
kubectl apply -f k8s/

# Verificar status
kubectl get pods -l app=quarkus-app
kubectl logs -f deployment/quarkus-app
```

---

## Monitoramento

### Health Checks

```bash
# Liveness (aplicação está viva?)
curl http://localhost:8080/q/health/live

# Readiness (aplicação está pronta?)
curl http://localhost:8080/q/health/ready

# Todos os checks
curl http://localhost:8080/q/health
```

### Métricas (Prometheus)

Adicionar dependência no `pom.xml` :

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>
```

Endpoint de métricas:

```bash
curl http://localhost:8080/q/metrics
```

### Logs Estruturados

Configurar em `application.properties` :

```properties
# Formato JSON para agregadores de log
%prod.quarkus.log.console.json=true
%prod.quarkus.log.console.json.pretty-print=false
```

---

## Backup e Recuperação

### Backup do Banco de Dados

```bash
# Backup completo
pg_dump -h localhost -U postgres -d quarkus-test -F c -f backup_$(date +%Y%m%d).dump

# Backup apenas estrutura
pg_dump -h localhost -U postgres -d quarkus-test --schema-only -f schema.sql

# Backup apenas dados
pg_dump -h localhost -U postgres -d quarkus-test --data-only -f data.sql
```

### Restauração

```bash
# Restaurar backup completo
pg_restore -h localhost -U postgres -d quarkus-test -c backup_20240101.dump

# Restaurar de SQL
psql -h localhost -U postgres -d quarkus-test < schema.sql
psql -h localhost -U postgres -d quarkus-test < data.sql
```

### Script de Backup Automatizado

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backups/postgres"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="quarkus_prod"

# Criar diretório se não existir
mkdir -p $BACKUP_DIR

# Fazer backup
pg_dump -h localhost -U postgres -d $DB_NAME -F c -f $BACKUP_DIR/backup_$DATE.dump

# Remover backups com mais de 7 dias
find $BACKUP_DIR -name "*.dump" -mtime +7 -delete

echo "Backup realizado: backup_$DATE.dump"
```

Agendar no cron:

```bash
# Backup diário às 3h da manhã
0 3 * * * /opt/scripts/backup.sh >> /var/log/backup.log 2>&1
```

---

## Troubleshooting

### Problemas Comuns

#### 1. Erro de Conexão com Banco

**Sintoma:**

```
Connection refused to host: localhost:5432
```

**Solução:**

```bash
# Verificar se PostgreSQL está rodando
docker compose ps
sudo systemctl status postgresql

# Verificar conectividade
psql -h localhost -U postgres -d quarkus-test

# Verificar variáveis de ambiente
echo $DB_HOST $DB_PORT $DB_NAME
```

#### 2. Migrations Flyway Falham

**Sintoma:**

```
Migration checksum mismatch
```

**Solução:**

```bash
# CUIDADO: Apenas em desenvolvimento!
# Reparar migrations
./mvnw flyway:repair

# Ou limpar e recriar banco
docker compose down -v
docker compose up -d postgres
./mvnw quarkus:dev
```

#### 3. Memória Insuficiente

**Sintoma:**

```
java.lang.OutOfMemoryError: Java heap space
```

**Solução:**

```bash
# Aumentar heap
java -Xms512m -Xmx2048m -jar quarkus-run.jar

# Verificar uso de memória
jcmd <pid> GC.heap_info
```

#### 4. Porta Já em Uso

**Sintoma:**

```
Port 8080 is already in use
```

**Solução:**

```bash
# Encontrar processo
lsof -i :8080
netstat -tulpn | grep 8080

# Matar processo
kill -9 <PID>

# Ou usar outra porta
HTTP_PORT=8081 java -jar quarkus-run.jar
```

#### 5. Sessão Expira Rapidamente

**Sintoma:** Usuário é deslogado frequentemente

**Solução:**

```properties
# Aumentar timeout de sessão
quarkus.http.auth.session.timeout=PT4H
```

### Logs para Debug

```bash
# Aumentar nível de log temporariamente
java -Dquarkus.log.level=DEBUG -jar quarkus-run.jar

# Ou por categoria específica
java -Dquarkus.log.category."com.robsonbs".level=DEBUG -jar quarkus-run.jar
```

### Verificação de Saúde

```bash
# Script de verificação completa
#!/bin/bash

echo "=== Verificação de Saúde ==="

# 1. Aplicação
echo -n "Aplicação: "
curl -s http://localhost:8080/q/health/live | grep -q "UP" && echo "OK" || echo "FALHA"

# 2. Banco de dados
echo -n "Banco: "
curl -s http://localhost:8080/q/health/ready | grep -q "UP" && echo "OK" || echo "FALHA"

# 3. Memória
echo -n "Memória: "
free -h | grep Mem

# 4. Disco
echo -n "Disco: "
df -h / | tail -1

# 5. Conexões
echo -n "Conexões HTTP: "
netstat -an | grep :8080 | grep ESTABLISHED | wc -l
```

---

## Checklist de Deploy

### Pré-Deploy

* [ ] Todos os testes passando (`./mvnw test`)
* [ ] Build sem erros (`./mvnw package`)
* [ ] Migrations revisadas
* [ ] Variáveis de ambiente definidas
* [ ] Backup do banco de dados realizado
* [ ] Plano de rollback documentado

### Deploy

* [ ] Aplicação anterior desligada graciosamente
* [ ] Nova versão implantada
* [ ] Health checks passando
* [ ] Logs sem erros críticos

### Pós-Deploy

* [ ] Funcionalidades críticas testadas
* [ ] Monitoramento ativo
* [ ] Stakeholders notificados
* [ ] Documentação atualizada

---

## Contatos de Suporte

| Área | Responsável | Contato |
|------|-------------|---------|
| Infraestrutura | Time DevOps | devops@empresa.com |
| Banco de Dados | DBA | dba@empresa.com |
| Aplicação | Time Dev | dev@empresa.com |

---

*Última atualização: Janeiro 2025*
