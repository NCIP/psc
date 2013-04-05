/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.SortedSet;
import java.util.TreeSet;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setGridId;

public class PeriodXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PeriodXmlSerializer serializer;
    private Element element;
    private Period period;
    private PlannedActivityXmlSerializer plannedActivitySerializer;
    private SortedSet<Period> periods;


    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        plannedActivitySerializer = registerMockFor(PlannedActivityXmlSerializer.class);
        serializer = new PeriodXmlSerializer();
        serializer.setChildXmlSerializer(plannedActivitySerializer);
        period = setGridId("grid0", Fixtures.createPeriod("Period A", 1, 7, 3));

        periods = new TreeSet<Period>();
    }

    public void testCreateElementPeriod() {
        Element actual = serializer.createElement(period);

        assertEquals("Wrong attribute size", 6, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong period name", "Period A", actual.attribute("name").getValue());
        assertEquals("Wrong period duration unit", "day", actual.attributeValue("duration-unit"));
        assertEquals("Wrong period duration quantity", "7", actual.attributeValue("duration-quantity"));
        assertEquals("Wrong period start day", "1", actual.attributeValue("start-day"));
        assertEquals("Wrong period repetitions", "3", actual.attributeValue("repetitions"));

    }

    public void testReadElementPeriod() {
        Element elt = createPeriodElement();
        elt.addAttribute("duration-unit", "day");
        Period actual = (Period) serializer.readElement(elt);

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong period name", "Period A", actual.getName());
        assertEquals("Wrong period duration unit", "day", actual.getDuration().getUnit().toString());
        assertEquals("Wrong period duration quantity", new Integer(7), actual.getDuration().getQuantity());
        assertEquals("Wrong period start day", new Integer(1), actual.getStartDay());
        assertEquals("Wrong period repetitions", 3, actual.getRepetitions());
    }

    public void testReadElementWithDurationUnitAsMonth() throws Exception {
        Element elt = createPeriodElement();
        elt.addAttribute("duration-unit", "month");

        Period actual = (Period) serializer.readElement(elt);
        assertEquals("Wrong period duration unit", "month", actual.getDuration().getUnit().toString());
    }

    public void testReadElementWithDurationUnitAsDay() throws Exception {
        Element elt = createPeriodElement();
        elt.addAttribute("duration-unit", "day");

        Period actual = (Period) serializer.readElement(elt);
        assertEquals("Wrong period duration unit", "day", actual.getDuration().getUnit().toString());
    }

    public void testReadElementWithDurationUnitAsWeek() throws Exception {
        Element elt = createPeriodElement();
        elt.addAttribute("duration-unit", "week");

        Period actual = (Period) serializer.readElement(elt);
        assertEquals("Wrong period duration unit", "week", actual.getDuration().getUnit().toString());
    }

    public void testReadElementWithDurationUnitAsFortnight() throws Exception {
        Element elt = createPeriodElement();
        elt.addAttribute("duration-unit", "fortnight");

        Period actual = (Period) serializer.readElement(elt);
        assertEquals("Wrong period duration unit", "fortnight", actual.getDuration().getUnit().toString());
    }

    public void testReadElementWithDurationUnitAsQuarter() throws Exception {
        Element elt = createPeriodElement();
        elt.addAttribute("duration-unit", "quarter");

        Period actual = (Period) serializer.readElement(elt);
        assertEquals("Wrong period duration unit", "quarter", actual.getDuration().getUnit().toString());
    }

    public void testThrowValidationExceptionForUnreginizedDurationUnit() throws Exception {
        Element invalidElt = createPeriodElement();
        invalidElt.addAttribute("duration-unit", "unknown");
        try {
            serializer.readElement(invalidElt);
            fail("Exception not thrown");
        } catch (StudyImportException sie) {
            assertEquals("Unknown Duration Unit unknown",
                sie.getMessage());
        }
    }

    public void testValidateElement() throws Exception {
        Period period = createPeriod();
        Element actual = serializer.createElement(period);
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.setGridId("wrong grid id");
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.setDuration(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.getDuration().setQuantity(5);
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.setDuration(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.getDuration().setUnit(Duration.Unit.fortnight);
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.setName("wrong name");
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.setStartDay(7);
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));

    }

    public void testGetPeriodWithMatchingAttributes() throws Exception {
        Element actual = serializer.createElement(period);

        Period period = createPeriod();
        assertNotNull(serializer.getPeriodWithMatchingGridId(periods, actual));
        assertEquals("serializer must have a matching period", period, serializer.getPeriodWithMatchingGridId(periods, actual));
        period.setGridId("wrong grid id");
        assertNull(serializer.getPeriodWithMatchingGridId(periods, actual));
    }

    private Period createPeriod() {
        Period period = setGridId("grid0", Fixtures.createPeriod("Period A", 1, 7, 3));
        periods.clear();
        periods.add(period);
        return period;
    }

    // Test Helper Method
    private Element createPeriodElement() {
        Element elt = XsdElement.PERIOD.create();;
        elt.addAttribute("id", "grid0");
        elt.addAttribute("name", "Period A");
        elt.addAttribute("duration-quantity", "7");
        elt.addAttribute("start-day", "1");
        elt.addAttribute("repetitions", "3");
        return elt;
    }
}
