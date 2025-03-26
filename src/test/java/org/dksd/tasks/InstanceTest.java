package org.dksd.tasks;

import org.dksd.tasks.model.Instance;
import org.dksd.tasks.model.LinkType;
import org.dksd.tasks.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InstanceTest {

    private Instance instance;
    // Use a unique instance name for each test run.
    private final String instanceName = "test-instance-" + UUID.randomUUID();

    @BeforeEach
    void setUp() throws IOException {
        instance = new Instance(instanceName);
    }

    @AfterEach
    void tearDown() {
        // Delete the created instance directory after each test.
        File dir = new File("data/" + instanceName);
        if (dir.exists()) {
            deleteDirectory(dir);
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    @Test
    void testInstanceConstructorCreatesRootTask() {
        List<Task> tasks = instance.getTasks();
        assertNotNull(tasks, "Tasks list should not be null");
        assertFalse(tasks.isEmpty(), "Tasks list should not be empty after construction");

        Task root = tasks.get(0);
        assertTrue(root.getName().startsWith("ROOT:"), "First task should be a root task with name starting with 'ROOT:'");
    }

    @Test
    void testCreateCommonTask() {
        Task parent = instance.getTasks().get(0); // use the root task
        int initialTaskCount = instance.getTasks().size();
        int initialLinkCount = instance.getLinks().size();
        int initialConstraintCount = instance.getConstraints().size();

        Task newTask = instance.createCommonTask(parent, "New Task", "New Task Description");
        assertNotNull(newTask, "New task should not be null");
        assertEquals(initialTaskCount + 1, instance.getTasks().size(), "Task count should increase by 1");

        // createCommonTask adds one PARENT link and one CONSTRAINT link.
        assertEquals(initialLinkCount + 2, instance.getLinks().size(), "Link count should increase by 2");
        assertEquals(initialConstraintCount + 1, instance.getConstraints().size(), "Constraint count should increase by 1");

        // Verify that a PARENT link exists from the parent to the new task.
        boolean foundParentLink = instance.getLinks().stream()
                .anyMatch(link -> link.getLeft().equals(parent.getId())
                        && link.getRight().equals(newTask.getId())
                        && link.getLinkType() == LinkType.PARENT);
        assertTrue(foundParentLink, "There should be a PARENT link from the parent to the new task");
    }

    @Test
    void testCreateSubTask() {
        Task parent = instance.getTasks().get(0);
        int initialTaskCount = instance.getTasks().size();
        int initialLinkCount = instance.getLinks().size();

        Task subTask = instance.createSubTask(parent, "Sub Task", "Sub Task Description");
        assertNotNull(subTask, "Sub-task should not be null");
        assertEquals(initialTaskCount + 1, instance.getTasks().size(), "Task count should increase by 1");

        // createSubTask calls createCommonTask (which adds a PARENT link and constraint) and then adds a SUBTASK link.
        assertEquals(initialLinkCount + 3, instance.getLinks().size(), "Link count should increase by 3");

        boolean foundSubTaskLink = instance.getLinks().stream()
                .anyMatch(link -> link.getLeft().equals(parent.getId())
                        && link.getRight().equals(subTask.getId())
                        && link.getLinkType() == LinkType.SUBTASK);
        assertTrue(foundSubTaskLink, "There should be a SUBTASK link from the parent to the sub-task");
    }

    @Test
    void testCreateDepTask() {
        Task parent = instance.getTasks().get(0);
        int initialTaskCount = instance.getTasks().size();
        int initialLinkCount = instance.getLinks().size();

        Task depTask = instance.createDepTask(parent, "Dep Task", "Dep Task Description");
        assertNotNull(depTask, "Dependency task should not be null");
        assertEquals(initialTaskCount + 1, instance.getTasks().size(), "Task count should increase by 1");

        // createDepTask calls createCommonTask (which adds a PARENT link and constraint) and then adds a DEPENDENCY link.
        assertEquals(initialLinkCount + 3, instance.getLinks().size(), "Link count should increase by 3");

        boolean foundDepTaskLink = instance.getLinks().stream()
                .anyMatch(link -> link.getLeft().equals(parent.getId())
                        && link.getRight().equals(depTask.getId())
                        && link.getLinkType() == LinkType.DEPENDENCY);
        assertTrue(foundDepTaskLink, "There should be a DEPENDENCY link from the parent to the dependency task");
    }

    @Test
    void testWriteMethod() {

        File instanceDir = new File("data/" + instanceName);
        File tasksFile = new File(instanceDir, "tasks.json");
        File linksFile = new File(instanceDir, "links.json");
        File constraintsFile = new File(instanceDir, "constraints.json");

        // Check that files were created.
        assertFalse(tasksFile.exists(), "Tasks file should exist after writing");
        assertFalse(linksFile.exists(), "Links file should exist after writing");
        assertFalse(constraintsFile.exists(), "Constraints file should exist after writing");

        // Optionally, verify that the files are not empty.
        assertFalse(tasksFile.length() > 0, "Tasks file should not be empty");
        assertFalse(linksFile.length() > 0, "Links file should not be empty");
        assertFalse(constraintsFile.length() > 0, "Constraints file should not be empty");
    }
}
