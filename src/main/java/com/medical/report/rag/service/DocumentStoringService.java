package com.medical.report.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DocumentStoringService {

    @Autowired
    public VectorStore vectorStore;

    public void setVectorStore(List<Document> documents) {
        log.info("Storing {} documents in vector store", documents.size());
        
        // Log metadata distribution
        Map<String, Long> patientCounts = documents.stream()
            .filter(doc -> doc.getMetadata().containsKey("patientId"))
            .collect(java.util.stream.Collectors.groupingBy(
                doc -> doc.getMetadata().get("patientId").toString(), 
                java.util.stream.Collectors.counting()
            ));
            
        patientCounts.forEach((patientId, count) -> 
            log.info("Patient {}: {} documents", patientId, count));
            
        // Store documents
        vectorStore.accept(documents);
        log.info("Successfully stored documents in vector store");
    }

    public List<Document> similaritySearch(String query, int k) {
        return vectorStore.similaritySearch(query);
    }
}
