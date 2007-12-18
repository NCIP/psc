package edu.northwestern.bioinformatics.studycalendar.xml.writers;

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
import org.custommonkey.xmlunit.XMLUnit;
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

    protected void setUp() throws Exception {
        super.setUp();

        writer = new StudyXmlWriter();

        study = Fixtures.createBasicTemplate();

        /* Create EpochDelta */
//        Add addSegment = createAdd(createNamedInstance("Segment 0", StudySegment.class), 0);
//        Delta<Epoch> epochDelta = createDeltaFor(study.getPlannedCalendar().getEpochs().get(0), addSegment);
//        approveTemplate(study, createAmendment(study, epochDelta));
//
//        /* Create StudySegmentDelta */
//        Add addPeriod = createAdd(createNamedInstance("Period 0", Period.class), 0);
//        Delta<StudySegment> segmentDelta = createDeltaFor(study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0), addPeriod);
//        approveTemplate(study, createAmendment(study, segmentDelta));
//
//        /* Create PeriodDelta */
//        Period period1 = createNamedInstance("Period 1", Period.class);
//        study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).getPeriods().add(period1);
//        Add addActivity = createAdd(createPlannedActivity("Planned Activity 0"), 0);
//        Delta<Period> periodDelta = createDeltaFor(period1, addActivity);
//        approveTemplate(study, createAmendment(study, periodDelta));
//
//        // TODO: make sure to leave last delta as development to test that functionality
//
//        // TODO: Clean this up
//        Amendment initial = study.getAmendmentsList().get(study.getAmendmentsList().size()-1);
//        setGridIds( study,
//                    study.getPlannedCalendar(),
//                    initial,
//                    initial.getDeltas().get(0),
//                    initial.getDeltas().get(0).getChanges().get(0),
//                    initial.getDeltas().get(0).getChanges().get(1),
//                    initial.getDeltas().get(0).getChanges().get(2),
//                    ((ChildrenChange) initial.getDeltas().get(0).getChanges().get(0)).getChild(),
//                    ((Epoch)((ChildrenChange) initial.getDeltas().get(0).getChanges().get(0)).getChild()).getStudySegments().get(0),
//                    ((ChildrenChange) initial.getDeltas().get(0).getChanges().get(1)).getChild(),
//                    ((Epoch)((ChildrenChange) initial.getDeltas().get(0).getChanges().get(1)).getChild()).getStudySegments().get(0),
//                    ((Epoch)((ChildrenChange) initial.getDeltas().get(0).getChanges().get(1)).getChild()).getStudySegments().get(1),
//                    ((Epoch)((ChildrenChange) initial.getDeltas().get(0).getChanges().get(1)).getChild()).getStudySegments().get(2),
//                     ((ChildrenChange) initial.getDeltas().get(0).getChanges().get(2)).getChild(),
//                    ((Epoch)((ChildrenChange) initial.getDeltas().get(0).getChanges().get(2)).getChild()).getStudySegments().get(0)
//        );

        XMLUnit.setIgnoreWhitespace(true);

        amendment = createAmendment();
        study = createStudy("Study A");
        study.setDevelopmentAmendment(amendment);


    }

    public void testWriteRootAndPlannedCalendar() throws Exception {
        study.setDevelopmentAmendment(null);

        String expected = insertXml(study, EMPTY);
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }

    public void testWriteAmendment() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        StringBuffer body = new StringBuffer();
        body.append(
                format("<amendment date=\"{0}\" id=\"{1}\" mandatory=\"{2}\" name=\"{3}\"/>",
                        formatter.format(amendment.getDate()), amendment.getGridId(), amendment.isMandatory(), amendment.getName()));

        String expected = insertXml(study, body.toString());
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }


    public void testWriteDelta() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Delta delta = setGridId(new PlannedCalendarDelta());

        amendment.addDelta(delta);

        StringBuffer body = new StringBuffer();
        body.append(
                format("<amendment date=\"{0}\" id=\"{1}\" mandatory=\"{2}\" name=\"{3}\">",
                        dateFormat.format(amendment.getDate()), amendment.getGridId(), amendment.isMandatory(), amendment.getName()))
            .append(format("<delta id=\"{0}\"/>", delta.getGridId()))
            .append("</amendment>");


        String expected = insertXml(study, body.toString());
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }

    public void testWriteAdd() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Add add = createAdd(null, 0);

        Delta delta = setGridId(new PlannedCalendarDelta());
        delta.addChange(add);

        amendment.addDelta(delta);

        StringBuffer body = new StringBuffer();
        body.append(
                format("<amendment date=\"{0}\" id=\"{1}\" mandatory=\"{2}\" name=\"{3}\">",
                        dateFormat.format(amendment.getDate()), amendment.getGridId(), amendment.isMandatory(), amendment.getName()))
            .append(format("<delta id=\"{0}\">", delta.getGridId()))
            .append(format("<add id=\"{0}\" index=\"{1}\"/>", add.getGridId(), add.getIndex()))
            .append("</delta>")
            .append("</amendment>");


        String expected = insertXml(study, body.toString());
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }

    public void testWriteEpoch() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Epoch epoch = createNamedInstance("Epoch A", Epoch.class);

        Add add = createAdd(epoch, 0);

        Delta delta = setGridId(new PlannedCalendarDelta());
        delta.addChange(add);

        amendment.addDelta(delta);

        StringBuffer body = new StringBuffer();
        body.append(
                format("<amendment date=\"{0}\" id=\"{1}\" mandatory=\"{2}\" name=\"{3}\">",
                        dateFormat.format(amendment.getDate()), amendment.getGridId(), amendment.isMandatory(), amendment.getName()))
            .append(format("<delta id=\"{0}\">", delta.getGridId()))
            .append(format("<add id=\"{0}\" index=\"{1}\">", add.getGridId(), add.getIndex()))
            .append(format("<epoch id=\"{0}\" name=\"{1}\"/>", epoch.getGridId(), epoch.getName()))
            .append("</add>")
            .append("</delta>")
            .append("</amendment>");


        String expected = insertXml(study, body.toString());
        String output = createAndValidateXml(study);

        assertXMLEqual(expected, output);
    }


