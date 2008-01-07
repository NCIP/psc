package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import static org.easymock.EasyMock.expect;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static java.text.MessageFormat.format;
import java.util.Calendar;
import java.util.List;

public class StudyXMLReaderTest extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private StudyXMLReader reader;
    private StudyDao studyDao;
    private PlannedCalendarDao plannedCalendarDao;
    private AmendmentDao amendmentDao;
    private DeltaDao deltaDao;
    private ChangeDao changeDao;
    private Study study;
    private PlannedCalendar calendar;
    private EpochDao epochDao;
    private StudySegmentDao studySegmentDao;
    private PeriodDao periodDao;
    private PlannedActivityDao plannedActivityDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        epochDao = registerDaoMockFor(EpochDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        changeDao = registerDaoMockFor(ChangeDao.class);
        periodDao = registerDaoMockFor(PeriodDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        plannedCalendarDao = registerDaoMockFor(PlannedCalendarDao.class);

        reader = new StudyXMLReader();
        reader.setEpochDao(epochDao);
        reader.setStudyDao(studyDao);
        reader.setDeltaDao(deltaDao);
        reader.setPeriodDao(periodDao);
        reader.setChangeDao(changeDao);
        reader.setAmendmentDao(amendmentDao);
        reader.setStudySegmentDao(studySegmentDao);
        reader.setPlannedActivityDao(plannedActivityDao);
        reader.setTemplateService(new TestingTemplateService());
        reader.setPlannedCalendarDao(plannedCalendarDao);

        calendar = setGridId("grid1", new PlannedCalendar());

        study = setGridId("grid0", createNamedInstance("Study A", Study.class));
        study.setPlannedCalendar(calendar);
    }

    public void testReadNewStudyAndPlannedCalendar() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
           .append(       " <planned-calendar id=\"grid1\" />\n")
           .append(       "</study>");

        expect(studyDao.getByGridId("grid0")).andReturn(null);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(null);
        replayMocks();
        
        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        assertEquals("Wrong Study Identifier", "Study A", actual.getAssignedIdentifier());
        assertEquals("Wrong Grid Id", "grid0", actual.getGridId());

        PlannedCalendar actualCalendar = actual.getPlannedCalendar();
        assertEquals("Wrong Grid Id", "grid1", actualCalendar.getGridId());
        assertEquals("Wrong Study Name", "Study A", actualCalendar.getStudy().getName());
    }

    public void testReadExistingStudyAndPlannedCalendar() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
           .append(       " <planned-calendar id=\"grid1\" />\n")
           .append(       "</study>");

        PlannedCalendar calendar = setGridId("grid1", new PlannedCalendar());

        Study study = setGridId("grid0", createNamedInstance("Study A", Study.class));
        study.setPlannedCalendar(calendar);

        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);
        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        assertSame("Studies should be same", study, actual);
        assertSame("Studies should be same", calendar, actual.getPlannedCalendar());
    }

    public void testReadAmendments() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
           .append(       "  <planned-calendar id=\"grid1\" />\n")
           .append(       "  <amendment id=\"grid2\" name=\"amendment A\" date=\"2007-12-25\" mandatory=\"true\"/>\n")
           .append(       "  <amendment id=\"grid3\" name=\"amendment B\" date=\"2007-12-26\" mandatory=\"true\" previous-amendment-id=\"grid2\"/>\n")
           .append(       "</study>");

        Amendment amendment0 = new Amendment();
        amendment0.setGridId("grid2");
        amendment0.setName("amendment A");
        amendment0.setDate(DateUtils.createDate(2007, Calendar.DECEMBER, 25, 0, 0, 0));
        amendment0.setMandatory(true);

        study.setAmendment(amendment0);

        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);

        expect(amendmentDao.getByGridId("grid2")).andReturn(amendment0);
        expect(amendmentDao.getByGridId("grid3")).andReturn(null);
        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        Amendment current = actual.getAmendment();
        Amendment actualAmendment0 = current.getPreviousAmendment();
        assertSame("Amendments Should be the same", amendment0, actualAmendment0);

        assertEquals("Wrong Grid Id", "grid3", current.getGridId());
        assertEquals("Wrong Name", "amendment B", current.getName());
        assertEquals("Wrong Date", DateUtils.createDate(2007, Calendar.DECEMBER, 26, 0, 0, 0), current.getDate());
        assertTrue("Wrong Mandatory Value", current.isMandatory());
        assertSame("Previous Amendment Should be same", amendment0, current.getPreviousAmendment());

    }

    public void testReadExistingDelta() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
           .append(       "  <planned-calendar id=\"grid1\" />\n")
           .append(       "  <amendment id=\"grid2\" name=\"amendment A\" date=\"2007-12-25\" mandatory=\"true\">\n")
           .append(       "    <delta id=\"grid3\" node-id=\"grid1\">\n")
           .append(       "      <add id=\"grid4\" index=\"0\">")
           .append(       "        <epoch id=\"grid5\" name=\"Epoch A\"/>")
           .append(       "      </add>")
           .append(       "    </delta>")
           .append(       "  </amendment>")
           .append(       "</study>");

        Epoch epoch = setGridId("grid5", createNamedInstance("Epoch A", Epoch.class));

        Add add = setGridId("grid4", Add.create(epoch, 0));

        Delta delta = setGridId("grid3", Delta.createDeltaFor(epoch, add));

        Amendment amendment = setGridId("grid2", new Amendment());
        amendment.addDelta(delta);

        study.setAmendment(amendment);

        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);
        expect(amendmentDao.getByGridId("grid2")).andReturn(amendment);

        expect(deltaDao.getByGridId("grid3")).andReturn(delta);
        expect(changeDao.getByGridId("grid4")).andReturn(add);
        expect(epochDao.getByGridId("grid5")).andReturn(epoch);
        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        Delta actualDelta0 = actual.getAmendment().getDeltas().get(0);
        assertSame("Deltas should be the same", delta, actualDelta0);
        assertSame("Changes should be the same", add, actualDelta0.getChanges().get(0));
        assertSame("Children should be the same", epoch, ((ChildrenChange) actualDelta0.getChanges().get(0)).getChild());
    }

    public void testReadNewDeltas() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\"?>\n")
           .append(       "<study id=\"grid0\" assigned-identifier=\"Study A\"")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
           .append(       "  <planned-calendar id=\"grid1\"/>\n")
           .append(       "  <amendment id=\"grid2\" name=\"Amendment A\" date=\"2008-01-01\" mandatory=\"true\">\n")
           .append(       "    <delta id=\"grid3\" node-id=\"grid1\">\n")
           .append(       "      <add id=\"grid4\" index=\"0\">\n")
           .append(       "        <epoch id=\"grid5\" name=\"Epoch A\">\n")
           .append(       "          <study-segment id=\"grid6\" name=\"Segment A\">\n")
           .append(       "            <period id=\"grid7\" name=\"Period A\">\n")
