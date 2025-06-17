package com.example.proyectosanmiguel.chatbot;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PdfTxtLoader {

    public String loadPdf(Path path) throws IOException {
        try (PDDocument doc = PDDocument.load(path.toFile())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    public String loadTxt(Path path) throws IOException {
        return Files.readString(path);
    }
}