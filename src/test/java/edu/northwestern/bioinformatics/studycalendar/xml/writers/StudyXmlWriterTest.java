package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static org.custommonkey.xmlunit.XMLUnit.setIgnoreWhitespace;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XmlValidator.TEMPLATE_VALIDATOR_INSTANCE;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlWriter.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlWriterTest.StudyXMLSkeleton.insertXml;
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

public class StudyXmlWriterTest extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private StudyXmlWriter writer;

    private Study study;

    private static int id = 1;
    private Amendment amendment;
    private Delta<PlannedCalendar> calendarDelta;
    private Delta<Epoch> epochDelta;
    private Delta<StudySegment> segmentDelta;
    private Delta<PlannedActivity> plannedActivityDelta;
    private Add addEpoch;
    private Add addSegment;
    private Add addPeriod;
    private Add addActivity;
    private Epoch epoch;
    private StudySegment segment;
    private Period period;
    private PlannedActivity plannedActivity;

    protected void setUp() throws Exception {
        super.setUp();

        setIgnoreWhitespace(true);

        writer = new StudyXmlWriter();

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
        plannedActivity = createPlannedActivity("Bone Scan", 1, "details", ActivityType.DISEASE_MEASURE);
        addActivity = createAdd(plannedActivity, 0);
        plannedActivityDelta = createDeltaFor(plannedActivity, addActivity);
    }

    public void testWriteEpoch() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        /* Add Deltas */
        amendment.addDelta(calendarDelta);

        StringBuffer body = new StringBuffer();
        body.append(format("<amendment date=\"{0}\" id=\"{1}\" mandatory=\"{2}\" name=\"{3}\">", dateFormat.format(amendment.getDate()), amendment.getGridId(), amendment.isMandatory(), amendment.getName()))
            .append(format("  <delta id=\"{0}\" node-id=\"{1}\">", calendarDelta.getGridId(), calendarDelta.getNode().getGridId()))
            .append(format("    <add id=\"{0}\" index=\"{1}\">", addEpoch.getGridId(), addEpoch.getIndex()))
            .append(format("      <epoch id=\"{0}\" name=\"{1}\"/>", epoch.getGridId(), epoch.getName()))
            .append(       "    </add>")
            .append(       "  </delta>")
            .append(       "</amendment>");


        String expected = insertXml(study, body.toString());
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }

    public void testWriteStudySegment() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        /* Add Deltas */
        amendment.addDelta(calendarDelta);
        amendment.addDelta(epochDelta);

        StringBuffer body = new StringBuffer();
        body.append(format(    "<amendment date=\"{0}\" id=\"{1}\" mandatory=\"{2}\" name=\"{3}\">", dateFormat.format(amendment.getDate()), amendment.getGridId(), amendment.isMandatory(), amendment.getName()))
                .append(format("  <delta id=\"{0}\" node-id=\"{1}\">", calendarDelta.getGridId(), calendarDelta.getNode().getGridId()))
                .append(format("    <add id=\"{0}\" index=\"{1}\">", addEpoch.getGridId(), addEpoch.getIndex()))
                .append(format("      <epoch id=\"{0}\" name=\"{1}\"/>", epoch.getGridId(), epoch.getName()))
                .append(       "    </add>")
                .append(       "  </delta>")
                .append(format("  <delta id=\"{0}\" node-id=\"{1}\">", epochDelta.getGridId(), epochDelta.getNode().getGridId()))
                .append(format("    <add id=\"{0}\" index=\"{1}\">", addSegment.getGridId(), addSegment.getIndex()))
                .append(format("      <study-segment id=\"{0}\" name=\"{1}\"/>", segment.getGridId(), segment.getName()))
                .append(       "    </add>")
                .append(       "  </delta>")
                .append(       "</amendment>");


        String expected = insertXml(study, body.toString());
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }

    public void testWritePeriod() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        /* Add Deltas */
        amendment.addDelta(calendarDelta);
        amendment.addDelta(epochDelta);
        amendment.addDelta(segmentDelta);

        StringBuffer body = new StringBuffer();
        body.append(format(    "<amendment date=\"{0}\" id=\"{1}\" mandatory=\"{2}\" name=\"{3}\">", dateFormat.format(amendment.getDate()), amendment.getGridId(), amendment.isMandatory(), amendment.getName()))
                .append(format("  <delta id=\"{0}\" node-id=\"{1}\">", calendarDelta.getGridId(), calendarDelta.getNode().getGridId()))
                .append(format("    <add id=\"{0}\" index=\"{1}\">", addEpoch.getGridId(), addEpoch.getIndex()))
                .append(format("      <epoch id=\"{0}\" name=\"{1}\"/>", epoch.getGridId(), epoch.getName()))
                .append(       "    </add>")
                .append(       "  </delta>")
                .append(format("  <delta id=\"{0}\" node-id=\"{1}\">", epochDelta.getGridId(), epochDelta.getNode().getGridId()))
                .append(format("    <add id=\"{0}\" index=\"{1}\">", addSegment.getGridId(), addSegment.getIndex()))
                .append(format("      <study-segment id=\"{0}\" name=\"{1}\"/>", segment.getGridId(), segment.getName()))
                .append(       "    </add>")
                .append(       "  </delta>")
                .append(format("  <delta id=\"{0}\" node-id=\"{1}\">", segmentDelta.getGridId(), segmentDelta.getNode().getGridId()))
                .append(format("    <add id=\"{0}\" index=\"{1}\">", addPeriod.getGridId(), addPeriod.getIndex()))
                .append(format("      <period id=\"{0}\" name=\"{1}\"/>", period.getGridId(), period.getName()))
                .append(       "    </add>")
                .append(       "  </delta>")
                .append(       "</amendment>");


        String expected = insertXml(study, body.toString());
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }


     public void testWritePlannedActivity() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        /* Add Deltas */
        amendment.addDelta(calendarDelta);
        amendment.addDelta(epochDelta);
        amendment.addDelta(segmentDelta);
        amendment.addDelta(plannedActivityDelta);

        StringBuffer body = new StringBuffer();
        body.append(format(    "<amendment date=\"{0}\" id=\"{1}\" mandatory=\"{2}\" name=\"{3}\">", dateFormat.format(amendment.getDate()), amendment.getGridId(), amendment.isMandatory(), amendment.getName()))
                .append(format("  <delta id=\"{0}\" node-id=\"{1}\">", calendarDelta.getGridId(), calendarDelta.getNode().getGridId()))
                .append(format("    <add id=\"{0}\" index=\"{1}\">", addEpoch.getGridId(), addEpoch.getIndex()))
                .append(format("      <epoch id=\"{0}\" name=\"{1}\"/>", epoch.getGridId(), epoch.getName()))
                .append(       "    </add>")
                .append(       "  </delta>")
                .append(format("  <delta id=\"{0}\" node-id=\"{1}\">", epochDelta.getGridId(), epochDelta.getNode().getGridId()))
                .append(format("    <add id=\"{0}\" index=\"{1}\">", addSegment.getGridId(), addSegment.getIndex()))
                .append(format("      <study-segment id=\"{0}\" name=\"{1}\"/>", segment.getGridId(), segment.getName()))
                .append(       "    </add>")
                .append(       "  </delta>")
                .append(format("  <delta id=\"{0}\" node-id=\"{1}\">", segmentDelta.getGridId(), segmentDelta.getNode().getGridId()))
                .append(format("    <add id=\"{0}\" index=\"{1}\">", addPeriod.getGridId(), addPeriod.getIndex()))
                .append(format("      <period id=\"{0}\" name=\"{1}\"/>", period.getGridId(), period.getName()))
                .append(       "    </add>")
                .append(       "  </delta>")
                .append(format("  <delta id=\"{0}\" node-id=\"{1}\">", plannedActivityDelta.getGridId(), plannedActivityDelta.getNode().getGridId()))
                .append(format("    <add id=\"{0}\" index=\"{1}\">", addActivity.getGridId(), addActivity.getIndex()))
                .append(format("      <planned-activity id=\"{0}\" />", plannedActivity.getGridId()))
                .append(       "    </add>")
                .append(       "  </delta>")
                .append(       "</amendment>");


        String expected = insertXml(study, body.toString());
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }


    /* Validate methods */
    public String createAndValidateXml(Study study) throws Exception{
        String s = writer.createStudyXml(study);
        log.debug("XML: {}", s);
        
        validate(s.getBytes());

        return s;
    }

    private static void validate(byte[] byteOutput) {
        BindException errors = new BindException(byteOutput, EMPTY);
        invokeValidator(TEMPLATE_VALIDATOR_INSTANCE, new ByteArrayInputStream(byteOutput), errors);

        assertFalse("Template xml should be error free", errors.hasErrors());
    }


    /* Create Domain Objects with Grid Ids */
    private  static <T extends Named & GridIdentifiable> T createNamedInstance(String name, Class<T> clazz) throws Exception {
        return setGridId(Fixtures.createNamedInstance(name, clazz));
    }

    public static Study createStudy(String name) throws Exception {
        Study study = createNamedInstance(name, Study.class);
        study.setPlannedCalendar(setGridId(new PlannedCalendar()));
        return study;
    }

    public static Amendment createAmendment() throws Exception {
        Amendment amendment = new Amendment(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        amendment.setDate(new Date());
        setGridId(amendment);
        return amendment;
    }
    
    private static <T extends PlanTreeNode<? extends GridIdentifiable>> Delta<T> createDeltaFor(T node, Change... changes) throws Exception {
        return setGridId(Delta.createDeltaFor(node, changes));
    }

    private static Add createAdd(PlanTreeNode<?> child, int index) throws Exception {
        return setGridId(Add.create(child, index));
    }

    private static PlannedActivity createPlannedActivity(String activityName, int day, String details, ActivityType type) throws Exception {
        return setGridId(Fixtures.createPlannedActivity(activityName, day, details, type));
    }


    /* Grid Id Assignment Methods */
    private static <T extends GridIdentifiable> T setGridId(T object) throws Exception{
        object.setGridId(valueOf(nextGridId()));
        return object;
    }

    private static String nextGridId() {
        // For some reason, the schema doesn't like strings integers 15 and up for ids, so prepend 'a'
        return 'a' + valueOf(id++);
    }

    public static class StudyXMLSkeleton {

        public static String insertXml(Study study, String xml) {
            StringBuffer buf = new StringBuffer();
            buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
               .append(format("<{0} {1}=\"{2}\" \n" , ROOT, ASSIGNED_IDENTIFIER, study.getAssignedIdentifier()))
               .append(format(" {0}=\"{1}\" \n"     , ID, study.getGridId()))
               .append(format(" {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE))
               .append(format(" {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
               .append(format(" {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XML_SCHEMA))
               .append(format("<{0} {1}=\"{2}\"/>\n", PLANNDED_CALENDAR, ID, study.getPlannedCalendar().getGridId()))
               .append(xml)
               .append(format("</{0}>", ROOT));
            return buf.toString();
        }
    }

}
