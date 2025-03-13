package org.dksd.tasks;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

import static dev.langchain4j.model.chat.request.ResponseFormat.JSON;

public class ModelCache {

    private ChatLanguageModel model;
    private ChatLanguageModel pojoModel;
    ConstraintExtractor constraintExtractor;

    public ModelCache() {
        model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                //.responseFormat(JSON)
                .modelName("deepseek-r1:latest")
                .build();

        pojoModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .responseFormat(JSON)
                .modelName("mistral:latest")
                .build();
        constraintExtractor = AiServices.create(ConstraintExtractor.class, pojoModel);

    }

    public String chat(String chat) {
        return model.chat(chat);
    }

    public Constr extractConstraintFrom(String string) {
        return constraintExtractor.extractConstraintFrom(string);
    }
}
