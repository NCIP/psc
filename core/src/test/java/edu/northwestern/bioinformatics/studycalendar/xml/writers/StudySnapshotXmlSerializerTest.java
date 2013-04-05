/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.addSecondaryIdentifier;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createPopulation;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import static org.dom4j.DocumentHelper.createElement;
import static org.easymock.EasyMock.expect;

import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
public class StudySnapshotXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudySnapshotXmlSerializer serializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer
            = (StudySnapshotXmlSerializer) getDeployedApplicationContext().getBean("studySnapshotXmlSerializer");
    }

    public void testAssignedIdentifierRequired() throws Exception {
        try {
            doParse(createRootElementString("study-snapshot", null, true));
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException expected) {
            assertEquals("Wrong error message",
                "assigned-identifier is required for study-snapshot", expected.getMessage());
        }
    }

    public void testReadWithNoChildren() throws Exception {
        String expectedIdent = "ASSIGNED";
        Study noChildren = doParse(createRootElementString("study-snapshot", "assigned-identifier='%s'", true), expectedIdent);
        assertNotNull("No study returned", noChildren);
        assertEquals("Study does not have ident", expectedIdent, noChildren.getAssignedIdentifier());
        assertNotNull("Study doesn't have planned calendar", noChildren.getPlannedCalendar());
        assertEquals("Should have no populations: " + noChildren.getPopulations(), 0, noChildren.getPopulations().size());
    }

    public void testReadWithPlannedCalendar() throws Exception {
        Study actual = doParse(
            "%s<planned-calendar><epoch name='Treatment'><study-segment name='Treatment'/></epoch></planned-calendar></study-snapshot>",
            createRootElementString("study-snapshot", "assigned-identifier='foom'", false));
        assertNotNull(actual);
        assertNotNull(actual.getPlannedCalendar());
        assertEquals("Wrong number of epochs", 1, actual.getPlannedCalendar().getEpochs().size());
        Epoch actualEpoch = actual.getPlannedCalendar().getEpochs().get(0);
        assertEquals("Wrong epoch name", "Treatment", actualEpoch.getName());
        assertEquals("Wrong number of segments", 1, actualEpoch.getStudySegments().size());
        assertEquals("Wrong name for study segment", "Treatment",
            actualEpoch.getStudySegments().get(0).getName());
        assertEquals("Should have no periods", 0, actualEpoch.getStudySegments().get(0).getPeriods().size());
    }

    public void testReadWithUnusedPopulations() throws Exception {
        Study actual = doParse(
            "%s<population name='Elderly' abbreviation='E'/><population name='Fair' abbreviation='F'/></study-snapshot>",
            createRootElementString("study-snapshot", "assigned-identifier='foom'", false));
        assertNotNull(actual);
        assertNotNull(actual.getPlannedCalendar());
        assertNotNull(actual.getPopulations());
        assertEquals("Wrong number of populations", 2, actual.getPopulations().size());
        Iterator<Population> populations = actual.getPopulations().iterator();
        Population first = populations.next();
        Population second = populations.next();
        assertEquals("Wrong name for first pop", "Elderly", first.getName());
        assertEquals("Wrong abbrev for first pop", "E", first.getAbbreviation());
        assertEquals("Wrong name for second pop", "Fair", second.getName());
        assertEquals("Wrong abbrev for second pop", "F", second.getAbbreviation());
    }

    public void testReadElementWithSecondaryIdentifiers() throws Exception {
        StudySecondaryIdentifierXmlSerializer xmlSerializer =
                registerMockFor(StudySecondaryIdentifierXmlSerializer.class);
        serializer.setStudySecondaryIdentifierXmlSerializer(xmlSerializer);
        Study study = createNamedInstance("Study A", Study.class);
        Element eStudy = createElement("study");
        eStudy.addAttribute("assigned-identifier", "Id1");
        Element eIdentifier = DocumentHelper.createElement("secondary-identifier");
        eStudy.add(eIdentifier);
        StudySecondaryIdentifier identifier = addSecondaryIdentifier(study, "Type1", "Value1");
        expect(xmlSerializer.readElement(eIdentifier)).andReturn(identifier);
        replayMocks();
        Study actual = serializer.readElement(eStudy);
        verifyMocks();
        assertNotNull(actual);
        assertNotNull(actual.getSecondaryIdentifiers());
        assertEquals("Wrong number of secondary identifiers", 1, actual.getSecondaryIdentifiers().size());
        assertEquals("Wrong type for first secondary identifier", "Type1", actual.getSecondaryIdentifiers().first().getType());
        assertEquals("Wrong value for first secondary identifier", "Value1", actual.getSecondaryIdentifiers().first().getValue());
    }

    public void testReadElementWithLongTitle() throws Exception {
        Element eltStudy = createElement("study");
        eltStudy.addAttribute("assigned-identifier", "Id1");
        Element eltLongTitle = DocumentHelper.createElement("long-title");
        eltLongTitle.addText("study long title");
        eltStudy.add(eltLongTitle);
        replayMocks();

        Study actual = serializer.readElement(eltStudy);
        verifyMocks();
        assertEquals("Wrong Long title name", eltLongTitle.getText(), actual.getLongTitle() );
    }

    public void testReadElementLongTitleNormalizeWhiteSpace() throws Exception {
        Element eltStudy = createElement("study");
        eltStudy.addAttribute("assigned-identifier", "Id1");
        Element eltLongTitle = DocumentHelper.createElement("long-title");
        eltLongTitle.addText("\n  study\n long\t title\n  ");
        eltStudy.add(eltLongTitle);
        replayMocks();

        Study actual = serializer.readElement(eltStudy);
        verifyMocks();
        assertEquals("No normalize of whitespaces for long title", "study long title", actual.getLongTitle() );
    }