//    public void testContainsEpoch() throws Exception {
//        String output = createAndValidateXml(study);
//
//        assertContainsTag(output, StudyXmlWriter.EPOCH);
//    }
//
//    public void testContainsStudySegment() throws Exception {
//        String output = createAndValidateXml(study);
//
//        assertContainsTag(output, StudyXmlWriter.STUDY_SEGMENT);
//    }
//
//    public void testContainsPeriod() throws Exception {
//        String output = createAndValidateXml(study);
//
//        assertContainsTag(output, StudyXmlWriter.PERIOD);
//    }



    /* Test Helpers */

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

    private static void setGridIds(GridIdentifiable... objects) throws Exception {
        for(GridIdentifiable object : objects) {
            object.setGridId(valueOf(nextGridId()));
        }
    }

    private static <T extends GridIdentifiable> T setGridId(T object) throws Exception{
        setGridIds(object);
        return object;
    }

    /* For some reason, the schema doesn't like strings integers 15 and up for ids, so prepend 'a' */
    private static String nextGridId() {
        return 'a' + valueOf(id++);
    }

    public static Amendment createAmendment() throws Exception {
        Amendment amendment = new Amendment(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        amendment.setDate(new Date());
        setGridId(amendment);
        return amendment;
    }
    
    private  static <T extends Named & GridIdentifiable> T createNamedInstance(String name, Class<T> clazz) throws Exception {
        return setGridId(Fixtures.createNamedInstance(name, clazz));
    }


    private static <T extends PlanTreeNode<? extends GridIdentifiable>> Delta<T> createDeltaFor(T node, Change... changes) throws Exception {
        return setGridId(Delta.createDeltaFor(node, changes));
    }

    private static Add createAdd(PlanTreeNode<?> child, int index) throws Exception {
        return setGridId(Add.create(child, index));
    }


    private static PlannedActivity createPlannedActivity(String activityName) throws Exception{
        PlannedActivity activity = new PlannedActivity();
        activity.setActivity(setGridId(createActivity(activityName)));
        activity.setDay(1);
        activity.setDetails("Scan Arm");
        setGridId(activity);
        return activity;
    }

    public static Study createStudy(String name) throws Exception {
        Study study = createNamedInstance(name, Study.class);
        study.setPlannedCalendar(setGridId(new PlannedCalendar()));
        return study;
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
