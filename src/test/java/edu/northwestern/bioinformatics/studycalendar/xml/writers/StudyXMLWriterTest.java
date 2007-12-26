package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static org.custommonkey.xmlunit.XMLUnit.setIgnoreWhitespace;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator.TEMPLATE_VALIDATOR_INSTANCE;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriterTest.StudyXMLSkeleton.insertXml;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import static org.springframework.validation.ValidationUtils.invokeValidator;

import java.io.ByteArrayInputStream;
import static java.lang.String.valueOf;
import static java.text.MessageFormat.format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StudyXMLWriterTest extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private StudyXMLWriter writer;

    private Study study;

    private static int id = 1;
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

    protected void setUp() throws Exception {
        super.setUp();

        setIgnoreWhitespace(true);

        writer = new StudyXMLWriter();

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
        addPeriod = createAdd(period, 0);
        segmentDelta = createDeltaFor(segment, addPeriod);

        /* Period Delta for Add(ing) Planned Activities */
        source = createNamedInstance("LOINK Source", Source.class);
        activity = createActivity("Bone Scan", "AA", null, ActivityType.DISEASE_MEASURE, "make sure im not broken");
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
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }

    public void testWriteStudySegment() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( epochDeltaXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }

    public void testWritePeriod() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( epochDeltaXML())
            .append( segmentDeltaXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }


     public void testWritePlannedActivity() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( epochDeltaXML())
            .append( segmentDeltaXML())
            .append( periodDeltaXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }


    public void testWritePlannedActivityWithoutDetailsAndCondition() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
                .append( epochDeltaXML())
                .append( segmentDeltaXML())
                .append( periodDeltaXMLWithoutDetailsAndCondition());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }

    public void testWriteRemoveEpoch() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( calendarDeltaRemoveEpochXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }

    public void testWriteReorderEpochs() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( calendarDeltaReorderEpochXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }

    public void testWritePlannedCalendarPropertyChange() throws Exception {
        StringBuffer body = new StringBuffer();

        body.append( calendarDeltaXML())
            .append( epochDeltaXML())
            .append( epochDeltaPropertyChangeXML());

        String expected = insertXml(study, amendment, body);
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }


    /* Output XML Methods */
    private String calendarDeltaXML() {
        amendment.addDelta(calendarDelta);

        StringBuffer body = new StringBuffer();
        body.append(    format("<delta id=\"{0}\" node-id=\"{1}\">", calendarDelta.getGridId(), calendarDelta.getNode().getGridId()))
                .append(format("  <add id=\"{0}\" index=\"{1}\">", addEpoch.getGridId(), addEpoch.getIndex()))
                .append(format("    <epoch id=\"{0}\" name=\"{1}\"/>", epoch.getGridId(), epoch.getName()))
                .append(       "  </add>")
                .append(       "</delta>");
        return body.toString();
    }

    private String epochDeltaXML() {
        amendment.addDelta(epochDelta);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">", epochDelta.getGridId(), epochDelta.getNode().getGridId()))
            .append(format("  <add id=\"{0}\" index=\"{1}\">", addSegment.getGridId(), addSegment.getIndex()))
            .append(format("    <study-segment id=\"{0}\" name=\"{1}\"/>", segment.getGridId(), segment.getName()))
            .append(       "    </add>")
            .append(       "</delta>");

        return body.toString();
    }

    private String segmentDeltaXML() {
        amendment.addDelta(segmentDelta);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">", segmentDelta.getGridId(), segmentDelta.getNode().getGridId()))
            .append(format("  <add id=\"{0}\" index=\"{1}\">", addPeriod.getGridId(), addPeriod.getIndex()))
            .append(format("    <period id=\"{0}\" name=\"{1}\"/>", period.getGridId(), period.getName()))
            .append(       "  </add>")
            .append(       "</delta>");

        return body.toString();
    }

    private String periodDeltaXML() {
        amendment.addDelta(periodDelta);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">", periodDelta.getGridId(), periodDelta.getNode().getGridId()))
            .append(format("  <add id=\"{0}\" index=\"{1}\">", addActivity.getGridId(), addActivity.getIndex()))
            .append(format("    <planned-activity id=\"{0}\" day=\"{1}\" details=\"{2}\" condition=\"{3}\" >", plannedActivity.getGridId(), plannedActivity.getDay(), plannedActivity.getDetails(), plannedActivity.getCondition()))
            .append(format("      <activity id=\"{0}\" name=\"{1}\" description=\"{2}\" type-id=\"{3}\" code=\"{4}\">", activity.getGridId(), activity.getName(), activity.getDescription(), activity.getType().getId(), activity.getCode()))
            .append(format("        <source id=\"{0}\" name=\"{1}\"/>", source.getGridId(), source.getName()))
            .append(       "      </activity>")
            .append(       "    </planned-activity>")
            .append(       "  </add>")
            .append(       "</delta>");

        return body.toString();
    }

    private String periodDeltaXMLWithoutDetailsAndCondition() {
        plannedActivity.setDetails(null);
        plannedActivity.setCondition(null);

        amendment.addDelta(periodDelta);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">", periodDelta.getGridId(), periodDelta.getNode().getGridId()))
            .append(format("  <add id=\"{0}\" index=\"{1}\">", addActivity.getGridId(), addActivity.getIndex()))
            .append(format("    <planned-activity id=\"{0}\" day=\"{1}\" >", plannedActivity.getGridId(), plannedActivity.getDay()))
            .append(format("      <activity id=\"{0}\" name=\"{1}\" description=\"{2}\" type-id=\"{3}\" code=\"{4}\">", activity.getGridId(), activity.getName(), activity.getDescription(), activity.getType().getId(), activity.getCode()))
            .append(format("        <source id=\"{0}\" name=\"{1}\"/>", source.getGridId(), source.getName()))
            .append(       "      </activity>")
            .append(       "    </planned-activity>")
            .append(       "  </add>")
            .append(       "</delta>");

        return body.toString();
    }

    private String calendarDeltaRemoveEpochXML() {
        amendment.addDelta(calendarDeltaForRemove);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">", calendarDeltaForRemove.getGridId(), calendarDeltaForRemove.getNode().getGridId()))
            .append(format("  <remove id=\"{0}\" child-id=\"{1}\"/>", removeEpoch.getGridId(), removeEpoch.getChild().getGridId()))
            .append(       "</delta>");

        return body.toString();
    }


    private String calendarDeltaReorderEpochXML() {
        amendment.addDelta(calendarDeltaForReorder);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">", calendarDeltaForReorder.getGridId(), calendarDeltaForReorder.getNode().getGridId()))
            .append(format("  <reorder id=\"{0}\" child-id=\"{1}\" old-index=\"{2}\" new-index=\"{3}\"  />", reorderEpoch.getGridId(), reorderEpoch.getChild().getGridId(), reorderEpoch.getOldIndex(), reorderEpoch.getNewIndex()))
            .append(       "</delta>");

        return body.toString();
    }

    private String epochDeltaPropertyChangeXML() {
        amendment.addDelta(epochDeltaForPropertyChange);

        StringBuffer body = new StringBuffer();
        body.append(format("<delta id=\"{0}\" node-id=\"{1}\">", epochDeltaForPropertyChange.getGridId(), epochDeltaForPropertyChange.getNode().getGridId()))
            .append(format("  <property-change id=\"{0}\" property-name=\"{1}\" old-value=\"{2}\" new-value=\"{3}\" />", epochPropertyChange.getGridId(), epochPropertyChange.getPropertyName(), epochPropertyChange.getOldValue(), epochPropertyChange.getNewValue()))
            .append(       "</delta>");

        return body.toString();
    }

    /* Validate methods */
    public String createAndValidateXml(Study study) throws Exception{
        String s = writer.createStudyXML(study);
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
    private <T extends Named & GridIdentifiable> T createNamedInstance(String name, Class<T> clazz) throws Exception {
        return setGridId(Fixtures.createNamedInstance(name, clazz));
    }

    public Study createStudy(String name) throws Exception {
        Study study = createNamedInstance(name, Study.class);
        study.setPlannedCalendar(setGridId(new PlannedCalendar()));
        return study;
    }

    public Amendment createAmendment() throws Exception {
        Amendment amendment = new Amendment(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        amendment.setDate(new Date());
        setGridId(amendment);
        return amendment;
    }
    
    private <T extends PlanTreeNode<? extends GridIdentifiable>> Delta<T> createDeltaFor(T node, Change... changes) throws Exception {
        return setGridId(Delta.createDeltaFor(node, changes));
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

    private Activity createActivity(String name, String code, Source source, ActivityType type, String description) throws Exception {
        return setGridId(Fixtures.createActivity(name, code, source, type, description));
    }


    /* Base Grid Id Assignment Methods */
    private <T extends GridIdentifiable> T setGridId(T object) throws Exception{
        object.setGridId(valueOf(nextGridId()));
        return object;
    }

    private String nextGridId() {
        return 'a' + valueOf(id++); // For some reason, the schema doesn't like integers for ids, so prepend 'a'
    }


    /* Skeleton for test XML */
    public static class StudyXMLSkeleton {
        private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        public static String insertXml(Study study, Amendment amendment, StringBuffer xml) {
            StringBuffer buf = new StringBuffer();
            buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
               .append(format("<study assigned-identifier=\"{0}\" id=\"{1}\" \n", study.getAssignedIdentifier(), study.getGridId()))
               .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
               .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
               .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
               .append(format("     <planned-calendar id=\"{0}\"/>\n", study.getPlannedCalendar().getGridId()))
               .append(format("     <amendment date=\"{0}\" id=\"{1}\" mandatory=\"{2}\" name=\"{3}\">", dateFormat.format(amendment.getDate()), amendment.getGridId(), amendment.isMandatory(), amendment.getName()))

               .append(                 xml)

               .append(       "     </amendment>")
               .append(       "</study>");
            return buf.toString();
        }
    }

}
