package com.medical.report.rag.ingestion;

import com.medical.report.rag.service.DocumentReaderService;
import com.medical.report.rag.service.DocumentStoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MedicalReportIngestion implements CommandLineRunner {
    @Autowired
    DocumentReaderService documentReaderService;

    @Autowired
    DocumentStoringService documentStoringService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Report in PDF Ingestion");
        List<Document> chunks = documentReaderService.readPdfAndCreateChunks();

        log.info("Finished PDF Ingestion");
        documentStoringService.setVectorStore(chunks);
        log.info("Finished Vector Store Ingestion");
    }


}
