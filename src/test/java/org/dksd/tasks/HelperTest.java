package org.dksd.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class HelperTest {

    private Helper helper;

    @BeforeEach
    void setUp() {
        loadFiles("test_tasks.json", "test_links.json", "test_constraints.json");
    }

    private void loadFiles(String tasks, String links, String constraints) {
        ClassLoader classLoader = getClass().getClassLoader();
        File tasksFile = new File(classLoader.getResource(tasks).getFile());
        File linksFile = new File(classLoader.getResource(links).getFile());
        File constraintsFile = new File(classLoader.getResource(constraints).getFile());
        helper = new Helper(tasksFile, linksFile, constraintsFile);
    }

    @Test
    void createTaskTest() {
        Task task = helper.createProjectTask("First Task", "first description");
        System.out.println("Task:" + task);
        System.out.println("Tasks:" + helper.getTasks());
        System.out.println("Links:" + helper.getLinks());
        System.out.println("Tree:" + helper.getTaskNodeMap());
        System.out.println("Full:" + helper.getTaskMap());
        System.out.println("CurrentTask:" + helper.getCurrentTask());
        assertTrue(helper.getLinks().isEmpty());
        assertEquals("First Task", task.getName());
        assertEquals(1, helper.getTasks().size());
        assertEquals(1, helper.getTaskMap().size());
        assertEquals(1, helper.getTaskNodeMap().get(1L).getId());
        assertEquals(0, helper.getTaskNodeMap().get(1L).getSubTasks().size());
        assertEquals(0, helper.getTaskNodeMap().get(1L).getDependencies().size());
        assertEquals(0, helper.getWorkingSet().size());
    }

    @Test
    void createProjectTasksTest() {
        Task task = helper.createProjectTask("Parent Task", "first description");
        helper.createProjectTask( "Second Task", "first description");
        helper.createProjectTask( "Third Task", "first description");
        System.out.println("Tasks:" + helper.getTasks());
        System.out.println("Links:" + helper.getLinks());
        System.out.println("Tree:" + helper.getTaskNodeMap());
        System.out.println("Full:" + helper.getTaskMap());
        System.out.println("CurrentTask:" + helper.getCurrentTask());
        assertEquals("Parent Task", task.getName());
        assertEquals(3, helper.getTasks().size());
        assertEquals(3, helper.getTaskMap().size());
        assertEquals(1, helper.getTaskNodeMap().get(1L).getId());
        assertEquals(0, helper.getTaskNodeMap().get(1L).getSubTasks().size());
        assertEquals(0, helper.getTaskNodeMap().get(1L).getDependencies().size());
        assertEquals(0, helper.getWorkingSet().size());
    }

    @Test
    void createSubTaskTest() {
        Task task = helper.createProjectTask("First Task", "first description");
        Task subTask = helper.createSubTask(task, "First Sub Task", "first sub description");
        System.out.println("Tasks:" + helper.getTasks());
        System.out.println("Links:" + helper.getLinks());
        System.out.println("Tree:" + helper.getTaskNodeMap());
        System.out.println("Full:" + helper.getTaskMap());
        System.out.println("CurrentTask:" + helper.getCurrentTask());
        assertFalse(helper.getLinks().isEmpty());
        assertEquals(LinkType.PARENT, helper.getLinks().getFirst().getLinkType());
        assertEquals(LinkType.SUBTASK, helper.getLinks().get(1).getLinkType());
        assertEquals("First Task", task.getName());
        assertEquals(2, helper.getTasks().size());
        assertEquals(2, helper.getTaskMap().size());
        assertEquals(1, helper.getTaskNodeMap().get(1L).getId());
        assertEquals(1, helper.getTaskNodeMap().get(1L).getSubTasks().size());
        assertEquals(0, helper.getTaskNodeMap().get(1L).getDependencies().size());
        assertEquals(0, helper.getWorkingSet().size());
    }

    @Test
    void createDepTaskTest() {
        Task task = helper.createProjectTask("First Task", "first description");
        Task depTask = helper.createDepTask(task, "First Dep Task", "first dep description");
        System.out.println("Tasks:" + helper.getTasks());
        System.out.println("Links:" + helper.getLinks());
        System.out.println("Tree:" + helper.getTaskNodeMap());
        System.out.println("Full:" + helper.getTaskMap());
        System.out.println("CurrentTask:" + helper.getCurrentTask());
        assertFalse(helper.getLinks().isEmpty());
        assertEquals(LinkType.PARENT, helper.getLinks().getFirst().getLinkType());
        assertEquals(LinkType.DEPENDENCY, helper.getLinks().get(1).getLinkType());
        assertEquals("First Task", task.getName());
        assertEquals(2, helper.getTasks().size());
        assertEquals(2, helper.getTaskMap().size());
        assertEquals(1, helper.getTaskNodeMap().get(1L).getId());
        assertEquals(0, helper.getTaskNodeMap().get(1L).getSubTasks().size());
        assertEquals(1, helper.getTaskNodeMap().get(1L).getDependencies().size());
        assertEquals(0, helper.getWorkingSet().size());
    }

    @Test
    void createSubTaskStress() {
        Task task = helper.createProjectTask("First Task", "first description");
        for (int i = 0; i < 10000; i++) {
            helper.createSubTask(task, "First Sub Task", "first sub description");
        }
        System.out.println("Tasks:" + helper.getTasks());
        System.out.println("Links:" + helper.getLinks());
        System.out.println("Tree:" + helper.getTaskNodeMap());
        System.out.println("Full:" + helper.getTaskMap());
        System.out.println("CurrentTask:" + helper.getCurrentTask());
    }

    @Test
    void moveUpTest() {
        Task p1 = helper.createProjectTask("First Project", "first project description");
        Task p2 = helper.createProjectTask("Second Project", "first project description");
        Task p3 = helper.createProjectTask("Third Project", "first project description");
        helper.createDepTask(p2, "Second Dep Task", "Second dep description");
        Task p7 = helper.createSubTask(p3, "Third Dep Task", "Third dep description");
        System.out.println(helper.getTaskNodeMap().get(p7.getId()));
        NodeTask nt = helper.moveUp(helper.getTaskNodeMap().get(p7.getId()));
        System.out.println(nt);
        assertEquals(3L, nt.getId());
        nt = helper.moveUp(nt);
        assertEquals(2L, nt.getId());
        nt = helper.moveUp(nt);
        assertEquals(1L, nt.getId());
        nt = helper.moveUp(nt);
        assertEquals(0L, nt.getId());
        nt = helper.moveUp(nt);
        assertEquals(0L, nt.getId());
    }

    @Test
    void moveDownTest() {
        Task p1 = helper.createProjectTask("First Project", "first project description");
        Task p2 = helper.createProjectTask("Second Project", "first project description");
        Task p3 = helper.createProjectTask("Third Project", "first project description");
        helper.createDepTask(p2, "Second Dep Task", "Second dep description");
        Task p7 = helper.createSubTask(p3, "Third Dep Task", "Third dep description");
        System.out.println(helper.getTaskNodeMap().get(p7.getId()));
        NodeTask nt = helper.moveDown(helper.getTaskNodeMap().get(p1.getId()));
        System.out.println(nt);
        assertEquals(2L, nt.getId());
        nt = helper.moveDown(nt);
        assertEquals(3L, nt.getId());
        nt = helper.moveDown(nt);
        assertEquals(3L, nt.getId());
        String tasks = helper.toJson(helper.getTasks());
        System.out.println(tasks);
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