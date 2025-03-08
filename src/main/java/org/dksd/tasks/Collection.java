package org.dksd.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class Collection {

    private List<Instance> instances = new ArrayList<>();
    private ObjectMapper mapper = new ObjectMapper();

    public Collection() {
    }

    public String toJson(List<?> tasks) {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(tasks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void displayTasks() {
        String greenCheck = "\u001B[32m\u2713\u001B[0m";
        //Needs to be recursive right?
        //for (Task wt : workingSet) {
        for (Instance instance : instances) {
        NodeTask currentTask = instance.getCurrentNodeTask();
        Task wt = instance.getTask(currentTask.getId());
        String suffix = currentTask != null && currentTask.getId() == wt.getId() ? " (*) " : "";

        NodeTask p = instance.getTaskNode(wt.getId());
        List<String> hierarchy = new ArrayList<>();
        while (p.getParentId() != null) {
            p = instance.getTaskNode(p.getParentId());
            hierarchy.add(instance.getTask(p.getId()).getName());
        }

        String indent = "  ";
        System.out.println(wt.getName() + " <- " + hierarchy + " " + suffix);
        //System.out.println(suffix + "   Description: " + wt.getDescription());
        System.out.flush();
            /*if (!taskNodeMap.get(wt.getId()).getSubTasks().isEmpty()) {
                System.out.println(suffix + "   SubTasks: ");
            }*/

        for (UUID subTask : p.getSubTasks()) {
            for (UUID constraint : p.getConstraints()) {
                System.out.print(instance.getConstraint(constraint).toCompactString());
            }
            System.out.println(indent + "- " + instance.getTask(subTask).getName());
        }
        System.out.flush();
        if (!p.getDependencies().isEmpty()) {
            System.out.print(indent + "  Dependencies: ");
        }
        for (UUID dep : p.getDependencies()) {
            System.out.print(instance.getTask(dep).getName() + ", ");
        }

        System.out.flush();
        }
    }

    public void multiInput(BufferedReader reader, BiConsumer<String, String> updateFunction) throws IOException {
        System.out.print("Edit name: ");
        String name = reader.readLine();
        System.out.print("Edit description: ");
        String desc = reader.readLine();
        updateFunction.accept(name, desc);
    }

    public void find(String searchTerm) {
        for (Instance instance : instances) {
            for (Task task : instance.getTasks()) {
                if (task.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    instance.setCurrentTaskNode(instance.getTaskNode(task.getId()));
                }
            }
        }
    }



}
