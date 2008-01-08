package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static org.easymock.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator.TEMPLATE_VALIDATOR_INSTANCE;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriterTest.StudyXMLSkeleton.insertXml;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.SpringDaoFinder;
import edu.nwu.bioinformatics.commons.StringUtils;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import static org.springframework.validation.ValidationUtils.invokeValidator;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.BeansException;
import org.xml.sax.SAXException;
import org.easymock.EasyMock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import static java.lang.String.valueOf;
import static java.text.MessageFormat.format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StudyXMLWriterTest extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private StudyXMLWriter writer;
    private DaoFinder daoFinder;
    DomainObjectDao<?> daoMock;

    private Study study;

    private int id = 1;
    private Amendment amendment;
    private Delta<PlannedCalendar> calendarDelta;
    private Delta<PlannedCalendar> calendarDeltaForRemove;
    private Delta<PlannedCalendar> calendarDeltaForReorder;
    private Delta<Epoch> epochDeltaForPropertyChange;
    private Delta<Epoch> epochDelta;
    private Delta<StudySegment> segmentDelta;
    private Delta<PlannedActivity> periodDelta;
    private Add addEpoch;
    private Add addSegment;
    private Add addPeriod;
    private Add addActivity;
    private Remove removeEpoch;
    private Reorder reorderEpoch;
    private PropertyChange epochPropertyChange;
    private Epoch epoch;
    private StudySegment segment;
    private Period period;
    private PlannedActivity plannedActivity;
    private Activity activity;
    private Source source;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        daoMock = registerMockFor(DomainObjectDao.class);
        daoFinder = new TestingSpringDaoFinder(daoMock);

        XMLUnit.setIgnoreWhitespace(true);

        writer = new StudyXMLWriter(daoFinder);

        amendment = createAmendment();

        study = createStudy("Study A");
        study.setAmendment(amendment);

        /* Planned Calendar Delta for Add(ing) an Epoch */
        epoch = createNamedInstance("Epoch A", Epoch.class);
        addEpoch = createAdd(epoch, 0);
        calendarDelta = createDeltaFor(study.getPlannedCalendar(), addEpoch);

        /* Epoch Delta for Add(ing) a StudySegment */
        segment = createNamedInstance("Segment A", StudySegment.class);
        addSegment = createAdd(segment, 0);
        epochDelta = createDeltaFor(epoch, addSegment);

        /* Study Segment Delta for Add(ing) Periods */
        period = createNamedInstance("Period A", Period.class);
        addPeriod = createAdd(period);
        segmentDelta = createDeltaFor(segment, addPeriod);

        /* Period Delta for Add(ing) Planned Activities */
        source = createNamedInstance("LOINK Source", Source.class);
        activity = createActivity("Bone Scan", "AA", ActivityType.DISEASE_MEASURE, "make sure im not broken");
        activity.setSource(source);
        plannedActivity = createPlannedActivity("Bone Scan", 1, "details", "patient is male");
        plannedActivity.setActivity(activity);
        addActivity = createAdd(plannedActivity, 0);
        periodDelta = createDeltaFor(plannedActivity, addActivity);

        /* Planned Calendar Delta for Remove(ing) an Epoch */
        removeEpoch = createRemove(epoch);
        calendarDeltaForRemove = createDeltaFor(study.getPlannedCalendar(), removeEpoch);

        /* Planned Calendar Delta for Reorder(ing) an Epoch */
        reorderEpoch = createReorder(epoch, 0, 1);
        calendarDeltaForReorder = createDeltaFor(study.getPlannedCalendar(), reorderEpoch);

        /* Epoch Delta for an Epoch Property Change */
        epochPropertyChange = createPropertyChange("name", "Epoch A", "Epoch B");
        epochDeltaForPropertyChange = createDeltaFor(epoch, epochPropertyChange);
    }

    public void testWriteEpoch() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml();

        assertXMLEqual(expected, output);
    }

    private void assertXMLEqual(String expected, String actual) throws SAXException, IOException {
        // XMLUnit's whitespace stripper stopped working at r1976 (of PSC) or so
        // This is a quick-and-dirty (and not necessarily correct) alternative
        String expectedNormalized = StringUtils.normalizeWhitespace(expected);
        String actualNormalized = StringUtils.normalizeWhitespace(actual);
        XMLAssert.assertXMLEqual(expectedNormalized, actualNormalized);
    }

    public void testWriteStudySegment() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( epochDeltaXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml();

        assertXMLEqual(expected, output);
    }

    public void testWritePeriod() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( epochDeltaXML())
            .append( segmentDeltaXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml();

        assertXMLEqual(expected, output);
    }


     public void testWritePlannedActivity() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( epochDeltaXML())
            .append( segmentDeltaXML())
            .append( periodDeltaXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml();

         assertXMLEqual(expected, output);
     }


    public void testWritePlannedActivityWithoutDetailsAndCondition() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
                .append( epochDeltaXML())
                .append( segmentDeltaXML())
                .append( periodDeltaXMLWithoutDetailsAndCondition());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml();

        assertXMLEqual(expected, output);
    }

    public void testWriteRemoveEpoch() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( calendarDeltaRemoveEpochXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml();

        assertXMLEqual(expected, output);
    }

    public void testWriteReorderEpochs() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( calendarDeltaReorderEpochXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml();

        assertXMLEqual(expected.trim(), output.trim());
    }

    public void testWritePlannedCalendarPropertyChange() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( epochDeltaXML())
            .append( epochDeltaPropertyChangeXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml();

        log.debug("Expected:\n{}", expected);
        log.debug("Output:\n{}", output);

        assertXMLEqual(expected, output);
    }


    /* Output XML Methods */
    private String calendarDeltaXML() {
        amendment.addDelta(calendarDelta);
        
        expect(daoMock.getById(epoch.getId())).andReturn(epoch);

        StringBuffer body = new StringBuffer();
        body.append(    format("<delta id=\"{0}\" node-id=\"{1}\">\n", calendarDelta.getGridId(), calendarDelta.getNode().getGridId()))
                .append(format("  <add id=\"{0}\" index=\"{1}\">\n", addEpoch.getGridId(), addEpoch.getIndex()))
                .append(format("    <epoch id=\"{0}\" name=\"{1}\"/>\n", epoch.getGridId(), epoch.getName()))
                .append(       "  </add>\n")
                .append(       "</delta>\n");
        return body.toString();
    }

    private String epochDeltaXML() {
        amendment.addDelta(epochDelta);

        expect(daoMock.getById(segment.getId())).andReturn(segment);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">\n", epochDelta.getGridId(), epochDelta.getNode().getGridId()))
            .append(format("  <add id=\"{0}\" index=\"{1}\">\n", addSegment.getGridId(), addSegment.getIndex()))
            .append(format("    <study-segment id=\"{0}\" name=\"{1}\"/>\n", segment.getGridId(), segment.getName()))
            .append(       "  </add>\n")
            .append(       "</delta>\n");

        return body.toString();
    }

    private String segmentDeltaXML() {
        amendment.addDelta(segmentDelta);

        expect(daoMock.getById(period.getId())).andReturn(period);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">\n", segmentDelta.getGridId(), segmentDelta.getNode().getGridId()))
            .append(format("  <add id=\"{0}\">\n", addPeriod.getGridId()))
            .append(format("    <period id=\"{0}\" name=\"{1}\"/>\n", period.getGridId(), period.getName()))
            .append(       "  </add>\n")
            .append(       "</delta>\n");

        return body.toString();
    }

    private String periodDeltaXML() {
        amendment.addDelta(periodDelta);

        expect(daoMock.getById(plannedActivity.getId())).andReturn(plannedActivity);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">\n", periodDelta.getGridId(), periodDelta.getNode().getGridId()))
            .append(format("  <add id=\"{0}\" index=\"{1}\">\n", addActivity.getGridId(), addActivity.getIndex()))
            .append(format("    <planned-activity id=\"{0}\" day=\"{1}\" details=\"{2}\" condition=\"{3}\" >\n", plannedActivity.getGridId(), plannedActivity.getDay(), plannedActivity.getDetails(), plannedActivity.getCondition()))
            .append(format("      <activity id=\"{0}\" name=\"{1}\" description=\"{2}\" type-id=\"{3}\" code=\"{4}\">\n", activity.getGridId(), activity.getName(), activity.getDescription(), activity.getType().getId(), activity.getCode()))
            .append(format("        <source id=\"{0}\" name=\"{1}\"/>\n", source.getGridId(), source.getName()))
            .append(       "      </activity>\n")
            .append(       "    </planned-activity>\n")
            .append(       "  </add>\n")
            .append(       "</delta>\n");

        return body.toString();
    }

    private String periodDeltaXMLWithoutDetailsAndCondition() {
        plannedActivity.setDetails(null);
        plannedActivity.setCondition(null);

        amendment.addDelta(periodDelta);

        expect(daoMock.getById(plannedActivity.getId())).andReturn(plannedActivity);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">\n", periodDelta.getGridId(), periodDelta.getNode().getGridId()))
            .append(format("  <add id=\"{0}\" index=\"{1}\">\n", addActivity.getGridId(), addActivity.getIndex()))
            .append(format("    <planned-activity id=\"{0}\" day=\"{1}\" >\n", plannedActivity.getGridId(), plannedActivity.getDay()))
            .append(format("      <activity id=\"{0}\" name=\"{1}\" description=\"{2}\" type-id=\"{3}\" code=\"{4}\">\n", activity.getGridId(), activity.getName(), activity.getDescription(), activity.getType().getId(), activity.getCode()))
            .append(format("        <source id=\"{0}\" name=\"{1}\"/>\n", source.getGridId(), source.getName()))
            .append(       "      </activity>\n")
            .append(       "    </planned-activity>\n")
            .append(       "  </add>\n")
            .append(       "</delta>\n");

        return body.toString();
    }

    private String calendarDeltaRemoveEpochXML() {
        amendment.addDelta(calendarDeltaForRemove);

        expect(daoMock.getById(epoch.getId())).andReturn(epoch);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">\n", calendarDeltaForRemove.getGridId(), calendarDeltaForRemove.getNode().getGridId()))
            .append(format("  <remove id=\"{0}\" child-id=\"{1}\"/>\n", removeEpoch.getGridId(), removeEpoch.getChild().getGridId()))
            .append(       "</delta>\n");

        return body.toString();
    }


    private String calendarDeltaReorderEpochXML() {
        amendment.addDelta(calendarDeltaForReorder);

        expect(daoMock.getById(epoch.getId())).andReturn(epoch);


        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">\n", calendarDeltaForReorder.getGridId(), calendarDeltaForReorder.getNode().getGridId()))
            .append(format("  <reorder id=\"{0}\" child-id=\"{1}\" old-index=\"{2}\" new-index=\"{3}\"  />\n", reorderEpoch.getGridId(), reorderEpoch.getChild().getGridId(), reorderEpoch.getOldIndex(), reorderEpoch.getNewIndex()))
            .append(       "</delta>\n");

        return body.toString();
    }

    private String epochDeltaPropertyChangeXML() {
        amendment.addDelta(epochDeltaForPropertyChange);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">\n", epochDeltaForPropertyChange.getGridId(), epochDeltaForPropertyChange.getNode().getGridId()))
            .append(format("  <property-change id=\"{0}\" property-name=\"{1}\" old-value=\"{2}\" new-value=\"{3}\" />\n", epochPropertyChange.getGridId(), epochPropertyChange.getPropertyName(), epochPropertyChange.getOldValue(), epochPropertyChange.getNewValue()))
            .append(       "</delta>\n");

        return body.toString();
    }

    /* Validate methods */
    public String createAndValidateXml() throws Exception{
        replayMocks();
        String s = writer.createStudyXML(study);
        verifyMocks();
        log.debug("XML: {}", s);
        
        validate(s.getBytes());

        return s;
    }

    private void validate(byte[] byteOutput) {
        BindException errors = new BindException(byteOutput, EMPTY);
        invokeValidator(TEMPLATE_VALIDATOR_INSTANCE, new ByteArrayInputStream(byteOutput), errors);

        assertFalse("Template xml should be error free", errors.hasErrors());
    }


    /* Create Domain Objects with Grid Ids */
    private <T extends Named & GridIdentifiable & DomainObject> T createNamedInstance(String name, Class<T> clazz) throws Exception {
        return setGridId(Fixtures.createNamedInstance(name, clazz));
    }

    public Study createStudy(String name) throws Exception {
        Study newStudy = createNamedInstance(name, Study.class);
        newStudy.setPlannedCalendar(setGridId(new PlannedCalendar()));
        return newStudy;
    }

    public Amendment createAmendment() throws Exception {
        Amendment newAmendment = new Amendment(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        newAmendment.setDate(new Date());
        setGridId(newAmendment);
        return newAmendment;
    }
    
    private <T extends PlanTreeNode<? extends GridIdentifiable>> Delta<T> createDeltaFor(T node, Change... changes) throws Exception {
        return setGridId(Delta.createDeltaFor(node, changes));
    }

    private Add createAdd(PlanTreeNode<?> child) throws Exception {
        return setGridId(Add.create(child));
    }

    private Add createAdd(PlanTreeNode<?> child, int index) throws Exception {
        return setGridId(Add.create(child, index));
    }

    private Remove createRemove(PlanTreeNode<?> child) throws Exception {
        return setGridId(Remove.create(child));
    }

    private Reorder createReorder(PlanTreeNode<?> child, Integer oldIndex, Integer newIndex) throws Exception {
        return setGridId(Reorder.create(child, oldIndex, newIndex));
    }

    private PropertyChange createPropertyChange(String prop, String oldValue, String newValue) throws Exception {
        return setGridId(PropertyChange.create(prop, oldValue, newValue));
    }

    private PlannedActivity createPlannedActivity(String activityName, int day, String details, String condition) throws Exception {
        return setGridId(Fixtures.createPlannedActivity(activityName, day, details, condition));
    }

    private Activity createActivity(String name, String code, ActivityType type, String description) throws Exception {
        return setGridId(Fixtures.createActivity(name, code, source, type, description));
    }


    /* Base Grid Id Assignment Methods */
    private <T extends GridIdentifiable & DomainObject> T setGridId(T object) throws Exception {
        int nextGridId = nextGridId();
        object.setGridId('a' + valueOf(nextGridId)); // For some reason, the schema doesn't like integers for ids, so prepend 'a'
        object.setId((nextGridId));
        return object;
    }

    private int nextGridId() {
        return id++;
    }


    /* Skeleton for test XML */
    public static class StudyXMLSkeleton {
        private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        public static String insertXml(Study study, Amendment amendment, StringBuffer xml) {
            StringBuffer buf = new StringBuffer();
            buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
               .append(format("<study assigned-identifier=\"{0}\" id=\"{1}\" \n", study.getAssignedIdentifier(), study.getGridId()))
               .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
               .append(format("       {0}=\"{1} {2}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
               .append(format("       {0}=\"{1}\">\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
               .append(format("     <planned-calendar id=\"{0}\"/>\n", study.getPlannedCalendar().getGridId()))
               .append(format("     <amendment date=\"{0}\" id=\"{1}\" mandatory=\"{2}\" name=\"{3}\">\n", dateFormat.format(amendment.getDate()), amendment.getGridId(), amendment.isMandatory(), amendment.getName()))

               .append(                 xml).append('\n')

               .append(       "     </amendment>\n")
               .append(       "</study>\n");
            return buf.toString();
        }
    }

    // Needed this class becuase EasyMock has problems with expecting findDao method calls 
    public class TestingSpringDaoFinder implements DaoFinder {
        DomainObjectDao<?> mockDao;

        public TestingSpringDaoFinder(DomainObjectDao<?> mockDao) {
            this.mockDao = mockDao;
        }

        @SuppressWarnings({"unchecked"})
        public <T extends DomainObject> DomainObjectDao<?> findDao(Class<T> domainClass) {
            return mockDao;
        }
    }

}
