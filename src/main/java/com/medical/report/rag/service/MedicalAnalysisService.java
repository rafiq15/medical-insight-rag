package com.medical.report.rag.service;

import com.medical.report.rag.dto.QueryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.util.StringUtils;
import org.springframework.ai.document.Document;

import java.util.List;

@Service
public class MedicalAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(MedicalAnalysisService.class);
    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Autowired
    private VectorStore vectorStore;


    public String analyze(QueryDto queryDto) {
        try {
            String query = queryDto.getQuery();

            logger.info("[MedicalAnalysisService] Received query: {}", query);
            logger.info("[MedicalAnalysisService] Patient ID: {}", queryDto.getPatientId());
            // Build filter expression dynamically
            String filterExpression = buildFilterExpression(queryDto.getPatientId());
            logger.debug("Constructed filter expression: {}", filterExpression);

            // Configure SearchRequest with a lower threshold to improve recall
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(10) // Increase from 5 to 10
                    .similarityThreshold(0.5) // Lower from 0.75 to 0.5 to increase matching chances
                    .filterExpression(filterExpression)
                    .build();

            // Debug: Perform a manual similarity search to inspect retrieved documents
            List<Document> documents = vectorStore.similaritySearch(searchRequest);
            logger.info("Retrieved {} documents for query: {}", documents.size(), query);

            // Try without filter if no documents found
            if (documents.isEmpty() && filterExpression != null) {
                logger.info("No documents found with filter. Trying again without patient filter.");
                SearchRequest unrestrictedRequest = SearchRequest.builder()
                        .query(query)
                        .topK(10)
                        .similarityThreshold(0.5)
                        .build(); // No filter expression
                documents = vectorStore.similaritySearch(unrestrictedRequest);
                logger.info("Retrieved {} documents without filter for query: {}", documents.size(), query);
            }
            for (Document doc : documents) {
                logger.info("Document content (truncated): {}, Metadata: {}",
                        doc.getText().length() > 100 ? doc.getText().substring(0, 100) + "..." : doc.getText(),
                        doc.getMetadata());

                // Additional check for patient ID in metadata
                if (doc.getMetadata().containsKey("patientId")) {
                    logger.info("Found document with patient ID: {}", doc.getMetadata().get("patientId"));
                } else {
                    logger.warn("Document doesn't have patientId metadata: {}", doc.getId());
                }
            }
            // Custom PromptTemplate to ensure clear context presentation
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


            // Configure QuestionAnswerAdvisor with custom PromptTemplate
            QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(searchRequest)
                    .promptTemplate(customPromptTemplate)
                    .build();


            String response = ChatClient.builder(ollamaChatModel)
                    .build()
                    .prompt()
                    .advisors(advisor)
                    .user(u -> u.text("{query}").param("query", query))
                    .call()
                    .content();
            logger.info("[MedicalAnalysisService] RAG response: {}", response);
            return response;

        } catch (Exception e) {
            logger.error("[MedicalAnalysisService] Error retrieving documents: {}", e.getMessage(), e);
            return "An error occurred while processing your request. Please try again later.";
        }
    }

    private String buildFilterExpression(String patientId) {
        StringBuilder filter = new StringBuilder();
        if (StringUtils.hasText(patientId)) {
            // Sanitize patientId to prevent injection (basic escaping)
            String sanitizedPatientId = patientId.replace("'", "''");
            // Ensure we're explicitly using String comparison
            filter.append("patientId == '").append(sanitizedPatientId).append("'");
            logger.debug("Built filter expression for patientId {}: {}", patientId, filter.toString());
        }

        return !filter.isEmpty() ? filter.toString() : null;
    }
}
