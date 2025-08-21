# Medical Reports RAG System: Deployment Guide

This document provides detailed instructions for deploying the Medical Reports RAG System in different environments.

## Table of Contents

1. [Local Development Deployment](#local-development-deployment)
2. [Production Deployment](#production-deployment)
3. [Cloud Deployment Options](#cloud-deployment-options)
4. [Scaling Strategies](#scaling-strategies)
5. [Monitoring and Maintenance](#monitoring-and-maintenance)

## Local Development Deployment

### Prerequisites

- JDK 17+
- Maven 3.6+
- Docker and Docker Compose
- Git

### Step 1: Clone the Repository

```bash
git clone https://github.com/your-org/medical-reports-rag-system.git
cd medical-reports-rag-system
```

### Step 2: Start Development Services

```bash
# Start only the supporting services (database and Ollama)
docker-compose up -d pgvector ollama
```

### Step 3: Run the Application Locally

```bash
# For Linux/MacOS:
./mvnw spring-boot:run

# For Windows:
mvnw.cmd spring-boot:run
```

This will start the Spring Boot application on port 8080 and connect to the Docker-managed services.

### Step 4: Verify Local Deployment

Access the API at `http://localhost:8080/api/medical-analysis`

## Production Deployment

### Standard Docker Compose Deployment

Follow these steps for a standard production deployment using Docker Compose:

### Step 1: Prepare the Server

```bash
# Update system packages
sudo apt update && sudo apt upgrade -y

# Install Docker and Docker Compose
sudo apt install docker.io docker-compose -y

# Add current user to docker group
sudo usermod -aG docker ${USER}
```

Log out and log back in for group changes to take effect.

### Step 2: Clone and Configure

```bash
git clone https://github.com/your-org/medical-reports-rag-system.git
cd medical-reports-rag-system

# Create production .env file
cat > .env << EOF
POSTGRES_PASSWORD=secure_production_password
POSTGRES_USER=postgres
SPRING_PROFILES_ACTIVE=prod
EOF
```

### Step 3: Configure Production Settings

Edit `src/main/resources/application-prod.yml` to set:
- Production logging levels
- Performance tuning parameters
- Security settings

### Step 4: Deploy with Docker Compose

```bash
# Build and start all services
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

### Step 5: Configure Reverse Proxy (Nginx)

```bash
sudo apt install nginx -y

# Create Nginx configuration
sudo nano /etc/nginx/sites-available/medical-rag

# Add the following configuration
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Enable site and restart Nginx
sudo ln -s /etc/nginx/sites-available/medical-rag /etc/nginx/sites-enabled/
sudo systemctl restart nginx
```

### Step 6: Set Up SSL with Let's Encrypt

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

## Cloud Deployment Options

### AWS Deployment

#### Option 1: EC2 with Docker Compose

1. Launch an EC2 instance with sufficient resources (t3.large or better recommended)
2. Install Docker and Docker Compose
3. Follow the standard production deployment steps

#### Option 2: AWS ECS

1. Create a new ECS cluster
2. Define task definitions for each service
3. Set up an Application Load Balancer
4. Create ECS services using the task definitions
5. Configure networking and security groups

Example ECS task definition:

```json
{
  "family": "medical-rag-app",
  "executionRoleArn": "arn:aws:iam::ACCOUNT_ID:role/ecsTaskExecutionRole",
  "networkMode": "awsvpc",
  "containerDefinitions": [
    {
      "name": "medical-rag-app",
      "image": "ACCOUNT_ID.dkr.ecr.REGION.amazonaws.com/medical-rag:latest",
      "essential": true,
      "portMappings": [
        {
          "containerPort": 8080,
          "hostPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_DATASOURCE_URL",
          "value": "jdbc:postgresql://your-rds-instance:5432/rag_vector_store"
        },
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/medical-rag",
          "awslogs-region": "REGION",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ],
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048"
}
```

### Azure Deployment

#### Azure Container Apps

1. Create an Azure Container Registry
2. Push Docker images to ACR
3. Create an Azure Container App
4. Configure scaling, networking, and environment variables

Example Azure CLI commands:

```bash
# Create Azure Container Registry
az acr create --resource-group myResourceGroup --name myContainerRegistry --sku Basic

# Build and push image
az acr build --registry myContainerRegistry --image medical-rag:latest .

# Create Container App
az containerapp create \
  --name medical-rag \
  --resource-group myResourceGroup \
  --environment myContainerAppEnvironment \
  --image myContainerRegistry.azurecr.io/medical-rag:latest \
  --target-port 8080 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 5 \
  --env-vars "SPRING_PROFILES_ACTIVE=prod" "SPRING_DATASOURCE_URL=jdbc:postgresql://your-azure-db:5432/rag_vector_store"
```

### Google Cloud Platform

#### Google Kubernetes Engine (GKE)

1. Create a GKE cluster
2. Configure kubectl
3. Apply Kubernetes manifests

Example Kubernetes manifests:

`deployment.yaml`:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: medical-rag
spec:
  replicas: 2
  selector:
    matchLabels:
      app: medical-rag
  template:
    metadata:
      labels:
        app: medical-rag
    spec:
      containers:
      - name: medical-rag
        image: gcr.io/PROJECT_ID/medical-rag:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://your-cloud-sql:5432/rag_vector_store
        - name: SPRING_PROFILES_ACTIVE
          value: prod
```

`service.yaml`:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: medical-rag
spec:
  selector:
    app: medical-rag
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

## Scaling Strategies

### Vertical Scaling

Increase resources for individual containers:

```yaml
# In docker-compose.yml
services:
  medical-rag-ai-assistance:
    deploy:
      resources:
        limits:
          cpus: '4'
          memory: 8G
```

### Horizontal Scaling

For Kubernetes-based deployments:

```bash
# Scale to 5 replicas
kubectl scale deployment medical-rag --replicas=5
```

For Docker Swarm:

```bash
# Deploy as stack with replicas
docker stack deploy -c docker-compose.yml -c docker-compose.prod.yml medical-rag
docker service scale medical-rag_medical-rag-ai-assistance=5
```

### Database Scaling

1. **Read Replicas**: Configure PostgreSQL read replicas for query-heavy workloads
2. **Connection Pooling**: Implement PgBouncer for connection management
3. **Sharding**: For very large datasets, consider implementing database sharding

## Monitoring and Maintenance

### Setting Up Monitoring

1. **Prometheus & Grafana**:

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['medical-rag-ai-assistance:8080']
```

2. **ELK Stack**:

```yaml
# logstash.conf
input {
  file {
    path => "/logs/app.log"
    start_position => "beginning"
  }
}

filter {
  grok {
    match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:log_level} %{GREEDYDATA:log_message}" }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "medical-rag-%{+YYYY.MM.dd}"
  }
}
```

### Automated Backups

Create a backup script:

```bash
#!/bin/bash
# backup-db.sh
DATE=$(date +%Y%m%d-%H%M%S)
BACKUP_DIR="/backups"

# Create backup
docker exec -t pgvector pg_dump -U postgres -d rag_vector_store > $BACKUP_DIR/backup-$DATE.sql

# Compress backup
gzip $BACKUP_DIR/backup-$DATE.sql

# Delete backups older than 30 days
find $BACKUP_DIR -name "backup-*.sql.gz" -type f -mtime +30 -delete
```

Add to crontab:

```
0 2 * * * /path/to/backup-db.sh
```

### System Updates

Create an update script:

```bash
#!/bin/bash
# update.sh
set -e

# Pull latest changes
git pull

# Build new application
./mvnw clean package -DskipTests

# Update containers
docker-compose down
docker-compose up -d

# Verify health
curl -f http://localhost:8080/actuator/health || echo "Health check failed"
```

## Disaster Recovery

1. **Backup Strategy**:
   - Daily database backups
   - Configuration files version control
   - Docker image versioning

2. **Recovery Procedure**:
   - Provision new infrastructure
   - Restore database from backup
   - Deploy application with configuration
   - Verify system integrity

Example recovery:

```bash
# Restore database
cat backup.sql | docker exec -i pgvector psql -U postgres -d rag_vector_store

# Deploy application
docker-compose up -d
```

3. **Testing Recovery**:
   - Schedule regular recovery drills
   - Validate backup integrity monthly
   - Document and refine recovery procedures
