package org.dksd.tasks.cache;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.dksd.tasks.Constr;
import org.dksd.tasks.ConstraintExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static dev.langchain4j.model.chat.request.ResponseFormat.JSON;

public class ModelCache {

    private ChatLanguageModel model;
    private ChatLanguageModel pojoModel;
    private ConstraintExtractor constraintExtractor;
    private Map<String, String> inferenceCache = new HashMap<>();
    private Map<String, Constr> contrCache = new HashMap<>();

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
        loadCachesFromDisk();
        // Register a shutdown hook to write caches to disk when the JVM exits.
        Runtime.getRuntime().addShutdownHook(new Thread(this::writeCachesToDisk));
    }

    public String chat(String chat) {
        if (inferenceCache.containsKey(chat)) {
            System.out.println("Cache hit for chat " + chat);
            return inferenceCache.get(chat);
        }
        String ans = model.chat(chat);
        System.out.println("Cache miss for chat " + chat);
        inferenceCache.put(chat, ans);
        return ans;
    }

    public Constr extractConstraintFrom(String string) {
        if (contrCache.containsKey(string)) {
            System.out.println("Cache hit for constr " + string);
            return contrCache.get(string);
        }
        Constr c = constraintExtractor.extractConstraintFrom(string);
        contrCache.put(string, c);
        System.out.println("Cache miss for constr " + string);
        return c;
    }

    private void loadCachesFromDisk() {
        // Load contrCache from disk
        File contrFile = new File("contrCache.ser");
        if (contrFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(contrFile))) {
                contrCache = (Map<String, Constr>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Load inferenceCache from disk
        File inferenceFile = new File("inferenceCache.ser");
        if (inferenceFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inferenceFile))) {
                inferenceCache = (Map<String, String>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeCachesToDisk() {
        // Write inferenceCache to disk using serialization
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("inferenceCache.ser"))) {
            oos.writeObject(inferenceCache);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write contrCache to disk using serialization
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("contrCache.ser"))) {
            oos.writeObject(contrCache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
