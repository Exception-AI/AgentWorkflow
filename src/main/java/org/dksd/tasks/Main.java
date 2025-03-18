package org.dksd.tasks;

import org.dksd.tasks.model.Constraint;
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public class Main {

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
                    for (DayOfWeek dayOfWeek : constraint.getDaysOfWeek()) {
                        ScheduledTask newTask = new ScheduledTask(nodeTask, coll.getInstance().getTask(nodeTask.getId()).getName(),dayOfWeek, constraint);
                        scheduledTasks.add(newTask);
                    }
                }
                //WeekScheduler scheduler = new WeekScheduler();
                //Map<DayOfWeek, List<ScheduledTask>> weekSchedule = scheduler.planWeekTasks(path, coll.getInstance().getConstraintMap());
                //System.out.println("Week Scheduler: " + weekSchedule);
                coll.displayTasks(path, sorted);
                System.out.print("Enter choice: ");
                line = reader.readLine();
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
        //Should I unroll all the tasks based on schedules...
        //
        StandardConcurrentSwarm swarm = new StandardConcurrentSwarm(new FitnessFunction() {
            @Override
            public double calcFitness(Particle p) {
                LocalDate beginningOfWeek = LocalDate.now()
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                LocalDateTime beginningOfWeekAtSeven = beginningOfWeek.atTime(7, 0);
                LocalDateTime endOfWeek = beginningOfWeekAtSeven.plusDays(7);
                Duration duration = Duration.between(beginningOfWeekAtSeven, endOfWeek);
                long millisStart = beginningOfWeekAtSeven.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();
                long millisDifference = duration.toMillis();
                long endOfWeekMillis = endOfWeek.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();

                double error = 0;
                //TODO can now do some calcs.
                for (int i = 0; i < p.getGene().size(); i++) {
                    double fraction = p.getGene().getValue(i);
                    ScheduledTask scheduledTask = scheduledTasks.get(i);
                    long targetMillis = millisStart + (long) (millisDifference * fraction);
                    LocalDateTime targetTime = Instant.ofEpochMilli(targetMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    //TODO calc difference between the two
                    System.out.println(targetTime);
                }

                /*LocalDateTime time = beginningOfWeekAtSeven;
                //we step in unison to the end
                for (Map.Entry<Double, Integer> entry : sorted.entrySet()) {
                    ScheduledTask scheduledTask = scheduledTasks.get(entry.getValue());
                    Constraint c = scheduledTask.getConstraint();
                    LocalTime lt = c.getEndTime().minusSeconds(c.getLeadTimeSeconds());
                    time = (lt.isAfter(time.toLocalTime())) ? LocalDateTime.of(time.toLocalDate(), lt.plusSeconds(1)) : time;
                    Duration durationDifference = Duration.between(lt, time.toLocalTime());
                    error += Math.abs(durationDifference.toSeconds());
                    //time = LocalDateTime.of(time.toLocalDate(), c.getEndTime().plusSeconds(1));
                }
*/
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
        }, 10);
        try {
            for (int i = 0; i < 5; i++) {
                swarm.step();
            }
            System.out.println(swarm.getGbest());

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