//           .append(       "              <planned-activity id=\"grid8\" day=\"1\" details=\"My Details\" condition=\"My Condition\">\n")
//           .append(       "                <activity id=\"grid9\" name=\"Bone Scan\" description=\"make sure im not broken\" type-id=\"1\" code=\"AA\"/>")
//           .append(       "              </planned-activity>")
           .append(       "            </period>")
           .append(       "          </study-segment>")
           .append(       "        </epoch>")
           .append(       "      </add>\n")
           .append(       "    </delta>\n")
           .append(       "  </amendment>\n")
           .append(       "</study>");

        Amendment amendment = new Amendment();
        amendment.setGridId("grid2");

        study.setAmendment(amendment);

        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);
        expect(amendmentDao.getByGridId("grid2")).andReturn(amendment);

        // PlannedCalendarDelta
        expect(deltaDao.getByGridId("grid3")).andReturn(null);
        expect(changeDao.getByGridId("grid4")).andReturn(null);

        expect(epochDao.getByGridId("grid5")).andReturn(null);
        expect(studySegmentDao.getByGridId("grid6")).andReturn(null);
        expect(periodDao.getByGridId("grid7")).andReturn(null);
//        expect(plannedActivityDao.getByGridId("grid8")).andReturn(null);
        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        // PlannedCalendarDelta
        Delta actualDelta0 = actual.getAmendment().getDeltas().get(0);
        assertTrue("Delta should be instance of PlannedCalendarDelta", actualDelta0 instanceof PlannedCalendarDelta);
        assertEquals("Wrong grid id", "grid3", actualDelta0.getGridId());
        assertEquals("Wrong node grid id", "grid1", actualDelta0.getNode().getGridId());
        assertEquals("Wrong Parent Amendment ID", "grid2", ((Amendment) actualDelta0.getRevision()).getGridId());

        // Add Change
        Add add = (Add) actualDelta0.getChanges().get(0);
        assertEquals("Wrong change grid id", "grid4", add.getGridId());
        assertEquals("Wrong parent Delta ID", "grid3", add.getDelta().getGridId());

        // Epoch
        Epoch epoch = (Epoch) add.getChild();
        assertEquals("Wrong Child GridId", "grid5", epoch.getGridId());
        assertEquals("Wrong Child Name", "Epoch A", epoch.getName());

        // StudySegment
        StudySegment segment = epoch.getStudySegments().get(0);
        assertEquals("Wrong Child GridId", "grid6", segment.getGridId());
        assertEquals("Wrong Child Name", "Segment A", segment.getName());


        // Period
        Period period = segment.getPeriods().first();
        assertEquals("Wrong Child GridId", "grid7", period.getGridId());
        assertEquals("Wrong Child Name", "Period A", period.getName());


/*        // PlannedActivity
        PlannedActivity plannedActivity = period.getPlannedActivities().get(0);
        assertEquals("Wrong Child GridId", "grid8", plannedActivity.getGridId());
        assertEquals("Wrong Day", 1, (int) plannedActivity.getDay());
        assertEquals("Wrong details", "My Details", plannedActivity.getDetails());
        assertEquals("Wrong condition", "My Condition", plannedActivity.getCondition());

        // Activity
        Activity activity = plannedActivity.getActivity();*/
//        assertEquals("Wrong GridId", "grid9", activity.getGridId());
//        assertEquals("Wrong Name", "Bone Scane", activity.getName());
//        assertEquals("Wrong Description", "make sure im not broken", activity.getDescription());
//        assertEquals("Wrong Type Id", ActivityType.DISEASE_MEASURE, activity.getType());
//        assertEquals("Wrong Code", "AA", activity.getCode());
    }

    /* Test Helpers */
    private InputStream toInputStream(StringBuffer buf) throws Exception {
        return new ByteArrayInputStream(buf.toString().getBytes());
    }

     ////// CUSTOM MATCHERS

    private static <T extends AbstractMutableDomainObject> T planTreeNodeEq(T expectedNode) {
        EasyMock.reportMatcher(new StudySiteMatcher<T>(expectedNode));
        return null;
    }

    private static class StudySiteMatcher<T extends AbstractMutableDomainObject> implements IArgumentMatcher {
        private T expectedNode;

        public StudySiteMatcher(T expectedNode) {
            this.expectedNode = expectedNode;
        }

        public boolean matches(Object object) {
            T actual = (T) object;

            if (expectedNode.getGridId().equals(actual.getGridId())) {
                return true;
            }

            return false;
        }

        public void appendTo(StringBuffer sb) {
            sb.append("StudySite with study=").append(expectedNode);
        }
    }
}
