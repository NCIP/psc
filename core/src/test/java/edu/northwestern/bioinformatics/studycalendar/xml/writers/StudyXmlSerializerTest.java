package edu.northwestern.bioinformatics.studycalendar.xml.writers;


import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.STUDY;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static java.util.Arrays.asList;
import static org.dom4j.DocumentHelper.createElement;
import static org.dom4j.DocumentHelper.createQName;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;

public class StudyXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudyXmlSerializer serializer;
    private Study study;
    private Element element;
    private PlannedCalendar calendar;
    private ActivitySourceXmlSerializer activitySourceSerializer;
    private PlannedCalendarXmlSerializer plannedCalendarSerializer;
    private AmendmentXmlSerializer amendmentSerializer;
    private AmendmentXmlSerializer developmentAmendmentSerializer;
    private Amendment firstAmendment;
    private Amendment developmentAmendment;
    private Element eFirstAmendment;
    private Element eDevelopmentAmendment;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
    private Element eCalendar;
    private Amendment secondAmendment;
    private Element eSecondAmendment;

    private Collection<Source> sources;
    private Element eSources;


    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        activitySourceSerializer = registerMockFor(ActivitySourceXmlSerializer.class);
        amendmentSerializer = registerMockFor(AmendmentXmlSerializer.class);
        plannedCalendarSerializer = registerMockFor(PlannedCalendarXmlSerializer.class);
        developmentAmendmentSerializer = registerMockFor(AmendmentXmlSerializer.class);

        serializer = new StudyXmlSerializer() {
            protected PlannedCalendarXmlSerializer getPlannedCalendarXmlSerializer(Study study) {
                return plannedCalendarSerializer;
            }

            public AmendmentXmlSerializer getAmendmentSerializer(Study study) {
                return amendmentSerializer;
            }

            protected AmendmentXmlSerializer getDevelopmentAmendmentSerializer(Study study) {
                return developmentAmendmentSerializer;
            }
        };

        serializer.setActivitySourceXmlSerializer(activitySourceSerializer);

        calendar = setGridId("grid1", new PlannedCalendar());
        firstAmendment = createAmendment("[First]", createDate(2008, Calendar.JANUARY, 2), true);

        secondAmendment = createAmendment("[Second]", createDate(2008, Calendar.JANUARY, 11), true);
        secondAmendment.setPreviousAmendment(firstAmendment);

        developmentAmendment = createAmendment("[Development]", createDate(2008, Calendar.FEBRUARY, 13), true);
        developmentAmendment.setReleasedDate(null);

        sources = asList(createNamedInstance("nu-activities", Source.class));

        study = createStudy();

        eCalendar = createCalendarElement(calendar);
        eFirstAmendment = createAmendmentElement(firstAmendment);
        eSecondAmendment = createAmendmentElement(secondAmendment);
        eDevelopmentAmendment = createDevelopmentAmendmentElement(developmentAmendment);
        eSources = createSourcesElement(sources);
    }

    public void testReadElementWhereElementIsNew() {
        expectDeserializeCommonDataForNewElement();
        replayMocks();

        Study actual = serializer.readElement(createStudyElement(), study);
        verifyMocks();

        assertEquals("Wrong assigned identifier", "Study A", actual.getAssignedIdentifier());
        assertSame("PlannedCalendar should be the same", calendar, actual.getPlannedCalendar());
        assertSame("Wrong first amendment", firstAmendment, actual.getAmendment().getPreviousAmendment());
        assertSame("Wrong second amendment", secondAmendment, actual.getAmendment());
        assertSame("Wrong development amendment", developmentAmendment, actual.getDevelopmentAmendment());
    }

    public void testReadElementWithProviderForNewElement() throws Exception {
        Element elt = createStudyElement();
        elt.addAttribute("provider", "study-provider");
        expectDeserializeCommonDataForNewElement();
        replayMocks();

        Study actual = serializer.readElement(elt, new Study());
        verifyMocks();
        assertNotNull(actual.getProvider());
        assertEquals("Wrong provider", "study-provider", actual.getProvider());

    }

    public void testReadElementWithSecondaryIdentifier() throws Exception {
        StudySecondaryIdentifierXmlSerializer xmlSerializer =
                expectSecondaryIdentifierSerializer();
        Element eltStudy = createStudyElement();
        Element eIdentifier = DocumentHelper.createElement("secondary-identifier");
        eltStudy.add(eIdentifier);
        StudySecondaryIdentifier identifier = addSecondaryIdentifier(study, "NewType", "NewValue");
        expectDeserializeCommonDataForNewElement();
        expect(xmlSerializer.readElement(eIdentifier)).andReturn(identifier);
        replayMocks();

        Study actual = serializer.readElement(eltStudy, study);
        verifyMocks();
        assertNotNull(actual.getSecondaryIdentifiers());
        assertEquals("Wrong identifier type", "NewType", actual.getSecondaryIdentifiers().first().getType());
        assertEquals("Wrong identifier value", "NewValue", actual.getSecondaryIdentifiers().first().getValue());
    }

    public void testReadElementWithLongTitle() throws Exception {
        Element eltStudy = createStudyElement();
        Element eltLongTitle = DocumentHelper.createElement("long-title");
        eltLongTitle.addText("study long title");
        eltStudy.add(eltLongTitle);
        expectDeserializeCommonDataForNewElement();
        replayMocks();

        Study actual = serializer.readElement(eltStudy, study);
        verifyMocks();
        assertEquals("Wrong Long title name", eltLongTitle.getText(), actual.getLongTitle() );
    }

    public void testReadElementLongTitleWithNormalizeWhitespace() throws Exception {
        Element eltStudy = createStudyElement();
        Element eltLongTitle = DocumentHelper.createElement("long-title");
        eltLongTitle.addText("\n  study \nlong \ttitle\r  ");
        eltStudy.add(eltLongTitle);
        expectDeserializeCommonDataForNewElement();
        replayMocks();

        Study actual = serializer.readElement(eltStudy, study);
        verifyMocks();
        assertEquals("No normalize of whitespace characters for long title", "study long title", actual.getLongTitle() );
    }

    public void testReadElementWithInvalidElementName() {
        expect(element.getName()).andReturn("study-snapshot").times(2);
        try {
            replayMocks();
            serializer.readElement(element, new Study());
            fail("Exception should be thrown");
            verifyMocks();
        } catch (StudyCalendarValidationException e) {
            assertEquals("Element type is other than <study>", e.getMessage());
        }
    }

    public void testReadElementWithNoDevelopmentOrReleasedAmendments() {
        Element eStudy = createStudyElement();
        eStudy.remove(eFirstAmendment);
        eStudy.remove(eSecondAmendment);
        eStudy.remove(eDevelopmentAmendment);

        try {
            replayMocks();
            serializer.readElement(eStudy, new Study());
            fail("Exception should be thrown");
            verifyMocks();
        } catch (StudyCalendarValidationException e) {
            assertEquals("<study> must have at minimum an <amendment> or <development-amendment> child", e.getMessage());
        }
    }

    public void testCreateElement() {
        expectChildrenSerializers();
        replayMocks();

        Element actual = serializer.createElement(study);
        verifyMocks();

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong assigned identifier", "Study A", actual.attribute("assigned-identifier").getValue());
        assertEquals("Wrong last modified date", dateTimeFormat.format(study.getLastModifiedDate()), actual.attribute("last-modified-date").getValue());

        assertEquals("Should have planned calendar child and development-amendment", 4, actual.nodeCount());
        assertNotNull("Planned calendar should exist", actual.element("planned-calendar"));
        assertNotNull("Amendment should exist", actual.element("amendment"));
        assertNotNull("Development Amendment should exist", actual.element("development-amendment"));
    }

    public void testCreateElementWithProvider() throws Exception {
        study.setProvider("study-provider");
        expectChildrenSerializers();
        replayMocks();

        Element actual = serializer.createElement(study);
        verifyMocks();
        assertEquals("Wrong attribute size", 3, actual.attributeCount());
        assertEquals("Wrong provider", "study-provider", actual.attributeValue("provider"));

    }

    public void testCreateElementWithSecondaryIdentifiers() throws Exception {
        StudySecondaryIdentifierXmlSerializer xmlSerializer =
                expectSecondaryIdentifierSerializer();
        StudySecondaryIdentifier identifier = addSecondaryIdentifier(study, "Type1", "ident1");
        Element eIdentifier = DocumentHelper.createElement("secondary-identifier");
        expectChildrenSerializers();
        expect(xmlSerializer.createElement(identifier)).andReturn(eIdentifier);

        replayMocks();
        Element actual = serializer.createElement(study);
        verifyMocks();
        assertNotNull("Secondary Identifier should exist", actual.element("secondary-identifier"));
    }

    public void testCreateElementWithLongTitle() throws Exception {
        study.setLongTitle("Study Long Title");
        expectChildrenSerializers();
        replayMocks();
        Element actual = serializer.createElement(study);
        verifyMocks();
        Element eltLongTitle = actual.element("long-title");
        assertNotNull("Long title should exist", eltLongTitle);
        assertEquals("Long title name does not match", study.getLongTitle(), eltLongTitle.getText());
    }

    public void testCreateElementWithSources() throws Exception {
        expectChildrenSerializers();
        replayMocks();
        Element actual = serializer.createElement(study);
        verifyMocks();
        assertNotNull("Sources should exist", actual.element("sources"));
    }

    public void testCreateDocumentString() throws Exception {

        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("<study ");
        expected.append(MessageFormat.format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2}\"", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));
        expected.append(MessageFormat.format("       {0}=\"{1}\"", "assigned-identifier", "Study A"));
        expected.append(MessageFormat.format("       {0}=\"{1}\"", "last-modified-date", dateTimeFormat.format(study.getLastModifiedDate())));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2} {3}\">", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));

        expected.append(MessageFormat.format("<planned-calendar id=\"{0}\"/>", calendar.getGridId()));
        expected.append(MessageFormat.format("<amendment name=\"{0}\" date=\"{1}\" mandatory=\"{2}\" released-date=\"{3}\" />", firstAmendment.getName()
                , formatter.format(firstAmendment.getDate()), String.valueOf(firstAmendment.isMandatory()), dateTimeFormat.format(firstAmendment.getReleasedDate())));
        expected.append(MessageFormat.format("<development-amendment name=\"{0}\" date=\"{1}\" mandatory=\"{2}\"/>",
            developmentAmendment.getName(), formatter.format(developmentAmendment.getDate()), String.valueOf(developmentAmendment.isMandatory())));
        expected.append("<sources>");
        expected.append(MessageFormat.format("<source name=\"{0}\"/>", sources.iterator().next().getName()));
        expected.append("</sources>");

        expected.append("</study>");

        expectChildrenSerializers();

        replayMocks();

        String actual = serializer.createDocumentString(study);
        log.debug("actual:" + actual);
        log.debug("expected:" + expected);
        verifyMocks();

        assertXMLEqual(expected.toString(), actual);
    }

    public void testGroupActivitiesBySource() throws Exception {
        Source nu = createNamedInstance("nu-activities", Source.class);
        Source na = createNamedInstance("na-activities", Source.class);

        Activity cbc = createActivity("cbc");
        Activity bbc = createActivity("bbc");
        Activity nbc = createActivity("nbc");

        cbc.setSource(nu);
        bbc.setSource(na);
        nbc.setSource(na);

        Collection<Source> actual = serializer.groupActivitiesBySource(asList(cbc, bbc, nbc));

        assertEquals("Wrong size", 2, actual.size());
        assertContains(actual, nu);
        assertContains(actual, na);

        List<Source> sorted = new ArrayList<Source>(actual);
        Collections.sort(sorted, NamedComparator.INSTANCE);

        Source actualNa = sorted.get(0);
        Source actualNu = sorted.get(1);

        assertTrue("Should be transient", actualNa.isMemoryOnly());
        assertTrue("Should be transient", actualNu.isMemoryOnly());

        assertEquals("Wrong size", 1, actualNu.getActivities().size());
        assertContains(actualNu.getActivities(), cbc);

        assertEquals("Wrong size", 2, actualNa.getActivities().size());
        assertContains(actualNa.getActivities(), bbc);
        assertContains(actualNa.getActivities(), nbc);

        assertTrue("Should be transient", actualNu.getActivities().get(0).isMemoryOnly());
        assertTrue("Should be transient", actualNa.getActivities().get(0).isMemoryOnly());
        assertTrue("Should be transient", actualNa.getActivities().get(1).isMemoryOnly());
    }

    ////// Expect helper methods

    private void expectChildrenSerializers() {
        expect(plannedCalendarSerializer.createElement(calendar)).andReturn(eCalendar);
        expect(amendmentSerializer.createElement(firstAmendment)).andReturn(eFirstAmendment);
        expect(developmentAmendmentSerializer.createElement(developmentAmendment)).andReturn(eDevelopmentAmendment);
        expect(activitySourceSerializer.createElement((Collection) notNull())).andReturn(eSources);

    }

    private void expectDeserializePlannedCalendar() {
        expect(plannedCalendarSerializer.readElement(eCalendar)).andReturn(calendar);
    }

    private void expectDesearializeDevelopmentAmendment() {
        expect(developmentAmendmentSerializer.readElement(eDevelopmentAmendment)).andReturn(developmentAmendment);
    }

    private void expectDeserializeAmendments() {
        expect(amendmentSerializer.readElement(eFirstAmendment)).andReturn(firstAmendment);
        expect(amendmentSerializer.readElement(eSecondAmendment)).andReturn(secondAmendment);
    }

    private void expectDeserializeCommonDataForNewElement(){
        expectDeserializePlannedCalendar();
        expectDeserializeAmendments();
        expectDesearializeDevelopmentAmendment();
    }

    private StudySecondaryIdentifierXmlSerializer expectSecondaryIdentifierSerializer() {
        StudySecondaryIdentifierXmlSerializer xmlSerializer =
                registerMockFor(StudySecondaryIdentifierXmlSerializer.class);
        serializer.setStudySecondaryIdentifierXmlSerializer(xmlSerializer);
        return xmlSerializer;
    }

    ////// Element Creation Helpers

    private Element createStudyElement() {
        QName qStudy = createQName(STUDY.xmlName(), AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
        Element eStudy = createElement(qStudy);
        eStudy.addAttribute("assigned-identifier", study.getAssignedIdentifier());
        eStudy.addAttribute("last-modified-date", formatter.format(study.getLastModifiedDate()));

        eStudy.add(eCalendar);

        // Amendments are added in this order to test unordered deserialization
        eStudy.add(eSecondAmendment);
        eStudy.add(eFirstAmendment);
        eStudy.add(eDevelopmentAmendment);
        return eStudy;
    }

    private Element createDevelopmentAmendmentElement(Amendment developmentAmendment) {
        AmendmentXmlSerializer s = new AmendmentXmlSerializer();
        s.setDevelopmentAmendment(true);
        return s.createElement(developmentAmendment);
    }

    private Element createAmendmentElement(Amendment amendment) {
        AmendmentXmlSerializer s = new AmendmentXmlSerializer();
        return s.createElement(amendment);
    }

    private Element createCalendarElement(PlannedCalendar calendar) {
        PlannedCalendarXmlSerializer s = new PlannedCalendarXmlSerializer();
        return s.createElement(calendar);
    }

    private Element createSourcesElement(Collection<Source> sources) {
        ActivitySourceXmlSerializer s = new ActivitySourceXmlSerializer();
        return s.createElement(sources);
    }

    private StudySegment createSegment(Period... periods) {
        StudySegment result = new StudySegment();
        for (Period p : periods) {
            result.addPeriod(p);
        }
        return result;
    }

    ////// Create PSC object helpers
    private Study createStudy() {
        Study created = createNamedInstance("Study A", Study.class);
        created.setPlannedCalendar(calendar);
        created.setAmendment(firstAmendment);
        created.setDevelopmentAmendment(developmentAmendment);
        return created;
    }
}
