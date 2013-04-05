/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createPlannedActivityLabel;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;

/**
 * @author Jalpa Patel
 */
public class PlannedActivityLabelXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PlannedActivityLabel paLabel;
    private PlannedActivityLabelXmlSerializer serializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        paLabel = setGridId("PAL-89", createPlannedActivityLabel("testlabel", 2));
        serializer = new PlannedActivityLabelXmlSerializer();
    }

    public void testValidateElement() throws Exception {
        Element actual = serializer.createElement(paLabel);
        assertFalse(serializer.validateElement(null, actual));

        PlannedActivityLabel paLabel2 = createPlannedActivityLabel("testlabel", 2);
        assertTrue(serializer.validateElement(paLabel2, actual));
        paLabel2.setLabel("testlabel1");
        assertFalse(serializer.validateElement(paLabel2, actual));

        PlannedActivityLabel paLabel1 = createPlannedActivityLabel("testlabel", 2);
        assertTrue(serializer.validateElement(paLabel1, actual));
        paLabel1.setRepetitionNumber(3);
        assertFalse(serializer.validateElement(paLabel1, actual));
    }

    public void testCreateElement() throws Exception {
        Element actual = serializer.createElement(paLabel);
        assertEquals("Wrong element name", XsdElement.PLANNED_ACTIVITY_LABEL.xmlName(), actual.getName());
        assertEquals("Should have no children", 0, actual.elements().size());
        assertEquals("Wrong label", paLabel.getLabel(),
            LABEL_NAME.from(actual));
        assertEquals("Wrong repetition number",
            paLabel.getRepetitionNumber().toString(), LABEL_REP_NUM.from(actual));
        assertEquals("Wrong id", paLabel.getGridId(), LABEL_ID.from(actual));
    }

    public void testReadElement() throws Exception {
        Element input = elementFromString("<label id='PAL-72' name='pharma' repetition-number='2'/>");

        PlannedActivityLabel actual = serializer.readElement(input);
        assertEquals("Wrong id", "PAL-72", actual.getGridId());
        assertEquals("Wrong label", "pharma", actual.getLabel());
        assertEquals("Wrong rep", (Object) 2, actual.getRepetitionNumber());
    }
}
