package org.dksd.tasks;

import org.dksd.tasks.model.Constraint;
import org.dksd.tasks.model.DeadlineType;
import org.dksd.tasks.model.NodeTask;
import org.dksd.tasks.pso.Domain;
import org.dksd.tasks.pso.FitnessFunction;
import org.dksd.tasks.pso.Particle;
import org.dksd.tasks.pso.StandardConcurrentSwarm;
import org.dksd.tasks.scheduling.ScheduledTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Main {

    public static void updateConstraintAmount(Collection coll, String line, String prefix, BiConsumer<Constraint, Integer> updater) {
        int amount = Integer.parseInt(line.substring(prefix.length()).trim());
        Constraint c = coll.getInstance().getConstraint(coll.getCurrentNodeTask().getConstraints().getFirst());
        updater.accept(c, amount);
    }

    public static void updateConstraintString(Collection coll, String line, String prefix, BiConsumer<Constraint, String> updater) {
        String value = line.substring(prefix.length()).trim();
        Constraint c = coll.getInstance().getConstraint(coll.getCurrentNodeTask().getConstraints().getFirst());
        updater.accept(c, value);
    }

    public static void updateConstraintNext(Collection coll, Consumer<Constraint> updater) {
        Constraint c = coll.getInstance().getConstraint(coll.getCurrentNodeTask().getConstraints().getFirst());
        updater.accept(c);
    }

    public static void main(String[] args) throws IOException {

        //Need some logs...
        //What was the last thing we inferenced on,
        //When did we write the files to disk.
        //Which is the latest version?

        //Mostly avoid inference as much as possible.
        //watchFiles(coll);
        //onFilesChange(arethereHashDifferences?)->thenPersist;
        //beforeEachLLmInference(checkHashCache);
        //beforeEachLLmInference(checkEmbeddingCache);

        Collection coll = new Collection(new Instance("test"));
        TaskLLMProcessor taskLLMProcessor = new TaskLLMProcessor(coll);
        taskLLMProcessor.processSimpleTasks(parseTasks(Files.readAllLines(coll.getInstance().getTodoFilePath())));
        List<ScheduledTask> scheduledTasks = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = null;

        while (!"q".equals(line)) {
            try {
                List<NodeTask> path = coll.getInlineTasks();
                TreeMap<Integer, NodeTask> sorted = new TreeMap<>();
                scheduledTasks.clear();
                for (NodeTask nodeTask : path) {
                    Constraint constraint = coll.getInstance().getConstraint(nodeTask.getConstraints().getFirst());
                    if (coll.getInstance().isLeaf(nodeTask)) {
                        for (DayOfWeek dayOfWeek : constraint.getDaysOfWeek()) {
                            ScheduledTask newTask = new ScheduledTask(nodeTask, coll.getInstance().getTask(nodeTask.getId()).getName(), dayOfWeek, constraint);
                            scheduledTasks.add(newTask);
                        }
                    }
                }
                coll.displayTasks(path, sorted);
                System.out.print("Enter choice: ");
                line = reader.readLine();

                checkForChanges(line, "cleadtime", coll, (c, amount) -> c.setAllowedSecondsBeforeDeadline(c.getAllowedSecondsBeforeDeadline() + amount * 60));
                checkForChanges(line, "cduration", coll, (c, amount) -> c.setDurationSeconds(c.getDurationSeconds() + amount * 60));
                checkForChanges(line, "cdeadline", coll, (c, amount) -> c.setDeadlineTime(c.getDeadlineTime().plusMinutes(amount)));
                checkForChangesString(line, "cday", coll, (c, day) -> c.getDaysOfWeek().add(DayOfWeek.valueOf(day)));
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
                        coll.setCurrentTask(path, i -> (i - 1 + path.size()) % path.size());
                        break;
                    case "n":
                        coll.setCurrentTask(path, i -> (i + 1) % path.size());
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
                    case ":w": // Write
                        coll.getInstance().write(coll);
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
        StandardConcurrentSwarm swarm = new StandardConcurrentSwarm(new FitnessFunction() {

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

                double error = 0;
                for (int i = 0; i < p.getGene().size(); i++) {
                    double fraction = p.getGene().getValue(i);
                    ScheduledTask scheduledTask = scheduledTasks.get(i);
                    Constraint c = scheduledTask.getConstraint();
                    long targetMillis = millisStart + (long) (millisDifference * fraction);
                    LocalDateTime targetTime = Instant.ofEpochMilli(targetMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                    LocalDate effectiveDate = beginningOfWeek.with(TemporalAdjusters.nextOrSame(scheduledTask.getEndDay()));
                    LocalDateTime taskDeadline = effectiveDate.atTime(c.getDeadlineTime());
                    LocalDateTime leadTimeStart = effectiveDate.atTime(c.getDeadlineTime().minusSeconds(c.getAllowedSecondsBeforeDeadline()));
                    LocalDateTime targetPlusDuration = targetTime.plusSeconds(c.getDurationSeconds());
                    error += calcError(c, targetTime, taskDeadline, leadTimeStart, targetPlusDuration);
                    /*if (targetTime.isBefore(effectiveDeadline)) {
                        error += calcError(targetTime, effectiveDeadline, 1);
                    } else if (targetTime.isAfter(fullEndTime)) {
                        error += calcError(fullEndTime, targetTime, 1);
                    }*/
                }
                return error;
            }

            @Override
            public int getDimension() {
                return scheduledTasks.size();
            }

            @Override
            public List<Domain> getDomain() {
                List<Domain> domains = new ArrayList<>();
                for (int i = 0; i < scheduledTasks.size(); i++) {
                    domains.add(new Domain(0.0, 1.0));
                }
                return domains;
            }
        }, 1000);
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
        }
    }

    private static void checkForChanges(String line, String clt, Collection coll, BiConsumer<Constraint, Integer> func) {
        if (line.startsWith(clt)) {
            updateConstraintAmount(coll, line, clt, func);
        }
    }

    private static void checkForChangesString(String line, String clt, Collection coll, BiConsumer<Constraint, String> func) {
        if (line.startsWith(clt)) {
            updateConstraintString(coll, line, clt, func);
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
            parsedTask.parentTask = getParent(lines, indent, i);
            parsedTask.line = i;
            tasks.add(parsedTask);
        }

        return tasks;
    }

    private static String getParent(List<String> lines, int indent, int i) {
        for (int j = i - 1; j >= 0; j--) {
            if (countLeadingSpaces(lines.get(j)) < indent) {
                return lines.get(j);
            }
        }
        return null;
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
