package org.dksd.tasks;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import org.dksd.tasks.model.Assistant;
import org.dksd.tasks.model.Constraint;
import org.dksd.tasks.model.Instance;
import org.dksd.tasks.model.SimpleTask;
import org.dksd.tasks.model.Task;
import org.dksd.tasks.processor.TaskLLMProcessor;
import org.dksd.tasks.scheduling.ScheduledTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.DayOfWeek;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

public class Main {

    private static Random rand = new Random();
    public static void updateConstraintAmount(Collection coll, String line, String prefix, BiConsumer<Constraint, Integer> updater) {
        int amount = Integer.parseInt(line.substring(prefix.length()).trim());
        Constraint c = coll.getInstance().getConstraints(coll.getCurrentTask()).getFirst();
        updater.accept(c, amount);
    }

    public static void updateConstraintString(Collection coll, String line, String prefix, BiConsumer<Constraint, String> updater,
                                              BiConsumer<Constraint, String> remover) {
        String value = line.substring(prefix.length()).trim();
        Constraint c = coll.getInstance().getConstraints(coll.getCurrentTask()).getFirst();
        char sign = value.charAt(0);
        value = value.substring(1).trim();
        if (sign == '+') {
            updater.accept(c, value);
        }
        remover.accept(c, value);
    }

    public static void updateConstraintNext(Collection coll, Consumer<Constraint> updater) {
        Constraint c = coll.getInstance().getConstraints(coll.getCurrentTask()).getFirst();
        updater.accept(c);
    }

    private static ContentRetriever createContentRetriever(List<Document> documents) {

        // Here, we create an empty in-memory store for our documents and their embeddings.
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // Here, we are ingesting our documents into the store.
        // Under the hood, a lot of "magic" is happening, but we can ignore it for now.
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);

