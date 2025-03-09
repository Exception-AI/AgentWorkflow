package org.dksd.tasks;

import org.dksd.tasks.model.Concentration;
import org.dksd.tasks.model.Cost;
import org.dksd.tasks.model.DeadlineType;
import org.dksd.tasks.model.Effort;
import org.dksd.tasks.model.Importance;
import org.dksd.tasks.model.LeadTime;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintTest {

    @Test
    void testDefaultConstructor() {
        Constraint constraint = new Constraint();

        // Verify that an ID is generated
        assertNotNull(constraint.getId(), "ID should not be null");
        // Verify default schedule value
        assertEquals("30 22 * * 1", constraint.getSchedule(), "Default schedule should be '30 22 * * 1'");
        // Verify default LeadTime, Effort, Cost, Importance, Concentration, and DeadlineType
        assertEquals(LeadTime.ONE_DAY, constraint.getLeadTime(), "Default lead time should be ONE_DAY");
        assertEquals(Effort.MEDIUM, constraint.getEffort(), "Default effort should be MEDIUM");
        assertEquals(Cost.CHEAP, constraint.getCost(), "Default cost should be CHEAP");
        assertEquals(Importance.NOT_URGENT_IMPORTANT, constraint.getImportance(), "Default importance should be NOT_URGENT_IMPORTANT");
        assertEquals(Concentration.PARTIAL, constraint.getConcentration(), "Default concentration should be PARTIAL");
        assertEquals(DeadlineType.SOFT, constraint.getDeadlineType(), "Default deadline type should be SOFT");
    }

    @Test
    void testSettersAndGetters() {
        Constraint constraint = new Constraint();

        // Create new values (assume these enum constants exist)
        String newSchedule = "0 0 * * *";
        LeadTime newLeadTime = LeadTime.TWO_DAYS; // Assumes TWO_DAYS exists
        Effort newEffort = Effort.HIGH;
        Cost newCost = Cost.EXPENSIVE; // Assumes EXPENSIVE exists
        Importance newImportance = Importance.URGENT_IMPORTANT; // Assumes URGENT_IMPORTANT exists
        Concentration newConcentration = Concentration.FULL; // Assumes FULL exists
        DeadlineType newDeadlineType = DeadlineType.HARD; // Assumes HARD exists

        // Set new values
        constraint.setSchedule(newSchedule);
        constraint.setLeadTime(newLeadTime);
        constraint.setEffort(newEffort);
        constraint.setCost(newCost);
        constraint.setImportance(newImportance);
        constraint.setConcentration(newConcentration);
        constraint.setDeadlineType(newDeadlineType);

        // Verify that the getters return the new values
        assertEquals(newSchedule, constraint.getSchedule());
        assertEquals(newLeadTime, constraint.getLeadTime());
        assertEquals(newEffort, constraint.getEffort());
        assertEquals(newCost, constraint.getCost());
        assertEquals(newImportance, constraint.getImportance());
        assertEquals(newConcentration, constraint.getConcentration());
        assertEquals(newDeadlineType, constraint.getDeadlineType());
    }

    @Test
    void testToCompactString() {
        Constraint constraint = new Constraint();
        // The default constructor sets values, so we build the expected compact string based on those defaults.
        String expected = constraint.getImportance().getValue() + ":" +
                constraint.getEffort().getValue() + ":" +
                constraint.getCost().getValue() + ":" +
                constraint.getConcentration().getValue() + ":" +
                constraint.getDeadlineType().getValue();
        assertEquals(expected, constraint.toCompactString(), "The compact string should match the expected format");
    }

    @Test
    void testToStringContainsFields() {
        Constraint constraint = new Constraint();
        String str = constraint.toString();

        // Check that the string representation contains key field names/values
        assertTrue(str.contains("schedule='30 22 * * 1'"), "toString() should contain the schedule");
        assertTrue(str.contains("effort=" + constraint.getEffort()), "toString() should contain the effort");
        assertTrue(str.contains("cost=" + constraint.getCost()), "toString() should contain the cost");
        assertTrue(str.contains("importance=" + constraint.getImportance()), "toString() should contain the importance");
        assertTrue(str.contains("concentration=" + constraint.getConcentration()), "toString() should contain the concentration");
        assertTrue(str.contains("deadlineType=" + constraint.getDeadlineType()), "toString() should contain the deadlineType");
    }
}
