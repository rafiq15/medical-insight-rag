# MedInsight RAG: Medical Reports Retrieval-Augmented Generation System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![PGVector](https://img.shields.io/badge/PGVector-0.5.0-blue)](https://github.com/pgvector/pgvector)
[![Ollama](https://img.shields.io/badge/Ollama-latest-orange)](https://ollama.com/)
[![Java](https://img.shields.io/badge/Java-17-red)](https://openjdk.java.net/)
[![Docker](https://img.shields.io/badge/Docker-latest-blue)](https://www.docker.com/)

## Overview

MedInsight RAG is an intelligent medical report analysis system that leverages Retrieval Augmented Generation (RAG) technology to provide AI-powered insights from medical documents. The system ingests medical reports in PDF format, indexes them using vector embeddings, and enables natural language queries to retrieve relevant information and generate insightful analyses.

![MedInsight RAG System Architecture](https://i.imgur.com/0PfMb2Q.png)

## Key Features

- **PDF Medical Report Ingestion**: Automatically extracts text and metadata from medical PDF reports
- **Patient-Specific Filtering**: Target queries to specific patient records using ID filtering
- **Semantic Search**: Find information based on meaning, not just keywords
- **Context-Aware Analysis**: AI responses that incorporate relevant medical context
- **Containerized Deployment**: Easy setup with Docker and Docker Compose

## Documentation

This repository includes comprehensive documentation to help you understand, deploy, and use the MedInsight RAG system:

### 1. [Technical Documentation](./DOCUMENTATION.md)

Detailed explanation of the system architecture, components, and workflows, including:

- **Architecture Overview**: Complete system architecture and component interaction
- **Document Ingestion Process**: How medical PDFs are processed and indexed
- **Vector Search Implementation**: Technical details of semantic search capabilities
- **RAG Processing Flow**: How queries are processed through the retrieval and generation pipeline
- **Containerization**: Docker configuration and service orchestration
- **Performance Optimization**: Techniques for improving system performance
- **Troubleshooting Guide**: Common issues and their solutions

### 2. [User Guide](./USER_GUIDE.md)

Instructions for end-users on how to effectively use the system:

- **Getting Started**: Basic setup and access instructions
- **Query Formulation**: Best practices for writing effective queries
- **API Examples**: Sample requests and responses
- **Understanding Results**: How to interpret the system's outputs
- **Troubleshooting**: Common user issues and solutions

### 3. [Deployment Guide](./DEPLOYMENT.md)

Comprehensive instructions for deploying in various environments:

- **Local Development**: Setup for development and testing
- **Production Deployment**: Step-by-step guide for production environments
- **Cloud Deployment Options**: Configurations for AWS, Azure, and Google Cloud
- **Scaling Strategies**: Horizontal and vertical scaling approaches
- **Monitoring and Maintenance**: Setting up monitoring and maintenance procedures
- **Disaster Recovery**: Backup strategies and recovery procedures

## Quick Start

### Prerequisites

- Docker and Docker Compose
- 8GB+ RAM recommended
- 50GB+ storage space

### Installation

1. Clone the repository:

```bash
git clone https://github.com/rafiq15/medical-insight-rag.git
cd medical-insight-rag
```

2. Add your medical PDFs to the `src/main/resources/reports-pdf/` directory following the naming convention: `report_PATIENTID.pdf`

3. Build the application:

```bash
mvn clean package -DskipTests
```

4. Deploy with Docker Compose:

```bash
docker compose up -d
```

5. Access the API at `http://localhost:8080/api/medical-analysis`

### Basic Usage

Send a POST request to query the system:

```bash
curl -X POST \
  http://localhost:8080/api/medical-analysis \
  -H 'Content-Type: application/json' \
  -d '{
    "query": "What are the treatment recommendations for high cholesterol?",
    "patientId": "JD12345"
  }'
```

## System Components

- **Spring Boot Application**: Core application with REST API and RAG implementation
- **PGVector Database**: Vector database for semantic search (PostgreSQL extension)
- **Ollama LLM Service**: Large Language Model service for text generation
- **Document Processing Pipeline**: PDF extraction and chunking with Apache Tika

## Technology Stack

- **Backend Framework**: Spring Boot with Spring AI
- **Vector Database**: PostgreSQL with PGVector extension
- **Language Models**: Ollama with llama3.2
- **Document Processing**: Apache Tika
- **Containerization**: Docker and Docker Compose
- **PDF Extraction**: Apache Tika
- **Embedding Model**: nomic-embed-text

## Development

### Building from Source

```bash
# Using Maven directly:
mvn clean package -DskipTests

# Or using Maven wrapper:
# For Linux/MacOS:
./mvnw clean package -DskipTests

# For Windows:
mvnw.cmd clean package -DskipTests
```

### Running Tests

```bash
./mvnw test
```

## Contributing

We welcome contributions to MedInsight RAG! Please see our [Contributing Guidelines](CONTRIBUTING.md) for more information.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Spring AI team for their excellent framework
- PGVector contributors for vector search capabilities
- Ollama team for making LLMs accessible

---

**Note**: This system is designed for research and informational purposes. It should not be used as a substitute for professional medical advice, diagnosis, or treatment.
