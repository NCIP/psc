package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;

/**
 * @author Jalpa Patel
 */
public class PlannedActivityLabelXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PlannedActivityLabel paLabel;
    private PlannedActivityLabelXmlSerializer plannedActivityLabelXmlSerializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        paLabel = Fixtures.createPlannedActivityLabel("testlabel", 2);
        plannedActivityLabelXmlSerializer = new PlannedActivityLabelXmlSerializer();
    }

    public void testValidateElement() throws Exception {
        Element actual = plannedActivityLabelXmlSerializer.createElement(paLabel);
        assertFalse(plannedActivityLabelXmlSerializer.validateElement(null, actual));

        PlannedActivityLabel paLabel2 = Fixtures.createPlannedActivityLabel("testlabel", 2);
        assertTrue(plannedActivityLabelXmlSerializer.validateElement(paLabel2, actual));
        paLabel2.setLabel("testlabel1");
        assertFalse(plannedActivityLabelXmlSerializer.validateElement(paLabel2, actual));

        PlannedActivityLabel paLabel1 = Fixtures.createPlannedActivityLabel("testlabel", 2);
        assertTrue(plannedActivityLabelXmlSerializer.validateElement(paLabel1, actual));
        paLabel1.setRepetitionNumber(3);
        assertFalse(plannedActivityLabelXmlSerializer.validateElement(paLabel1, actual));
    }

    public void testCreateElement() throws Exception {
        Element actual = plannedActivityLabelXmlSerializer.createElement(paLabel);
        assertEquals("Wrong element name", XsdElement.PLANNED_ACTIVITY_LABEL.xmlName(), actual.getName());
        assertEquals("Should have no children", 0, actual.elements().size());
        assertEquals("Wrong Label", paLabel.getLabel(),
            LABEL_NAME.from(actual));
        assertEquals("Wrong Repetition Number", paLabel.getRepetitionNumber().toString(), LABEL_REP_NUM.from(actual));
    }
}