        // Lastly, let's create a content retriever from an embedding store.
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        //Easy RAG
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("/Users/dylan/Documents/StaffEng");
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);

        //Collection coll = new Collection(getAllInstances());
        Collection coll = new Collection(new Instance("Friday"));
        TaskLLMProcessor taskLLMProcessor = new TaskLLMProcessor(coll);
        taskLLMProcessor.processSimpleTasks(parseTasks(Files.readAllLines(coll.getInstance().getTodoFilePath())));
        List<ScheduledTask> scheduledTasks = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = null;

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(taskLLMProcessor.getChatModel())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // it should remember 10 latest messages
                .contentRetriever(createContentRetriever(documents)) // it should have access to our documents
                .build();
        String answer = assistant.chat("How to do Easy RAG with LangChain4j?");
        System.out.println(answer);

        while (!"q".equals(line)) {
            try {
                for (Task task : coll.getInstance().getTasks()) {
                    for (Constraint constraint : coll.getInstance().getConstraints(task)) {
                        for (DayOfWeek dayOfWeek : constraint.getDaysOfWeek()) {
                            ScheduledTask newTask = new ScheduledTask(task, coll.getInstance().getTask(task.getId()).getName(), dayOfWeek, constraint);
                            scheduledTasks.add(newTask);
                        }
                    }
                }
                coll.displayTasks(coll.getInstance().getTasks());
                System.out.print("Enter choice: ");
                line = reader.readLine();

                checkForChanges(line, "cleadtime", coll, (c, amount) -> c.setAllowedSecondsBeforeDeadline(c.getAllowedSecondsBeforeDeadline() + amount * 60));
                checkForChanges(line, "cduration", coll, (c, amount) -> c.setDurationSeconds(c.getDurationSeconds() + amount * 60));
                checkForChanges(line, "cdeadline", coll, (c, amount) -> c.setDeadlineTime(c.getDeadlineTime().plusMinutes(amount)));
                checkForChangesString(line, "cday", coll, (c, day) -> c.getDaysOfWeek().add(DayOfWeek.valueOf(day)),
                        (c, day) -> c.getDaysOfWeek().remove(DayOfWeek.valueOf(day)));
                checkForToggle(line, "teffort", coll, (c) -> c.setEffort(c.getEffort().next(c.getEffort())));
                checkForToggle(line, "tcost", coll, (c) -> c.setCost(c.getCost().next(c.getCost())));
                checkForToggle(line, "timport", coll, (c) -> c.setImportance(c.getImportance().next(c.getImportance())));
                checkForToggle(line, "tconcen", coll, (c) -> c.setConcentration(c.getConcentration().next(c.getConcentration())));
                checkForToggle(line, "tdeadlinetype", coll, (c) -> c.setDeadlineType(c.getDeadlineType().next(c.getDeadlineType())));

                switch (line) {
                    case "/": // Search
                        System.out.print("Find: ");
                        coll.find(reader.readLine());
                        break;
                    case "p":
                        coll.setPrevTask();
                        break;
                    case "n":
                        coll.setNextTask();
                        break;
                    case "": // Enter key
                        //selectTask();
                        System.out.println("Enter pressed");
                        break;
                    //case "csauto":
                    //    taskLLMProcessor.createAutoSubTaskFromParent(coll.getCurrentTask());
                    //    break;
                    case "cs":
                        multiInput(reader, (name, desc) -> coll.getInstance().createSubTask(coll.getCurrentTask(), name, desc));
                        break;
                    case "cd":
                        multiInput(reader, (name, desc) -> coll.getInstance().createDepTask(coll.getCurrentTask(), name, desc));
                        break;
                    case "e":
                        multiInput(reader, (name, desc) -> coll.getCurrentTask().updateTask(name, desc));
                        break;
                    case "d":
                        //deleteTask(coll.getCurrentTask());
                        break;
                    case "q": // Quit
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        coll.getInstance().write(coll);

        TreeMap<DayOfWeek, TreeMap<LocalTime, ScheduledTask>> newTasks = new TreeMap<>();
        for (DayOfWeek value : DayOfWeek.values()) {
            newTasks.put(value, new TreeMap<>());
        }

        List<ScheduledTask> pool = new ArrayList<>(scheduledTasks);
        while (!pool.isEmpty()) {
            int randSTask = (int) (rand.nextDouble() * pool.size());
            ScheduledTask scheduledTask = pool.get(randSTask);
            pool.remove(scheduledTask);
            newTasks.get(scheduledTask.getEndDay()).put(scheduledTask.getConstraint().getDeadlineTime(), scheduledTask);
        }
        //We could just stop here now.
        for (Map.Entry<DayOfWeek, TreeMap<LocalTime, ScheduledTask>> entry : newTasks.entrySet()) {
            for (ScheduledTask scheduledTask : entry.getValue().values()) {
                System.out.println(scheduledTask);
            }
        }
        // 7am to 8:15am get ready for school (block 1)
        // 8:15am to 10:15am work   2
        // 10:15 to 10:45 walk/break/gardening
        // 11:00 to 12:00 work   3
        // 12:00 to 12:15 lunch
        // 12:15 to 2pm work     4
        // 2:15 pm to 3:125 driving
        // 8pm kids duty and other light non focus work 5
        // 8pm to 11pm work focus time 6
        // How do I define periods that are blocked out or have special rules...
        // hmm need a method to penalize based on rules...
        /*double error = 0;
        for (Map.Entry<DayOfWeek, TreeMap<LocalTime, ScheduledTask>> entry : newTasks.entrySet()) {
            LocalTime time = LocalTime.of(8, 15);
            for (ScheduledTask scheduledTask : entry.getValue().values()) {
                LocalTime deadline = scheduledTask.getConstraint().getDeadlineTime();
                if (time.plusSeconds(scheduledTask.getConstraint().getDurationSeconds()).isAfter(deadline)) {
                    error += ChronoUnit.SECONDS.between(deadline, time);
                }
                time = time.plusSeconds(scheduledTask.getConstraint().getDurationSeconds());
                if (time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(20, 0))) {
                    time = LocalTime.of(20, 1);
                }

            }
        }
        System.out.println("Error: " + error);
        */

        /*StandardConcurrentSwarm swarm = new StandardConcurrentSwarm(new FitnessFunction() {

            private long calcWeekMillis(LocalDate date) {
                LocalDate beginningOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                LocalDateTime beginningOfWeekAtSeven = beginningOfWeek.atTime(7, 0);
                LocalDateTime endOfWeek = beginningOfWeekAtSeven.plusDays(7);
                LocalDateTime endOfWeekAtMidnight = endOfWeek.toLocalDate().atTime(22, 0);
                return Duration.between(beginningOfWeekAtSeven, endOfWeekAtMidnight).toMillis();
            }

            private double calcError(Constraint c, LocalDateTime targetTime, LocalDateTime taskDeadline, LocalDateTime leadTimeStart, LocalDateTime targetPlusDuration) {

                double error = 0.0;

                // Determine weight based on deadline type
                double deadlineWeight;
                switch (c.getDeadlineType()) {
                    case ASAP:
                        deadlineWeight = 10.0;
                        break;
                    case HARD:
                        deadlineWeight = 8.0;
                        break;
                    case ANYTIME_ON_DAY:
                        deadlineWeight = 2.0;
                        break;
                    case ANYTIME_WEEK:
                        deadlineWeight = 1.0;
                        break;
                    case ANYTIME_MONTH:
                    case ANYTIME_BEFORE:
                    case ANYTIME_AFTER:
                        deadlineWeight = 0.5;
                        break;
                    default:
                        deadlineWeight = 1.0;
                }

                // Determine weight based on importance
                double importanceWeight;
                switch (c.getImportance()) {
                    case URGENT_IMPORTANT:
                        importanceWeight = 10.0;
                        break;
                    case URGENT_NOT_IMPORTANT:
                        importanceWeight = 8.0;
                        break;
                    case NOT_URGENT_IMPORTANT:
                        importanceWeight = 5.0;
                        break;
                    case NOT_URGENT_NOT_IMPORTANT:
                        importanceWeight = 3.0;
                        break;
                    default:
                        importanceWeight = 1.0;
                }

                // Combined multiplier
                double multiplier = deadlineWeight * importanceWeight;

                // Check if targetTime is before the leadTimeStart (i.e. too early)
                if (targetTime.isBefore(leadTimeStart) && c.getAllowedSecondsBeforeDeadline() > 0 && !c.getDeadlineType().equals(DeadlineType.ANYTIME_BEFORE)) {
                    // Calculate how many seconds early
                    Duration diff = Duration.between(targetTime, leadTimeStart);
                    double hoursLate = diff.getSeconds() / 60.0 / 60.0;
                    // Linear penalty for being early
                    error = hoursLate;
                }
                // Otherwise, if the task's end (targetPlusDuration) is after the deadline, it's late.
                else if (targetPlusDuration.isAfter(taskDeadline)) {
                    // Calculate how many seconds late the completion would be
                    Duration diff = Duration.between(taskDeadline, targetPlusDuration);
                    double hoursLate = diff.getSeconds() / 60.0 / 60.0;
                    // Cubic penalty for lateness; Math.pow can be adjusted if needed.
                    error = Math.pow(hoursLate, 2);
                }

                error *= multiplier;
                return error;
            }

            @Override
            public double calcFitness(Particle p) {
                LocalDate beginningOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                LocalDateTime beginningOfWeekAtSeven = beginningOfWeek.atTime(7, 0);

                long millisDifference = calcWeekMillis(LocalDate.now());
                long millisStart = beginningOfWeekAtSeven.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();

                //group child tasks together in some form split on duration.
                //don't schedule parent tasks
                //45 minute windows of work?
                //rest should be built in.
                //kid times are non moveable. like ghost times.
                //blocks of times can have tasks slotted into them.
                //interval, and tasks that fit in the interval.
                // 7am to 8:15am get ready for school (block 1)
                // 8:15am to 10:15am work   2
                // 10:15 to 10:45 walk/break/gardening
                // 11:00 to 12:00 work   3
                // 12:00 to 12:15 lunch
                // 12:15 to 2pm work     4
                // 2:15 pm to 3:125 driving
                // 8pm kids duty and other light non focus work 5
                // 8pm to 11pm work focus time 6
                // previously we generate a target date.
                // what about now we aim for a particular block? hmm.
                //

                Map<DayOfWeek, List<ScheduledTask>> newTasks = new HashMap<>();
                newTasks.put(DayOfWeek.SUNDAY, new ArrayList<>());
                newTasks.put(DayOfWeek.MONDAY, new ArrayList<>());
                newTasks.put(DayOfWeek.TUESDAY, new ArrayList<>());
                newTasks.put(DayOfWeek.WEDNESDAY, new ArrayList<>());
                newTasks.put(DayOfWeek.THURSDAY, new ArrayList<>());
                newTasks.put(DayOfWeek.FRIDAY, new ArrayList<>());
                newTasks.put(DayOfWeek.SATURDAY, new ArrayList<>());

                Random rand = new Random();
                List<ScheduledTask> pool = new ArrayList<>(scheduledTasks);
                while (!pool.isEmpty()) {
                    int randSTask = (int) (rand.nextDouble() * scheduledTasks.size());

                    ScheduledTask scheduledTask = scheduledTasks.get(randSTask);
                    pool.remove(scheduledTask);
                    newTasks.get(scheduledTask.getEndDay()).add(scheduledTask);
                }
                //Now we can do the fitness calc.

                int numBlocks = 6;
                double error = 0;
                for (int i = 0; i < p.getGene().size(); i+=2) {
                    ScheduledTask scheduledTask = scheduledTasks.get(i / 2);
                    double bucket = p.getGene().getValue(i / 2) * numBlocks;
                    double prob = p.getGene().getValue(i + 1);
                    newTasks.get(scheduledTask.getEndDay()).add(scheduledTask);

                    Constraint c = scheduledTask.getConstraint();
                    long targetMillis = millisStart + (long) (millisDifference * fraction);
                    LocalDateTime targetTime = Instant.ofEpochMilli(targetMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                    try {
                        LocalDate effectiveDate = beginningOfWeek.with(TemporalAdjusters.nextOrSame(scheduledTask.getEndDay()));
                        LocalDateTime taskDeadline = effectiveDate.atTime(c.getDeadlineTime());
                        LocalDateTime leadTimeStart = effectiveDate.atTime(c.getDeadlineTime().minusSeconds(c.getAllowedSecondsBeforeDeadline()));
                        LocalDateTime targetPlusDuration = targetTime.plusSeconds(c.getDurationSeconds());
                        error += calcError(c, targetTime, taskDeadline, leadTimeStart, targetPlusDuration);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return error;
            }

            @Override
            public int getDimension() {
                return scheduledTasks.size() * 2;
            }

            @Override
            public List<Domain> getDomain() {
                List<Domain> domains = new ArrayList<>();
                for (int i = 0; i < scheduledTasks.size() * 2; i++) {
                    domains.add(new Domain(0.0, 1.0));
                }
                return domains;
            }
        }, 100);
        try {
            int cnt = 0;
            while (cnt < 5) {
                double fitness = swarm.getGbestFitness();
                swarm.step();
                if (fitness == swarm.getGbestFitness()) {
                    cnt++;
                } else {
                    cnt = 0;
                }
                fitness = swarm.getGbestFitness();
                System.out.println(fitness + " -> " + swarm.getGbest());
            }
            System.out.println(swarm.getGbestFitness() + " -> " + swarm.getGbest());
            LocalDate beginningOfWeek = LocalDate.now()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
            LocalDateTime beginningOfWeekAtSeven = beginningOfWeek.atTime(7, 0);
            LocalDateTime endOfWeek = beginningOfWeekAtSeven.plusDays(7);
            Duration duration = Duration.between(beginningOfWeekAtSeven, endOfWeek);
            long millisStart = beginningOfWeekAtSeven.atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            long millisDifference = duration.toMillis();
            for (int i = 0; i < swarm.getGbest().size(); i++) {
                double fraction = swarm.getGbest().getValue(i);
                long targetMillis = millisStart + (long) (millisDifference * fraction);
                LocalDateTime targetTime = Instant.ofEpochMilli(targetMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                ScheduledTask scheduledTask = scheduledTasks.get(i);
                DayOfWeek dayOfWeek = targetTime.getDayOfWeek();
                System.out.println(scheduledTask);
                System.out.println(dayOfWeek + " -> " + targetTime);
                System.out.println();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        taskLLMProcessor.shutdownPool();
    }

    private static void checkForChanges(String line, String clt, Collection coll, BiConsumer<Constraint, Integer> func) {
        if (line.startsWith(clt)) {
            updateConstraintAmount(coll, line, clt, func);
        }
    }

    private static void checkForChangesString(String line, String clt, Collection coll, BiConsumer<Constraint, String> func,
                                              BiConsumer<Constraint, String> rmFunc) {
        if (line.startsWith(clt)) {
            updateConstraintString(coll, line, clt, func, rmFunc);
        }
    }

    private static void checkForToggle(String line, String clt, Collection coll, Consumer<Constraint> func) {
        if (line.startsWith(clt)) {
            updateConstraintNext(coll, func);
        }
    }

    public static void multiInput(BufferedReader reader, BiConsumer<String, String> updateFunction) throws IOException {
        System.out.print("Edit name: ");
        String name = reader.readLine();
        System.out.print("Edit description: ");
        String desc = reader.readLine();
        updateFunction.accept(name, desc);
    }

    public static List<SimpleTask> parseTasks(List<String> lines) {
        List<SimpleTask> tasks = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.trim().isEmpty()) {
                continue;
            }

            // Count leading spaces to determine the indentation level.
            int indent = countLeadingSpaces(line);
            String trimmed = line.trim();

            // Remove a leading dash, if present.
            if (trimmed.startsWith("-")) {
                trimmed = trimmed.substring(1).trim();
            }

            SimpleTask parsedTask = new SimpleTask();
            try {
                parsedTask.taskName = trimmed.trim();
                //parsedTask = taskExtractor.extractTaskFrom(trimmed);
            } catch (Exception ep) {
                //NOOP
                parsedTask.taskName = trimmed.trim();
            }
            if (parsedTask.taskName == null) {
                parsedTask.taskName = trimmed.trim();
            }
            parsedTask.indent = indent;
            SimpleTask parTask = getParent(tasks, indent);
            parsedTask.parentTask = (parTask != null) ? getParent(tasks, indent).taskName : null;
            parsedTask.line = i;
            tasks.add(parsedTask);
        }

        return tasks;
    }

    private static SimpleTask getParent(List<SimpleTask> tasks, int currIndent) {
        return IntStream.range(0, tasks.size())
                .map(i -> tasks.size() - 1 - i) // Convert to reverse index
                .mapToObj(tasks::get)
                .filter(task -> task.indent < currIndent)
                .findFirst()
                .orElse(null);
    }

    // Utility method to count leading spaces in a string.
    private static int countLeadingSpaces(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ' || c == '\t') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

}
