package org.dksd.tasks;

import org.dksd.tasks.model.LinkType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkTest {

    @Test
    void testDefaultConstructor() {
        Link link = new Link();
        // Verify that an ID is generated
        assertNotNull(link.getId(), "Default constructor should generate a non-null id");
        // The other fields should be null by default.
        assertNull(link.getLeft(), "Left should be null by default");
        assertNull(link.getRight(), "Right should be null by default");
        assertNull(link.getLinkType(), "LinkType should be null by default");
    }

    @Test
    void testParameterizedConstructor() {
        UUID left = UUID.randomUUID();
        UUID right = UUID.randomUUID();
        LinkType linkType = LinkType.PARENT; // assuming LinkType enum contains PARENT

        Link link = new Link(left, linkType, right);
        assertNotNull(link.getId(), "Parameterized constructor should generate a non-null id");
        assertEquals(left, link.getLeft(), "Left value should match the provided value");
        assertEquals(right, link.getRight(), "Right value should match the provided value");
        assertEquals(linkType, link.getLinkType(), "LinkType should match the provided value");
    }

    @Test
    void testSettersAndGetters() {
        Link link = new Link();
        UUID left = UUID.randomUUID();
        UUID right = UUID.randomUUID();
        LinkType linkType = LinkType.DEPENDENCY; // assuming LinkType enum contains DEPENDENCY

        link.setLeft(left);
        link.setRight(right);
        link.setLinkType(linkType);

        assertEquals(left, link.getLeft(), "getLeft() should return the value set by setLeft()");
        assertEquals(right, link.getRight(), "getRight() should return the value set by setRight()");
        assertEquals(linkType, link.getLinkType(), "getLinkType() should return the value set by setLinkType()");
    }

    @Test
    void testEqualsAndHashCode() {
        // Create two Link instances using the same left, right, and linkType references.
        UUID left = UUID.randomUUID();
        UUID right = UUID.randomUUID();
        LinkType linkType = LinkType.SUBTASK; // assuming LinkType enum contains SUBTASK

        Link link1 = new Link(left, linkType, right);
        Link link2 = new Link(left, linkType, right);

        // Because equals compares using reference equality (==) for left and right,
        // using the same UUID objects ensures that they are considered equal.
        assertTrue(link1.equals(link2), "Links with the same left, right, and linkType should be equal");
        assertEquals(link1.hashCode(), link2.hashCode(), "Equal links should have the same hashCode");

        // Create a link with a different left value.
        Link link3 = new Link(UUID.randomUUID(), linkType, right);
        assertFalse(link1.equals(link3), "Links with different left values should not be equal");
    }

    @Test
    void testToString() {
        UUID left = UUID.randomUUID();
        UUID right = UUID.randomUUID();
        LinkType linkType = LinkType.PARENT;
        Link link = new Link(left, linkType, right);

        String toStringValue = link.toString();
        assertTrue(toStringValue.contains(left.toString()), "toString() should contain the left UUID");
        assertTrue(toStringValue.contains(right.toString()), "toString() should contain the right UUID");
        assertTrue(toStringValue.contains(linkType.toString()), "toString() should contain the LinkType");
    }
}

