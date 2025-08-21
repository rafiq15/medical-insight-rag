package com.medical.report.rag.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Configuration
public class MultiFileTikaReader {
    @Value("${spring.application.document.ingestion.filename}")
    private Resource[] resources;

    /**
     * Reads all documents from files matching the resource pattern.
     *
     * @return List of Document objects containing extracted text and metadata.
     */
    public List<Document> readAllDocuments() {
        List<Document> documents = new ArrayList<>();

        try {
            // Check if resources are found
            if (resources == null || resources.length == 0) {
                log.warn("No resources found for the specified pattern.");
                return documents; // Return empty list if no resources found
            }
            // Process each resource with TikaDocumentReader
            log.info("Found {} resources to process", resources.length);
            for (Resource resource : resources) {
                String patientId = extractPatientIdFromFilename(resource.getFilename()); // Example: JD12345 from report_JD12345.pdf
                log.info("Processing file: {}, extracted patientId: {}", resource.getFilename(), patientId);
                TextSplitter textSplitter = new TokenTextSplitter();

                TikaDocumentReader reader = new TikaDocumentReader(resource); // Create a reader for each file
                List<Document> fileDocuments = reader.read(); // Read the content of the file
                log.info("Processing file: {}, extracted {} documents", resource.getFilename(), fileDocuments.size());
                List<Document> chunks = textSplitter.split(fileDocuments);

                for (Document chunk : chunks) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("patientId", patientId);
                    metadata.put("source", resource.getFilename());
                    chunk.getMetadata().putAll(metadata);
                    log.debug("Added metadata to chunk: {}", chunk.getMetadata());
                }

                // Log the number of chunks with patientId
                long chunksWithPatientId = chunks.stream()
                        .filter(chunk -> chunk.getMetadata().containsKey("patientId"))
                        .count();
                log.info("Created {} chunks with patientId {} for file {}",
                        chunksWithPatientId, patientId, resource.getFilename());

                documents.addAll(chunks);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing documents", e);
        }

        return documents;
    }

    // Helper method to extract patientId from filename (e.g., report_JD12345.pdf -> JD12345)
    private String extractPatientIdFromFilename(String filename) {
        log.info("Extracting patientId from filename: {}", filename);
        return filename.replace("report_", "").replace(".pdf", "");
    }
}