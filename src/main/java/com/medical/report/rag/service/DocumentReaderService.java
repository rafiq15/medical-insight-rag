package com.medical.report.rag.service;


import com.medical.report.rag.utils.MultiFileTikaReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class DocumentReaderService {
    private final MultiFileTikaReader multiFileTikaReader;

    public DocumentReaderService(MultiFileTikaReader tikaDocumentReader) {
        this.multiFileTikaReader = tikaDocumentReader;
    }

    public List<Document> readPdfAndCreateChunks() throws IOException {

        return multiFileTikaReader.readAllDocuments();


    }
}