//    public void testReadMatchesPopulationsAsAppropriate() throws Exception {
//        Study actual = doParse(
//            "%s<planned-calendar><epoch name='Treatment'><study-segment name='Treatment'><period duration-quantity='4' duration-unit='day' repetitions='1' start-day='1'>" +
//                "<planned-activity day='1' population='E'><activity code='Questionnaire' source='Spring' type='name' type-id='4'/></planned-activity>" +
//                "<planned-activity day='3'><activity code='Questionnaire' source='Spring' type='name' type-id='4'/></planned-activity>" +
//            "</period></study-segment></epoch></planned-calendar>" +
//            "<population name='Elderly' abbreviation='E'/>" +
//            "</study-snapshot>",
//            createRootElementString("study-snapshot", "assigned-identifier='foom'", false));
//        assertNotNull(actual);
//        assertNotNull(actual.getPlannedCalendar());
//        assertNotNull(actual.getPopulations());
//        assertEquals("Wrong number of populations", 1, actual.getPopulations().size());
//        Population actualPopulation = actual.getPopulations().iterator().next();
//        Period actualPeriod = actual.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).getPeriods().iterator().next();
//        assertNotNull("Missing period", actualPeriod);
//        assertEquals("Wrong number of planned activities", 2, actualPeriod.getPlannedActivities().size());
//        PlannedActivity first = actualPeriod.getPlannedActivities().get(0);
//        assertEquals("First activity not as expected", 1, (int) first.getDay());
//        assertSame("Pop not mapped onto the first activity", actualPopulation, first.getPopulation());
//        PlannedActivity second = actualPeriod.getPlannedActivities().get(1);
//        assertEquals("Second activity not as expected", 3, (int) second.getDay());
//        assertNull("Second activity should not have population", second.getPopulation());
//    }

    public void testCreateElement() {
        Study study = createNamedInstance("Study A", Study.class);
        study.addPopulation(createPopulation("DMP", "Disease Measure Population"));
        study.addPopulation(createPopulation("TP", "Therapy Population"));
        study.setPlannedCalendar(new PlannedCalendar());

        Element elt = serializer.createElement(study);

        assertEquals("Wrong name", "study-snapshot", elt.getName());
        assertEquals("Wrong number of populations", 2, elt.elements("population").size());
        assertEquals("Wrong number of planned calendars", 1, elt.elements("planned-calendar").size());
    }
    
    public void testCreateElementWithProvider() throws Exception {
        Study study = createNamedInstance("Study A", Study.class);
        study.setProvider("study-provider");
        study.setPlannedCalendar(new PlannedCalendar());
        Element elt = serializer.createElement(study);
        assertNotNull(elt.attribute("provider"));
        assertEquals("Wrong provider", "study-provider", elt.attributeValue("provider"));
    }

    public void testCreateElmentWithSecondaryIdentifiers() throws Exception {
        StudySecondaryIdentifierXmlSerializer xmlSerializer =
                registerMockFor(StudySecondaryIdentifierXmlSerializer.class);
        serializer.setStudySecondaryIdentifierXmlSerializer(xmlSerializer);
        Study study = createNamedInstance("Study A", Study.class);
        study.setPlannedCalendar(new PlannedCalendar());
        StudySecondaryIdentifier identifier = addSecondaryIdentifier(study, "Type1", "ident1");
        Element eIdentifier = DocumentHelper.createElement("secondary-identifier");
        expect(xmlSerializer.createElement(identifier)).andReturn(eIdentifier);

        replayMocks();
        Element actual = serializer.createElement(study);
        verifyMocks();
        assertNotNull("Secondary Identifier should exist", actual.element("secondary-identifier"));
    }

    public void testCreateElementWithLongTitle() throws Exception {
        Study study = createNamedInstance("Study A", Study.class);
        study.setPlannedCalendar(new PlannedCalendar());
        study.setLongTitle("Study Long Title");
        replayMocks();
        Element actual = serializer.createElement(study);
        verifyMocks();
        Element eltLongTitle = actual.element("long-title");
        assertNotNull("Long title should exist", eltLongTitle);
        assertEquals("Long title name does not match", study.getLongTitle(), eltLongTitle.getText());
    }

    private Study doParse(String xml, String... formatValues) {
        String doc = String.format(xml, (Object[]) formatValues);
        log.debug("about to parse\n{}", doc);
        return parseDocumentString(serializer, doc);
    }
}
