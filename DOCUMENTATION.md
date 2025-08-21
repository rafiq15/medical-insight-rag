# Medical Reports RAG System: Technical Documentation

## System Overview

The Medical Reports RAG System is a Spring Boot application that implements Retrieval Augmented Generation (RAG) for medical report analysis. The system provides AI-assisted analysis of medical reports by leveraging vector search technology and Large Language Models (LLMs).

## Architecture

The system follows a microservices architecture with three main components:

1. **Spring Boot Application**: Handles REST API requests, document ingestion, and RAG implementation
2. **PGVector Database**: Stores document embeddings for efficient semantic search
3. **Ollama LLM Service**: Provides the language model for generating responses

![Architecture Diagram](https://i.imgur.com/0PfMb2Q.png)

## Workflow

### 1. Document Ingestion Process

On application startup, the system automatically ingests medical reports from PDF files:

1. **Initialization**: `MedicalReportIngestion` class implements `CommandLineRunner` to execute during application startup.

2. **Document Reading**: `DocumentReaderService` uses `MultiFileTikaReader` to:
   - Load PDF files from the configured location (`reports-pdf/*.pdf`)
   - Extract patient IDs from filenames (format: `report_<PATIENT_ID>.pdf`)
   - Parse PDF content using Apache Tika
   - Split documents into semantically meaningful chunks

3. **Document Processing**:
   - Each document chunk is enriched with metadata including `patientId` and `source`
   - Text chunks are prepared for vector embedding

4. **Vector Storage**: `DocumentStoringService` stores the document chunks in PGVector:
   - Each chunk is converted to a vector embedding using the configured embedding model (nomic-embed-text)
   - Embeddings and metadata are stored in the PostgreSQL database with PGVector extension
   - Patient distribution statistics are logged

```java
// MedicalReportIngestion.java - Entry point for document ingestion
@Override
public void run(String... args) throws Exception {
    log.info("Starting Report in PDF Ingestion");
    List<Document> chunks = documentReaderService.readPdfAndCreateChunks();

    log.info("Finished PDF Ingestion");
    documentStoringService.setVectorStore(chunks);
    log.info("Finished Vector Store Ingestion");
}
```

### 2. Query Processing Flow

When a user submits a query through the API:

1. **API Request Handling**: `MedicalAnalysisController` receives a POST request at `/api/medical-analysis` with a JSON payload containing:
   - `query`: The user's question about the medical report
   - `patientId`: Optional filter to limit search to a specific patient's documents

```json
{
  "query": "What are the treatment recommendations for high cholesterol?",
  "patientId": "JD12345"
}
```

2. **Query Processing**: `MedicalAnalysisService` processes the query through the following steps:

   a. **Filter Construction**: If a patient ID is provided, a filter expression is built:
      ```java
      // Example: patientId == 'JD12345'
      String filterExpression = buildFilterExpression(queryDto.getPatientId());
      ```

   b. **Vector Search**: The system performs a semantic search in the vector database:
      - Converts the query to a vector embedding
      - Searches for similar document chunks in the vector store
      - Applies filters if specified (e.g., by patient ID)
      - Uses a similarity threshold of 0.5 (configurable)
      - Returns up to 10 most relevant document chunks

   c. **Fallback Mechanism**: If no documents are found with the patient filter, the system automatically retries without the filter to provide broader results.

3. **RAG Processing**:
   - The retrieved document chunks are combined with a customized prompt template
   - The template structures how the context should be used by the LLM

```java
PromptTemplate customPromptTemplate = new PromptTemplate("""
    Based on the following medical report context, analyze the patient's condition.
    Context:
    ---------------------
    {question_answer_context}
    ---------------------
    
    Provide:
    1. Key findings from the report.
    2. Recommendations for treatment or next steps.
    3. Step-by-step reasoning for your recommendations.
    
    If the context is empty or unrelated to the query, respond: "Insufficient information in the provided context."
    
    Query: {query}
    """);
```

4. **LLM Response Generation**:
   - The system uses Ollama LLM service to generate a response
   - The context from retrieved documents and the user query are passed to the LLM
   - The LLM processes the information and generates a structured analysis

5. **Response Delivery**: The generated response is returned to the client as plain text.

## Key Components

### 1. Vector Store Configuration

The system uses PostgreSQL with PGVector extension for efficient vector storage and retrieval:

```yaml
spring:
  ai:
    vectorstore:
      pgvector:
        initialize-schema: true
        remove-existing-vector-store-table: true
        dimensions: 768  # Embedding dimensions for nomic-embed-text model
```

### 2. Document Processing

Document processing is handled by the `MultiFileTikaReader`, which:
- Reads PDF files using Apache Tika
- Extracts patient IDs from filenames
- Splits documents into chunks using a token-based splitter
- Adds metadata to each chunk

### 3. Embedding Generation

The system uses the `nomic-embed-text` model through Ollama to generate embeddings for:
- Document chunks during ingestion
- User queries during retrieval

### 4. RAG Implementation

The RAG process is implemented using Spring AI's `QuestionAnswerAdvisor`, which:
- Manages the vector search process
- Formats the context for the LLM
- Applies the custom prompt template

### 5. LLM Integration

The system integrates with Ollama to provide the LLM functionality:
- Uses llama3.2 model for response generation
- Communicates via REST API
- Configurable through the `application.yml` settings

## Containerization and Deployment

The application is containerized using Docker and orchestrated with Docker Compose:

1. **Services**:
   - `pgvector`: PostgreSQL database with PGVector extension
   - `ollama`: LLM service with pre-loaded models
   - `medical-rag-ai-assistance`: Spring Boot application

2. **Networking**:
   - All services communicate through a dedicated bridge network

3. **Configuration**:
   - Environment variables for database connection and service URLs
   - Volume mapping for Ollama models persistence

## Deployment Process

### Prerequisites

Before deploying the Medical Reports RAG System, ensure you have:

1. **Hardware Requirements**:
   - Minimum 8GB RAM (16GB recommended for production)
   - 4+ CPU cores
   - 50GB+ storage space (depending on PDF volume)

2. **Software Requirements**:
   - Docker Engine v20.10.0+
   - Docker Compose v2.0.0+
   - Git (for cloning the repository)

3. **Network Requirements**:
   - Open port 8080 for API access
   - Internet connection for initial model downloads

### Step 1: Clone the Repository

```bash
git clone https://github.com/your-org/medical-reports-rag-system.git
cd medical-reports-rag-system
```

### Step 2: Configure the Application

1. **Application Configuration**

   Edit the `src/main/resources/application.yml` file to customize:
   
   ```yaml
   spring:
     application:
       document:
         ingestion:
           filename: classpath:reports-pdf/*.pdf  # Modify path if needed
     
     ai:
       ollama:
         chat:
           options:
             model: llama3.2:latest  # Change model if needed
         embedding:
           model: nomic-embed-text   # Change embedding model if needed
   ```

2. **Environment Variables**

   Create a `.env` file in the root directory with any sensitive information:
   
   ```
   POSTGRES_PASSWORD=your_secure_password
   POSTGRES_USER=your_db_user
   ```

3. **PDF Reports Preparation**

   Place your medical PDF reports in the `src/main/resources/reports-pdf/` directory following the naming convention: `report_PATIENTID.pdf`

### Step 3: Build the Application

```bash
# For Linux/MacOS:
./mvnw clean package -DskipTests

# For Windows:
mvnw.cmd clean package -DskipTests
```

This creates a JAR file in the `target/` directory.

### Step 4: Deploy with Docker Compose

1. **Start the Services**

   ```bash
   docker-compose up -d
   ```

   This command:
   - Builds the application image using the Dockerfile
   - Pulls necessary images (pgvector, ollama)
   - Creates and starts all containers
   - Sets up networking between services

2. **Monitor Deployment**

   ```bash
   docker-compose logs -f
   ```

   Observe the logs for:
   - Successful database initialization
   - Ollama model downloads
   - PDF ingestion process completion
   - Application startup success

3. **Verify Deployment**

   ```bash
   # Check if all services are running
   docker-compose ps
   
   # Test the API endpoint
   curl -X POST \
     http://localhost:8080/api/medical-analysis \
     -H 'Content-Type: application/json' \
     -d '{"query": "test", "patientId": ""}'
   ```

### Step 5: Production Considerations

1. **Security Hardening**:
   - Set strong database passwords in production
   - Use a reverse proxy (e.g., Nginx) for HTTPS termination
   - Implement authentication for the API
   - Remove port mappings for internal services from compose file

2. **Performance Tuning**:
   - Adjust JVM memory settings in Dockerfile
   - Configure PostgreSQL for your hardware
   - Adjust token limits based on document sizes
   
   Example Docker memory settings:
   ```
   docker-compose up -d --build medical-rag-ai-assistance -e JAVA_OPTS="-Xms2g -Xmx4g"
   ```

3. **Monitoring Setup**:
   - Implement Prometheus and Grafana for metrics
   - Configure ELK stack for centralized logging
   - Set up health checks and alerts

### Step 6: Backup and Maintenance

1. **Database Backups**:

   ```bash
   # Create a backup script
   docker exec -t pgvector pg_dump -U postgres -d rag_vector_store > backup_$(date +%Y%m%d).sql
   
   # Schedule with cron
   0 2 * * * /path/to/backup-script.sh
   ```

2. **Update Process**:

   ```bash
   # Pull latest changes
   git pull
   
   # Rebuild the application
   ./mvnw clean package -DskipTests
   
   # Update the deployment
   docker-compose down
   docker-compose up -d
   ```

### Step 7: Scaling (Optional)

For higher workloads, consider:

1. **Horizontal Scaling**:
   - Deploy with Kubernetes instead of Docker Compose
   - Use StatefulSet for database
   - Configure multiple replicas for the application

2. **Vertical Scaling**:
   - Increase resources for containers
   - Optimize JVM settings for larger heap

3. **Load Balancing**:
   - Implement Nginx load balancer
   - Configure sticky sessions if needed

### Troubleshooting Deployment Issues

1. **Container Startup Failures**:
   - Check logs: `docker-compose logs <service-name>`
   - Verify environment variables are set correctly
   - Ensure volumes have correct permissions

2. **Database Connection Issues**:
   - Verify network connectivity between containers
   - Check database credentials
   - Ensure PostgreSQL service is running: `docker-compose ps pgvector`

3. **PDF Ingestion Problems**:
   - Verify PDF files are in the correct location and format
   - Check application logs for Tika parsing errors
   - Ensure sufficient disk space for extraction

4. **Ollama Model Issues**:
   - Check if models downloaded successfully
   - Verify Ollama service health
   - Consider pre-downloading models in Dockerfile

## Performance Optimization

The system includes several optimizations:

1. **Similarity Threshold**: Configurable threshold (0.5) to balance precision and recall
2. **Top-K Results**: Returns the top 10 most relevant chunks for a comprehensive context
3. **Fallback Mechanism**: Removes filters if no results are found to increase chances of providing relevant information
4. **Error Handling**: Robust error handling with informative logging and graceful degradation

## Troubleshooting

Common issues and solutions:

1. **No Documents Found**: 
   - Verify patient ID matches the PDF filename format
   - Check if the vector store has been properly populated
   - Try reducing the similarity threshold (current: 0.5)
   - Review logs for ingestion process completion

2. **Poor Response Quality**:
   - Check the retrieved document chunks for relevance
   - Adjust the prompt template for better instruction
   - Ensure the LLM model is appropriate for medical domain tasks

3. **System Performance Issues**:
   - Monitor database connections and query performance
   - Check Ollama service health and response times
   - Review log files for bottlenecks or errors

## Logging and Monitoring

The system uses extensive logging to track:
- Document ingestion process
- Search performance and result counts
- Patient ID filtering
- Document metadata
- RAG processing flow
- LLM response generation

## Future Enhancements

Potential improvements for future versions:

1. **Advanced Chunking Strategies**: Implement medical domain-specific chunking
2. **Query Reformulation**: Add query rewriting for better semantic matching
3. **Multi-modal Support**: Add support for images within medical reports
4. **User Feedback Loop**: Incorporate feedback mechanism to improve results over time
5. **Advanced Filtering**: Support filtering by additional metadata fields
6. **Caching Layer**: Add response caching for improved performance
7. **Hybrid Search**: Combine vector and keyword search for better results
