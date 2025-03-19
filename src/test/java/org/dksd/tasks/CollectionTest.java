package org.dksd.tasks;

import org.dksd.tasks.model.Constraint;
import org.dksd.tasks.model.Link;
import org.dksd.tasks.model.NodeTask;
import org.dksd.tasks.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionTest {

    private Collection collection;

    @BeforeEach
    void setUp() throws IOException {
        loadFiles("test_tasks.json", "test_links.json", "test_constraints.json");
    }

    private void loadFiles(String tasks, String links, String constraints) throws IOException {
        //ClassLoader classLoader = getClass().getClassLoader();
        //File tasksFile = new File(classLoader.getResource(tasks).getFile());
        //File linksFile = new File(classLoader.getResource(links).getFile());
        //File constraintsFile = new File(classLoader.getResource(constraints).getFile());
        Instance instance = new Instance("test");
        collection = new Collection(instance);
    }

    @Test
    void createTaskTest() {
        Task task = collection.getInstance().createSubTask(collection.getCurrentTask(), "First Task", "first description");
        System.out.println("Task:" + task);
        System.out.println("CurrentTask:" + collection.getCurrentTask());
        assertFalse(collection.getInstance().getLinks().isEmpty());
        assertEquals("First Task", task.getName());
        List<Task> tasks = collection.getInstance().getTasks();
        List<Link> links = collection.getInstance().getLinks();
        List<Constraint> constraints = collection.getInstance().getConstraints();
        assertEquals(2, tasks.size());
        assertEquals(1, constraints.size());
        assertEquals(3, links.size());

        Task root = tasks.get(0);
        Task st = tasks.get(1);
        NodeTask ntRoot = collection.getInstance().getTaskNodes().get(root.getId());
        NodeTask ntSubTask = collection.getInstance().getTaskNodes().get(st.getId());
        assertEquals(1, ntRoot.getSubTasks().size());
        assertEquals(0, ntSubTask.getSubTasks().size());
    }

    /*
    @Test
    void createProjectTasksTest() {
        Task task = collection.createProjectTask("Parent Task", "first description");
        collection.createProjectTask( "Second Task", "first description");
        collection.createProjectTask( "Third Task", "first description");
        System.out.println("Tasks:" + collection.getTasks());
        System.out.println("Links:" + collection.getLinks());
        System.out.println("Tree:" + collection.getTaskNodeMap());
        System.out.println("Full:" + collection.getTaskMap());
        System.out.println("CurrentTask:" + collection.getCurrentTask());
        assertEquals("Parent Task", task.getName());
        assertEquals(3, collection.getTasks().size());
        assertEquals(3, collection.getTaskMap().size());
        assertEquals(1, collection.getTaskNodeMap().get(1L).getId());
        assertEquals(0, collection.getTaskNodeMap().get(1L).getSubTasks().size());
        assertEquals(0, collection.getTaskNodeMap().get(1L).getDependencies().size());
        assertEquals(0, collection.getWorkingSet().size());
    }

    @Test
    void createSubTaskTest() {
        Task task = collection.createProjectTask("First Task", "first description");
        Task subTask = collection.createSubTask(task, "First Sub Task", "first sub description");
        System.out.println("Tasks:" + collection.getTasks());
        System.out.println("Links:" + collection.getLinks());
        System.out.println("Tree:" + collection.getTaskNodeMap());
        System.out.println("Full:" + collection.getTaskMap());
        System.out.println("CurrentTask:" + collection.getCurrentTask());
        assertFalse(collection.getLinks().isEmpty());
        assertEquals(LinkType.PARENT, collection.getLinks().getFirst().getLinkType());
        assertEquals(LinkType.SUBTASK, collection.getLinks().get(1).getLinkType());
        assertEquals("First Task", task.getName());
        assertEquals(2, collection.getTasks().size());
        assertEquals(2, collection.getTaskMap().size());
        assertEquals(1, collection.getTaskNodeMap().get(1L).getId());
        assertEquals(1, collection.getTaskNodeMap().get(1L).getSubTasks().size());
        assertEquals(0, collection.getTaskNodeMap().get(1L).getDependencies().size());
        assertEquals(0, collection.getWorkingSet().size());
    }

    @Test
    void createDepTaskTest() {
        Task task = collection.createProjectTask("First Task", "first description");
        Task depTask = collection.createDepTask(task, "First Dep Task", "first dep description");
        System.out.println("Tasks:" + collection.getTasks());
        System.out.println("Links:" + collection.getLinks());
        System.out.println("Tree:" + collection.getTaskNodeMap());
        System.out.println("Full:" + collection.getTaskMap());
        System.out.println("CurrentTask:" + collection.getCurrentTask());
        assertFalse(collection.getLinks().isEmpty());
        assertEquals(LinkType.PARENT, collection.getLinks().getFirst().getLinkType());
        assertEquals(LinkType.DEPENDENCY, collection.getLinks().get(1).getLinkType());
        assertEquals("First Task", task.getName());
        assertEquals(2, collection.getTasks().size());
        assertEquals(2, collection.getTaskMap().size());
        assertEquals(1, collection.getTaskNodeMap().get(1L).getId());
        assertEquals(0, collection.getTaskNodeMap().get(1L).getSubTasks().size());
        assertEquals(1, collection.getTaskNodeMap().get(1L).getDependencies().size());
        assertEquals(0, collection.getWorkingSet().size());
    }

    @Test
    void createSubTaskStress() {
        Task task = collection.createProjectTask("First Task", "first description");
        for (int i = 0; i < 10000; i++) {
            collection.createSubTask(task, "First Sub Task", "first sub description");
        }
        System.out.println("Tasks:" + collection.getTasks());
        System.out.println("Links:" + collection.getLinks());
        System.out.println("Tree:" + collection.getTaskNodeMap());
        System.out.println("Full:" + collection.getTaskMap());
        System.out.println("CurrentTask:" + collection.getCurrentTask());
    }

    @Test
    void moveUpTest() {
        Task p1 = collection.createProjectTask("First Project", "first project description");
        Task p2 = collection.createProjectTask("Second Project", "first project description");
        Task p3 = collection.createProjectTask("Third Project", "first project description");
        collection.createDepTask(p2, "Second Dep Task", "Second dep description");
        Task p7 = collection.createSubTask(p3, "Third Dep Task", "Third dep description");
        System.out.println(collection.getTaskNodeMap().get(p7.getId()));
        NodeTask nt = collection.moveUp(collection.getTaskNodeMap().get(p7.getId()));
        System.out.println(nt);
        assertEquals(3L, nt.getId());
        nt = collection.moveUp(nt);
        assertEquals(2L, nt.getId());
        nt = collection.moveUp(nt);
        assertEquals(1L, nt.getId());
        nt = collection.moveUp(nt);
        assertEquals(0L, nt.getId());
        nt = collection.moveUp(nt);
        assertEquals(0L, nt.getId());
    }

    @Test
    void moveDownTest() {
        Task p1 = collection.createProjectTask("First Project", "first project description");
        Task p2 = collection.createProjectTask("Second Project", "first project description");
        Task p3 = collection.createProjectTask("Third Project", "first project description");
        collection.createDepTask(p2, "Second Dep Task", "Second dep description");
        Task p7 = collection.createSubTask(p3, "Third Dep Task", "Third dep description");
        System.out.println(collection.getTaskNodeMap().get(p7.getId()));
        NodeTask nt = collection.moveDown(collection.getTaskNodeMap().get(p1.getId()));
        System.out.println(nt);
        assertEquals(2L, nt.getId());
        nt = collection.moveDown(nt);
        assertEquals(3L, nt.getId());
        nt = collection.moveDown(nt);
        assertEquals(3L, nt.getId());
        String tasks = collection.toJson(collection.getTasks());
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

     */
}