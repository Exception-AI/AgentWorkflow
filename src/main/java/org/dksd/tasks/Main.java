package org.dksd.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        Map<Instance, Helper> helpers = new HashMap<>();
        Instance instance = new Instance();
        helpers.put(instance, new Helper("tasks.json", "links.json", "constraints.json"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        Helper helper = helpers.get(instance);

        while (!"q".equals(line)) {
            try {
                helper.displayTasks();

                System.out.print("Enter choice: ");
                line = reader.readLine();

                switch (line) {
                    case "/": // Search
                        System.out.print("Find: ");
                        helper.find(reader.readLine());
                        break;
                    case "n": // Next
                        helper.setCurrentTask(helper.nextTask(helper.getCurrentTask()));
                        break;
                    case "p": // Next
                        helper.setCurrentTask(helper.prevTask(helper.getCurrentTask()));
                        break;
                    case "o": // Next
                        helper.setCurrentTaskToParent();
                        break;
                    case "": // Enter key
                        //selectTask();
                        System.out.println("Enter pressed");
                        break;
                    case "cp":
                        helper.multiInput(reader, helper::createProjectTask);
                        break;
                    case "cs":
                        helper.multiInput(reader, (name, desc) -> helper.createSubTask(helper.getCurrent(), name, desc));
                        break;
                    case "cd":
                        helper.multiInput(reader, (name, desc) -> helper.createDepTask(helper.getCurrent(), name, desc));
                        break;
                    case "e":
                        helper.multiInput(reader, (name, desc) -> helper.updateTask(helper.getCurrent(), name, desc));
                        break;
                    case ":w": // Write
                        write(helper);
                        break;
                    case "q": // Quit
                        break;
                    default:
                        break;
                }
                helper.selectTasks();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        write(helper);
    }

    public static void write(Helper helper) {
        helper.writeJson("tasks.json", helper.toJson(helper.getTasks()));
        helper.writeJson("links.json", helper.toJson(helper.getLinks()));
        helper.writeJson("constraints.json", helper.toJson(helper.getConstraints()));
    }
}