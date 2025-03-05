package org.dksd.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class HelperTest {

    private Helper helper;

    @BeforeEach
    void setUp() {
        loadFiles("test_tasks.json", "test_links.json");
    }

    private void loadFiles(String tasks, String links) {
        ClassLoader classLoader = getClass().getClassLoader();
        File tasksFile = new File(classLoader.getResource(tasks).getFile());
        File linksFile = new File(classLoader.getResource(links).getFile());
        helper = new Helper(tasksFile, linksFile);
    }

    @Test
    void createTask() {
        Task task = helper.createTask("First Task", "first description");
        System.out.println("Task:" + task);
        System.out.println("Tasks:" + helper.getTasks());
        System.out.println("Links:" + helper.getLinks());
        System.out.println("Tree:" + helper.getTaskNodeMap());
        System.out.println("Full:" + helper.getTaskMap());
        System.out.println("CurrentTask:" + helper.getCurrentTask());
        assertTrue(helper.getLinks().isEmpty());
        assertNotNull(helper.getCurrentTask());
        assertEquals("First Task", task.getName());
        assertEquals(1, helper.getCurrentTask().getId());
        assertEquals(1, helper.getTasks().size());
        assertEquals(1, helper.getTaskMap().size());
        assertEquals(1, helper.getTaskNodeMap().get(1L).getId());
        assertEquals(0, helper.getTaskNodeMap().get(1L).getSubTasks().size());
        assertEquals(0, helper.getTaskNodeMap().get(1L).getDependencies().size());
        assertEquals(0, helper.getWorkingSet().size());
    }

    @Test
    void createSubTask() {
        Task task = helper.createTask("First Task", "first description");
        Task subTask = helper.createSubTask("First Sub Task", "first sub description");
        System.out.println("Tasks:" + helper.getTasks());
        System.out.println("Links:" + helper.getLinks());
        System.out.println("Tree:" + helper.getTaskNodeMap());
        System.out.println("Full:" + helper.getTaskMap());
        System.out.println("CurrentTask:" + helper.getCurrentTask());
        assertFalse(helper.getLinks().isEmpty());
        assertEquals(LinkType.PARENT, helper.getLinks().getFirst().getLinkType());
        assertEquals(LinkType.SUBTASK, helper.getLinks().get(1).getLinkType());
        assertNotNull(helper.getCurrentTask());
        assertEquals("First Task", task.getName());
        assertEquals(1, helper.getCurrentTask().getId());
        assertEquals(2, helper.getTasks().size());
        assertEquals(2, helper.getTaskMap().size());
        assertEquals(1, helper.getTaskNodeMap().get(1L).getId());
        assertEquals(1, helper.getTaskNodeMap().get(1L).getSubTasks().size());
        assertEquals(0, helper.getTaskNodeMap().get(1L).getDependencies().size());
        assertEquals(0, helper.getWorkingSet().size());
    }

    @Test
    void createDepTask() {
        Task task = helper.createTask("First Task", "first description");
        Task depTask = helper.createDepTask("First Dep Task", "first dep description");
        System.out.println("Tasks:" + helper.getTasks());
        System.out.println("Links:" + helper.getLinks());
        System.out.println("Tree:" + helper.getTaskNodeMap());
        System.out.println("Full:" + helper.getTaskMap());
        System.out.println("CurrentTask:" + helper.getCurrentTask());
        assertFalse(helper.getLinks().isEmpty());
        assertEquals(LinkType.PARENT, helper.getLinks().getFirst().getLinkType());
        assertEquals(LinkType.DEPENDENCY, helper.getLinks().get(1).getLinkType());
        assertNotNull(helper.getCurrentTask());
        assertEquals("First Task", task.getName());
        assertEquals(1, helper.getCurrentTask().getId());
        assertEquals(2, helper.getTasks().size());
        assertEquals(2, helper.getTaskMap().size());
        assertEquals(1, helper.getTaskNodeMap().get(1L).getId());
        assertEquals(0, helper.getTaskNodeMap().get(1L).getSubTasks().size());
        assertEquals(1, helper.getTaskNodeMap().get(1L).getDependencies().size());
        assertEquals(0, helper.getWorkingSet().size());
    }

    @Test
    void createSubTaskStress() {
        helper.createTask("First Task", "first description");
        for (int i = 0; i < 10000; i++) {
            helper.createSubTask("First Sub Task", "first sub description");
        }
        System.out.println("Tasks:" + helper.getTasks());
        System.out.println("Links:" + helper.getLinks());
        System.out.println("Tree:" + helper.getTaskNodeMap());
        System.out.println("Full:" + helper.getTaskMap());
        System.out.println("CurrentTask:" + helper.getCurrentTask());
    }



    @Test
    void moveUp() {
        helper.moveUp();
    }

    @Test
    void moveDown() {
    }



    @Test
    void buildTree() {
    }

    @Test
    void toJson() {
    }

    @Test
    void loadTasks() {
    }

    @Test
    void loadLinks() {
    }

    @Test
    void writeJson() {
    }

    @Test
    void selectTasks() {
    }
}