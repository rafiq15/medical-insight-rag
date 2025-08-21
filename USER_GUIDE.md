# Medical Reports RAG System: User Guide

## Overview

The Medical Reports RAG System is an AI-powered tool designed to analyze medical reports and provide intelligent insights based on the content. The system uses Retrieval Augmented Generation (RAG) to provide accurate, context-aware responses to your queries about medical reports.

## Getting Started

### Prerequisites

To use the Medical Reports RAG System, you need:
- A web browser or API client
- Medical report PDFs (must follow the naming convention: `report_<PATIENT_ID>.pdf`)
- Access credentials (if required by your instance)

### Accessing the System

The system is available through a REST API endpoint at:
```
http://localhost:8080/api/medical-analysis
```

## Using the System

### How to Submit Queries

You can submit queries to the system by sending a POST request to the API endpoint with a JSON payload containing:
- `query`: Your question about the medical report(s)
- `patientId`: (Optional) The ID of the specific patient whose reports you want to analyze

#### Example Request

```json
{
  "query": "What are the treatment recommendations for high cholesterol?",
  "patientId": "JD12345"
}
```

#### Example cURL Command

```bash
curl -X POST \
  http://localhost:8080/api/medical-analysis \
  -H 'Content-Type: application/json' \
  -d '{
    "query": "What are the treatment recommendations for high cholesterol?",
    "patientId": "JD12345"
  }'
```

### Understanding the Response

The system processes your query and returns a structured response that includes:

1. **Key findings** from the relevant medical reports
2. **Treatment recommendations** or next steps
3. **Step-by-step reasoning** for the recommendations

If the system cannot find relevant information, it will respond with: "Insufficient information in the provided context."

### Query Types

You can ask various types of questions about the medical reports, such as:

- Diagnosis-related: "What is the diagnosis for patient JD12345?"
- Treatment-related: "What treatments were recommended for the patient's condition?"
- Medication-related: "What are the prescribed medications and their dosages?"
- Lab results: "What do the blood test results show about the patient's cholesterol levels?"
- Historical context: "Has the patient's condition improved since the last report?"

### Best Practices for Effective Queries

To get the most accurate responses:

1. **Be specific** with your questions
2. **Include the patient ID** when looking for information about a specific patient
3. **Use medical terminology** when appropriate
4. **Break down complex questions** into simpler ones
5. **Provide context** if asking about specific sections of a report

## Troubleshooting

### Common Issues and Solutions

1. **"No documents found for query"**
   - Check if the patient ID is correct
   - Verify that the patient's reports have been uploaded to the system
   - Try the query without specifying a patient ID

2. **"Insufficient information in the provided context"**
   - The system couldn't find relevant information for your query
   - Try reformulating your question
   - Check if the information you're looking for exists in the uploaded reports

3. **Slow Response Times**
   - Complex queries may take longer to process
   - Try simplifying your query
   - Ensure you're connected to a stable network

## Privacy and Security

- All medical data is processed securely within the system
- Patient information is protected and only accessible with proper authorization
- Data is not shared with external systems or services

## Support

For technical assistance, please contact your system administrator or support team.
