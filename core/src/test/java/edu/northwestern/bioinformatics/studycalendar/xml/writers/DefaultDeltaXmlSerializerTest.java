/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaNodeType;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Collections;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setGridId;
import static org.easymock.EasyMock.expect;

public class DefaultDeltaXmlSerializerTest extends StudyCalendarXmlTestCase {
    private DeltaXmlSerializer serializer;
    private Delta plannedCalendarDelta;
    private Element element;
    private PlannedCalendar calendar;
    private ChangeXmlSerializerFactory changeSerializerFactory;
    private Add add;
    private ChangeXmlSerializer changeSerializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        changeSerializerFactory = registerMockFor(ChangeXmlSerializerFactory.class);
        changeSerializer = registerMockFor(AbstractChangeXmlSerializer.class);

        calendar = setGridId("grid1", new PlannedCalendar());
        serializer =
            DefaultDeltaXmlSerializer.create(DeltaNodeType.PLANNED_CALENDAR, changeSerializerFactory);

        add = new Add();
        plannedCalendarDelta = Delta.createDeltaFor(calendar, add);
        plannedCalendarDelta.setGridId("grid0");
    }

    public void testCreateElement() {
        expect(changeSerializerFactory.createXmlSerializer(add)).andReturn(changeSerializer);
        expect(changeSerializer.createElement(add)).andReturn(DocumentHelper.createElement("add"));
        replayMocks();

        Element element = serializer.createElement(plannedCalendarDelta);
        verifyMocks();

        assertEquals("Wrong element name", "planned-calendar-delta", element.getName());
        assertEquals("Wrong node id", "grid1", element.attributeValue("node-id"));
        assertEquals("Wrong id", "grid0", element.attributeValue("id"));
    }

    public void testReadElementWithNewAmendment() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.attributeValue("node-id")).andReturn("grid1");
        expect(element.elements()).andReturn(Collections.emptyList());
        replayMocks();

        Delta actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong delta grid id", "grid0", actual.getGridId());
        assertEquals("Wrong delta grid id", "grid1", actual.getNode().getGridId());

    }
}
