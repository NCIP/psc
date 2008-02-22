package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.io.InputStream;
import java.io.Reader;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.Date;
import static java.lang.String.valueOf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.factory.annotation.Required;
import junit.framework.TestCase;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;

/**
 * @author Saurabh Agrawal
 */
public class PSCStudyImportExportTest extends TestCase {

    private static final Log logger = LogFactory.getLog(PSCStudyImportExportTest.class);

    public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

    private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";


    private StudyDao studyDao;


    private StudyXMLWriter writer;

    private edu.northwestern.bioinformatics.studycalendar.domain.Study study;

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

        writer = new StudyXMLWriter();

        amendment = createAmendment();

        study = createStudy("Study A");
        study.setAmendment(amendment);

        /* Planned Calendar Delta for Add(ing) an Epoch */
//          epoch = createNamedInstance("Epoch A", Epoch.class);
//          addEpoch = createAdd(epoch, 0);
//          calendarDelta = createDeltaFor(study.getPlannedCalendar(), addEpoch);
//
//          /* Epoch Delta for Add(ing) a StudySegment */
//          segment = createNamedInstance("Segment A", StudySegment.class);
//          addSegment = createAdd(segment, 0);
//          epochDelta = createDeltaFor(epoch, addSegment);
//
//          /* Study Segment Delta for Add(ing) Periods */
//          period = createNamedInstance("Period A", Period.class);
//          addPeriod = createAdd(period, 0);
//          segmentDelta = createDeltaFor(segment, addPeriod);
//
//          /* Period Delta for Add(ing) Planned Activities */
//          source = createNamedInstance("LOINK Source", Source.class);
//          activity = createActivity("Bone Scan", "AA", null, ActivityType.DISEASE_MEASURE, "make sure im not broken");
//          activity.setSource(source);
//          plannedActivity = createPlannedActivity("Bone Scan", 1, "details", "patient is male");
//          plannedActivity.setActivity(activity);
//          addActivity = createAdd(plannedActivity, 0);
//          periodDelta = createDeltaFor(plannedActivity, addActivity);
//
//          /* Planned Calendar Delta for Remove(ing) an Epoch */
//          removeEpoch = createRemove(epoch);
//          calendarDeltaForRemove = createDeltaFor(study.getPlannedCalendar(), removeEpoch);
//
//          /* Planned Calendar Delta for Reorder(ing) an Epoch */
//          reorderEpoch = createReorder(epoch, 0, 1);
//          calendarDeltaForReorder = createDeltaFor(study.getPlannedCalendar(), reorderEpoch);

        /* Epoch Delta for an Epoch Property Change */
//          epochPropertyChange = createPropertyChange("name", "Epoch A", "Epoch B");
        //        epochDeltaForPropertyChange = createDeltaFor(epoch, epochPropertyChange);
    }

    public Study createStudy(String name) throws Exception {
        Study study = new Study();
        study.setId(1);
        study.setGridId("grid study");
        study.setAssignedIdentifier("assigned identifier");
        study.setPlannedCalendar(setGridId(new PlannedCalendar()));
        return study;
    }

    public Amendment createAmendment() throws Exception {
        Amendment amendment = new Amendment(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        amendment.setDate(new Date());
        setGridId(amendment);
        return amendment;
    }

    /* Base Grid Id Assignment Methods */
    private <T extends GridIdentifiable> T setGridId(T object) throws Exception {
        object.setGridId(valueOf(nextGridId()));
        return object;
    }

    private String nextGridId() {
        return 'a' + valueOf(id++); // For some reason, the schema doesn't like integers for ids, so prepend 'a'
    }

    public void testSerializeDto() throws Exception {


        String studyXml = writer.createStudyXML(study);
        assertFalse(StringUtils.isBlank(studyXml));


    }


    public String exportStudyByCoordinatingCenterIdentifier(String string) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Study populateStudyDTO(edu.northwestern.bioinformatics.studycalendar.domain.Study pscStudy) throws Exception {
        Study gridStudy = null;
        try {
            InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "gov/nih/nci/ccts/grid/client/client-config.wsdd");
            //Reader reader = new FileReader(regFile);
            //gridStudy = (Study) gov.nih.nci.cagrid.common.Utils.serializeObject(reader, Study.class, config);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
        return null;
        //return gridStudy;
    }

}