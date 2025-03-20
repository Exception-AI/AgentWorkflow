package org.dksd.tasks.cache;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.dksd.tasks.Instance;
import org.dksd.tasks.TaskModelExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(ModelCache.class);
    public static final String TASK_CACHE_SER = "taskCache.ser";
    private ChatLanguageModel model;
    private ChatLanguageModel pojoModel;
    private TaskModelExtractor taskModelExtractor;
    private Map<String, TaskModel> taskCache = new HashMap<>();

    public ModelCache() {
        model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                //.responseFormat(JSON)
                .modelName("deepseek-r1:latest")
                .build();

        pojoModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .responseFormat(JSON)
                .modelName("qwq")
                .build();

        String key = System.getenv("GROQ_API_KEY");
        ChatLanguageModel groqPojoModel = OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey("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")
                .strictJsonSchema(true)
                .modelName("llama-3.3-70b-versatile")
                .build();
        taskModelExtractor = AiServices.create(TaskModelExtractor.class, pojoModel);
        loadCachesFromDisk();
        // Register a shutdown hook to write caches to disk when the JVM exits.
        Runtime.getRuntime().addShutdownHook(new Thread(this::writeCachesToDisk));
    }

    public TaskModel extractTaskModelFrom(String taskStr) {
        if (taskCache.containsKey(taskStr)) {
            logger.debug("Cache hit for task model " + taskStr);
            return taskCache.get(taskStr);
        }
        TaskModel taskModel = null;
        try {
            taskModel = taskModelExtractor.extractTaskModelFrom(taskStr);
        } catch (Exception ep) {
            taskModel = new TaskModel();
            taskModel.shortTaskName = taskStr;
            taskModel.cronSchedule = "30 22 * * 1";
        }
        taskCache.put(taskStr, taskModel);
        logger.debug("Cache miss for task model " + taskStr);
        return taskModel;
    }

    private void loadCachesFromDisk() {
        // Load contrCache from disk
        File contrFile = new File(TASK_CACHE_SER);
        if (contrFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(contrFile))) {
                taskCache = (Map<String, TaskModel>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeCachesToDisk() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TASK_CACHE_SER))) {
            oos.writeObject(taskCache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
