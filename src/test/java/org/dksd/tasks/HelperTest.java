package org.dksd.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class HelperTest {

    private Helper helper;

    @BeforeEach
    void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        File tasksFile = new File(classLoader.getResource("test_tasks.json").getFile());
        File linksFile = new File(classLoader.getResource("test_links.json").getFile());
        helper = new Helper(tasksFile, linksFile);
    }

    @Test
    void moveUp() {
        helper.moveUp();
    }

    @Test
    void moveDown() {
    }

    @Test
    void createCommonTask() {
    }

    @Test
    void createSubTask() {
    }

    @Test
    void createDepTask() {
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