package com.example.proyectosanmiguel.chatbot;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Component
public class EmbeddingHelper {

    private final EmbeddingModel embeddingModel;

    public EmbeddingHelper(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public List<DocumentEmbedding> vectorizeChunks(List<String> chunks) {
        return chunks.stream()
                .map(chunk -> new DocumentEmbedding(chunk, getEmbedding(chunk)))
                .toList();
    }

    public List<DocumentEmbedding> findRelevantChunks(
            List<DocumentEmbedding> docs, String question, int topK) {

        List<Double> questionEmbedding = getEmbedding(question);

        return docs.stream()
                .sorted(Comparator.comparingDouble(doc ->
                        -cosineSimilarity(doc.embedding(), questionEmbedding)))
                .limit(topK)                .toList();
    }


    private List<Double> getEmbedding(String text) {
        EmbeddingRequest request = new EmbeddingRequest(List.of(text), null);
        EmbeddingResponse response = embeddingModel.call(request);
        float[] embedding = response.getResults().get(0).getOutput();
        
        List<Double> result = new ArrayList<>();
        for (float value : embedding) {
            result.add((double) value);
        }

        return result;
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public List<String> splitIntoChunks(String text, int maxWords) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder chunk = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            chunk.append(words[i]).append(" ");
            if ((i + 1) % maxWords == 0 || i == words.length - 1) {
                chunks.add(chunk.toString().trim());
                chunk.setLength(0);
            }
        }
        return chunks;
    }
}