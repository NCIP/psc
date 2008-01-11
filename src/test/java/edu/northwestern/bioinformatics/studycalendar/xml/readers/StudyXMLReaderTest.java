package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static java.text.MessageFormat.format;
import java.util.Calendar;
import java.util.List;

public class StudyXMLReaderTest extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private StudyXMLReader reader;
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private DeltaDao deltaDao;
    private ChangeDao changeDao;
    private Study study;
    private PlannedCalendar calendar;
    private ActivityDao activityDao;
    private DeltaService deltaService;
    private SourceDao sourceDao;
    private PlannedCalendarDao plannedCalendarDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        sourceDao = registerDaoMockFor(SourceDao.class);
        changeDao = registerDaoMockFor(ChangeDao.class);
        plannedCalendarDao = registerDaoMockFor(PlannedCalendarDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);
        deltaService = registerMockFor(DeltaService.class);

        reader = new StudyXMLReader();
        reader.setStudyDao(studyDao);
        reader.setDeltaDao(deltaDao);
        reader.setSourceDao(sourceDao);
        reader.setChangeDao(changeDao);
        reader.setAmendmentDao(amendmentDao);
        reader.setActivityDao(activityDao);
        reader.setDeltaService(deltaService);
        reader.setPlannedCalendarDao(plannedCalendarDao);
        reader.setTemplateService(new TestingTemplateService());

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

        Amendment previous = setGridId("grid2", new Amendment());

        study.setAmendment(previous);

        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);

        expect(amendmentDao.getByGridId("grid2")).andReturn(previous);
        expect(amendmentDao.getByGridId("grid3")).andReturn(null);

        deltaService.apply(gridIdEq(study), gridIdEq(setGridId("grid3", new Amendment())));
        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        Amendment actualCurrent = actual.getAmendment();
        assertEquals("Wrong Grid Id", "grid3", actualCurrent.getGridId());
        assertEquals("Wrong Name", "amendment B", actualCurrent.getName());
        assertEquals("Wrong Date", DateUtils.createDate(2007, Calendar.DECEMBER, 26, 0, 0, 0), actualCurrent.getDate());
        assertTrue("Wrong Mandatory Value", actualCurrent.isMandatory());
        assertSame("Previous Amendment Should be same", previous, actualCurrent.getPreviousAmendment());

        Amendment actualPrevious = actualCurrent.getPreviousAmendment();
        assertSame("Amendments Should be the same", previous, actualPrevious);
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
           .append(       "    <planned-calendar-delta id=\"grid3\" node-id=\"grid1\">\n")
           .append(       "      <add id=\"grid4\" index=\"0\">")
           .append(       "        <epoch id=\"grid5\" name=\"Epoch A\"/>")
           .append(       "      </add>")
           .append(       "    </planned-calendar-delta>")
           .append(       "  </amendment>")
           .append(       "</study>");

        Epoch epoch = setGridId("grid5", createNamedInstance("Epoch A", Epoch.class));

        Add add = setGridId("grid4", Add.create(epoch, 0));

        PlannedCalendarDelta delta = (PlannedCalendarDelta) setGridId("grid3", Delta.createDeltaFor(calendar, add));

        Amendment amendment = setGridId("grid2", new Amendment());
        amendment.addDelta(delta);

        study.setAmendment(amendment);

        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);
        expect(amendmentDao.getByGridId("grid2")).andReturn(amendment);

        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        Delta actualDelta0 = actual.getAmendment().getDeltas().get(0);
        assertSame("Deltas should be the same", delta, actualDelta0);
        assertSame("Changes should be the same", add, actualDelta0.getChanges().get(0));
        assertSame("Children should be the same", epoch, ((ChildrenChange) actualDelta0.getChanges().get(0)).getChild());
    }

    public void testReadNewPlannedCalendarDelta() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\"?>\n")
           .append(       "<study id=\"grid0\" assigned-identifier=\"Study A\"")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
           .append(       "  <planned-calendar id=\"grid1\"/>\n")
           .append(       "  <amendment id=\"grid2\" name=\"Amendment A\" date=\"2008-01-01\" mandatory=\"true\">\n")
           .append(       "    <planned-calendar-delta id=\"grid3\" node-id=\"grid1\">\n")
           .append(       "      <add id=\"grid4\" index=\"0\">\n")
           .append(       "        <epoch id=\"grid5\" name=\"Epoch A\">\n")
           .append(       "          <study-segment id=\"grid6\" name=\"Segment A\">\n")
           .append(       "            <period id=\"grid7\" name=\"Period A\">\n")
           .append(       "              <planned-activity id=\"grid8\" day=\"1\" details=\"My Details\" condition=\"My Condition\">\n")
           .append(       "                <activity id=\"grid9\" name=\"Bone Scan\" description=\"make sure im not broken\" type-id=\"1\" code=\"AA\">")
           .append(       "                  <source id=\"grid10\" name=\"Source A\"/>")
           .append(       "                </activity>")
           .append(       "              </planned-activity>")
           .append(       "            </period>")
           .append(       "          </study-segment>")
           .append(       "        </epoch>")
           .append(       "      </add>\n")
           .append(       "    </planned-calendar-delta>\n")
           .append(       "  </amendment>\n")
           .append(       "</study>");


        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);

        expect(amendmentDao.getByGridId("grid2")).andReturn(null);
        expect(deltaDao.getByGridId("grid3")).andReturn(null);
        expect(changeDao.getByGridId("grid4")).andReturn(null);

        expect(activityDao.getByCodeAndSourceName("AA", "Source A")).andReturn(null);
        expect(sourceDao.getByName("Source A")).andReturn(null);

        deltaService.apply(gridIdEq(study), gridIdEq(setGridId("grid2", new Amendment())));
        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        // PlannedCalendarDelta
        Delta actualDelta0 = actual.getAmendment().getDeltas().get(0);
        assertTrue("Delta should be instance of PlannedCalendarDelta", actualDelta0 instanceof PlannedCalendarDelta);
        assertEquals("Wrong grid id", "grid3", actualDelta0.getGridId());
        assertEquals("Wrong node grid id", "grid1", actualDelta0.getNode().getGridId());
        assertEquals("Wrong Parent grid ID", "grid2", ((Amendment) actualDelta0.getRevision()).getGridId());

        // Add Change
        Add add = (Add) actualDelta0.getChanges().get(0);
        assertEquals("Wrong change grid id", "grid4", add.getGridId());
        assertEquals("Wrong parent grid ID", "grid3", add.getDelta().getGridId());

        // Epoch
        Epoch epoch = (Epoch) add.getChild();
        assertEquals("Wrong GridId", "grid5", epoch.getGridId());
        assertEquals("Wrong Name", "Epoch A", epoch.getName());

        // StudySegment
        StudySegment segment = epoch.getStudySegments().get(0);
        assertEquals("Wrong GridId", "grid6", segment.getGridId());
        assertEquals("Wrong Name", "Segment A", segment.getName());
        assertEquals("Wrong parent grid ID", "grid5", segment.getParent().getGridId());


        // Period
        Period period = segment.getPeriods().first();
        assertEquals("Wrong GridId", "grid7", period.getGridId());
        assertEquals("Wrong Name", "Period A", period.getName());
        assertEquals("Wrong parent grid ID", "grid6", period.getParent().getGridId());


        // PlannedActivity
        PlannedActivity plannedActivity = period.getPlannedActivities().get(0);
        assertEquals("Wrong GridId", "grid8", plannedActivity.getGridId());
        assertEquals("Wrong Day", 1, (int) plannedActivity.getDay());
        assertEquals("Wrong details", "My Details", plannedActivity.getDetails());
        assertEquals("Wrong condition", "My Condition", plannedActivity.getCondition());
        assertEquals("Wrong parent grid ID", "grid7", plannedActivity.getParent().getGridId());

        // Activity
        Activity activity = plannedActivity.getActivity();
        assertEquals("Wrong GridId", "grid9", activity.getGridId());
        assertEquals("Wrong Name", "Bone Scan", activity.getName());
        assertEquals("Wrong Description", "make sure im not broken", activity.getDescription());
        assertEquals("Wrong Type Id", ActivityType.DISEASE_MEASURE, activity.getType());
        assertEquals("Wrong Code", "AA", activity.getCode());

        // Source
        Source source = activity.getSource();
        assertEquals("Wrong GridId", "grid10", source.getGridId());
        assertEquals("Wrong Name", "Source A", source.getName());
        assertEquals("Wrong activity gridId", "grid9", source.getActivities().get(0).getGridId());

    }

    public void testReadNewEpochDelta() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\"?>\n")
           .append(       "<study id=\"grid0\" assigned-identifier=\"Study A\"")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
           .append(       "  <planned-calendar id=\"grid1\"/>\n")
           .append(       "  <amendment id=\"grid2\" name=\"Amendment A\" date=\"2008-01-01\" mandatory=\"true\">\n")
           .append(       "    <planned-calendar-delta id=\"grid3\" node-id=\"grid1\">\n")
           .append(       "      <add id=\"grid4\" index=\"0\">\n")
           .append(       "        <epoch id=\"grid5\" name=\"Epoch A\"/>\n")
           .append(       "      </add>\n")
           .append(       "    </planned-calendar-delta>\n")
           .append(       "  </amendment>\n")
           .append(       "  <amendment id=\"grid6\" name=\"Amendment B\" date=\"2008-01-01\" mandatory=\"true\">\n")
           .append(       "    <epoch-delta id=\"grid7\" node-id=\"grid5\">\n")
           .append(       "      <add id=\"grid8\" index=\"0\">\n")
           .append(       "        <study-segment id=\"grid9\" name=\"Segment A\"/>\n")
           .append(       "      </add>\n")
           .append(       "    </epoch-delta>\n")
           .append(       "  </amendment>\n")
           .append(       "</study>");


        Epoch epoch = setGridId("grid5", createNamedInstance("Epoch A", Epoch.class));

        Amendment amendment0 = setGridId("grid2", new Amendment());

        calendar.addEpoch(epoch);

        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);

        expect(amendmentDao.getByGridId("grid2")).andReturn(amendment0);
        expect(amendmentDao.getByGridId("grid6")).andReturn(null);
        deltaService.apply(gridIdEq(study), gridIdEq(setGridId("grid6", new Amendment())));

        expect(deltaDao.getByGridId("grid7")).andReturn(null);
        expect(changeDao.getByGridId("grid8")).andReturn(null);

        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        // EpochDelta
        Delta actualDelta1 = actual.getAmendment().getDeltas().get(0);
        assertTrue("Delta should be instance of EpochDelta", actualDelta1 instanceof EpochDelta);
        assertEquals("Wrong grid id", "grid7", actualDelta1.getGridId());
        assertEquals("Wrong node grid id", "grid5", actualDelta1.getNode().getGridId());
        assertEquals("Wrong Parent Amendment ID", "grid6", ((Amendment) actualDelta1.getRevision()).getGridId());

        // Add Change
        Add actualAdd1 = (Add) actualDelta1.getChanges().get(0);
        assertEquals("Wrong change grid id", "grid8", actualAdd1.getGridId());
        assertEquals("Wrong parent Delta ID", "grid7", actualAdd1.getDelta().getGridId());

        // StudySegment
        StudySegment actualStudySegment = (StudySegment) actualAdd1.getChild();
        assertEquals("Wrong Child GridId", "grid9", actualStudySegment.getGridId());
        assertEquals("Wrong Child Name", "Segment A", actualStudySegment.getName());
    }

    public void testReadNewStudySegmentDelta() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\"?>\n")
           .append(       "<study id=\"grid0\" assigned-identifier=\"Study A\"")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
           .append(       "  <planned-calendar id=\"grid1\"/>\n")
           .append(       "  <amendment id=\"grid2\" name=\"Amendment A\" date=\"2008-01-01\" mandatory=\"true\">\n")
           .append(       "    <planned-calendar-delta id=\"grid3\" node-id=\"grid1\">\n")
           .append(       "      <add id=\"grid4\" index=\"0\">\n")
           .append(       "        <epoch id=\"grid5\" name=\"Epoch A\">\n")
           .append(       "        </epoch>")
           .append(       "      </add>\n")
           .append(       "    </planned-calendar-delta>\n")
           .append(       "  </amendment>\n")
           .append(       "  <amendment id=\"grid6\" name=\"Amendment B\" date=\"2008-01-01\" mandatory=\"true\">\n")
           .append(       "    <epoch-delta id=\"grid7\" node-id=\"grid5\">\n")
           .append(       "      <add id=\"grid8\" index=\"0\">\n")
           .append(       "        <study-segment id=\"grid9\" name=\"Segment A\"/>\n")
           .append(       "      </add>\n")
           .append(       "    </epoch-delta>\n")
           .append(       "  </amendment>\n")
           .append(       "  <amendment id=\"grid10\" name=\"Amendment C\" date=\"2008-01-01\" mandatory=\"true\">\n")
           .append(       "    <study-segment-delta id=\"grid11\" node-id=\"grid9\">\n")
           .append(       "      <add id=\"grid12\" index=\"0\">\n")
           .append(       "        <period id=\"grid13\" name=\"Period A\"/>\n")
           .append(       "      </add>\n")
           .append(       "    </study-segment-delta>\n")
           .append(       "  </amendment>\n")
           .append(       "</study>");



        StudySegment studySegment = setGridId("grid9", createNamedInstance("StudySegment A", StudySegment.class));


        Epoch epoch = setGridId("grid5", createNamedInstance("Epoch A", Epoch.class));
        epoch.addStudySegment(studySegment);

        calendar.addEpoch(epoch);

        Amendment amendment0 = setGridId("grid2", new Amendment());
        Amendment amendment1 = setGridId("grid6", new Amendment());
        amendment1.setPreviousAmendment(amendment0);

        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);

        expect(amendmentDao.getByGridId("grid2")).andReturn(amendment0);
        expect(amendmentDao.getByGridId("grid6")).andReturn(amendment1);
        expect(amendmentDao.getByGridId("grid10")).andReturn(null);

        expect(deltaDao.getByGridId("grid11")).andReturn(null);
        expect(changeDao.getByGridId("grid12")).andReturn(null);

        deltaService.apply(gridIdEq(study), gridIdEq(setGridId("grid10", new Amendment())));
        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        // StudySegmentDelta
        Delta actualDelta1 = actual.getAmendment().getDeltas().get(0);
        assertTrue("Delta should be instance of StudySegmentDelta", actualDelta1 instanceof StudySegmentDelta);
        assertEquals("Wrong grid id", "grid11", actualDelta1.getGridId());
        assertEquals("Wrong node grid id", "grid9", actualDelta1.getNode().getGridId());
        assertEquals("Wrong Parent Amendment ID", "grid10", ((Amendment) actualDelta1.getRevision()).getGridId());

        // Add Change
        Add actualAdd = (Add) actualDelta1.getChanges().get(0);
        assertEquals("Wrong change grid id", "grid12", actualAdd.getGridId());
        assertEquals("Wrong parent Delta ID", "grid11", actualAdd.getDelta().getGridId());

        // Period
        Period actualStudySegment = (Period) actualAdd.getChild();
        assertEquals("Wrong Child GridId", "grid13", actualStudySegment.getGridId());
        assertEquals("Wrong Child Name", "Period A", actualStudySegment.getName());
    }

    public void testRemoveChange() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
           .append(       "  <planned-calendar id=\"grid1\" />\n")
           .append(       "  <amendment id=\"grid2\" name=\"amendment0 A\" date=\"2007-12-25\" mandatory=\"true\">\n")
           .append(       "    <planned-calendar-delta id=\"grid3\" node-id=\"grid1\">\n")
           .append(       "      <add id=\"grid4\" index=\"0\">")
           .append(       "        <epoch id=\"grid5\" name=\"Epoch A\"/>")
           .append(       "      </add>")
           .append(       "    </planned-calendar-delta>")
           .append(       "  </amendment>")
           .append(       "  <amendment id=\"grid6\" name=\"amendment0 B\" date=\"2007-12-25\" mandatory=\"true\">\n")
           .append(       "    <planned-calendar-delta id=\"grid7\" node-id=\"grid1\">\n")
           .append(       "      <remove id=\"grid8\" child-id=\"grid5\"/>")
           .append(       "    </planned-calendar-delta>")
           .append(       "  </amendment>")
           .append(       "</study>");

        Epoch epoch = setId(50, setGridId("grid5", createNamedInstance("Epoch A", Epoch.class)));

        calendar.addEpoch(epoch);

        Amendment amendment0 = setGridId("grid2", new Amendment());

        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);

        expect(amendmentDao.getByGridId("grid2")).andReturn(amendment0);
        expect(amendmentDao.getByGridId("grid6")).andReturn(null);

        expect(deltaDao.getByGridId("grid7")).andReturn(null);
        expect(changeDao.getByGridId("grid8")).andReturn(null);

        deltaService.apply(gridIdEq(study), gridIdEq(setGridId("grid6", new Amendment())));
        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        Remove actualRemove = (Remove) actual.getAmendment().getDeltas().get(0).getChanges().get(0);
        assertEquals("Wrong gridId", "grid8", actualRemove.getGridId());
        assertEquals("Wrong id", 50, (int) actualRemove.getChildId());
        assertSame("Wrong child", epoch, actualRemove.getChild());
    }

     public void testReorderChange() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
           .append(       "  <planned-calendar id=\"grid1\" />\n")
           .append(       "  <amendment id=\"grid2\" name=\"amendment0 A\" date=\"2007-12-25\" mandatory=\"true\">\n")
           .append(       "    <planned-calendar-delta id=\"grid3\" node-id=\"grid1\">\n")
           .append(       "      <add id=\"grid4\" index=\"0\">")
           .append(       "        <epoch id=\"grid5\" name=\"Epoch A\"/>")
           .append(       "        <epoch id=\"grid9\" name=\"Epoch B\"/>")
           .append(       "      </add>")
           .append(       "    </planned-calendar-delta>")
           .append(       "  </amendment>")
           .append(       "  <amendment id=\"grid6\" name=\"amendment0 B\" date=\"2007-12-25\" mandatory=\"true\">\n")
           .append(       "    <planned-calendar-delta id=\"grid7\" node-id=\"grid1\">\n")
           .append(       "      <reorder id=\"grid8\" child-id=\"grid5\" old-index=\"0\" new-index=\"1\"/>")
           .append(       "    </planned-calendar-delta>")
           .append(       "  </amendment>")
           .append(       "</study>");

        Epoch epoch0 = setId(50, setGridId("grid5", createNamedInstance("Epoch A", Epoch.class)));
        Epoch epoch1 = setId(51, setGridId("grid9", createNamedInstance("Epoch B", Epoch.class)));

        calendar.addEpoch(epoch0);
        calendar.addEpoch(epoch1);

        Amendment amendment0 = setGridId("grid2", new Amendment());

        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);

        expect(amendmentDao.getByGridId("grid2")).andReturn(amendment0);
        expect(amendmentDao.getByGridId("grid6")).andReturn(null);

        expect(deltaDao.getByGridId("grid7")).andReturn(null);
        expect(changeDao.getByGridId("grid8")).andReturn(null);

        deltaService.apply(gridIdEq(study), gridIdEq(setGridId("grid6", new Amendment())));
        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        Reorder actualReorder = (Reorder) actual.getAmendment().getDeltas().get(0).getChanges().get(0);
        assertEquals("Wrong gridId", "grid8", actualReorder.getGridId());
        assertEquals("Wrong id", 50, (int) actualReorder.getChildId());
        assertEquals("Wrong index", 0, (int) actualReorder.getOldIndex());
        assertEquals("Wrong index", 1, (int) actualReorder.getNewIndex());
        assertSame("Wrong child", epoch0, actualReorder.getChild());
    }

    public void testPropertyChange() throws Exception {
        StringBuffer buf = new StringBuffer();
        buf.append(       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
           .append(       "<study assigned-identifier=\"Study A\" id=\"grid0\" \n")
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
           .append(format("       {0}=\"{1}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION))
           .append(format("       {0}=\"{1}\" >\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
           .append(       "  <planned-calendar id=\"grid1\" />\n")
           .append(       "  <amendment id=\"grid2\" name=\"amendment0 A\" date=\"2007-12-25\" mandatory=\"true\">\n")
           .append(       "    <planned-calendar-delta id=\"grid3\" node-id=\"grid1\">\n")
           .append(       "      <add id=\"grid4\" index=\"0\">")
           .append(       "        <epoch id=\"grid5\" name=\"Epoch A\"/>")
           .append(       "      </add>")
           .append(       "    </planned-calendar-delta>")
           .append(       "  </amendment>")
           .append(       "  <amendment id=\"grid6\" name=\"amendment0 B\" date=\"2007-12-25\" mandatory=\"true\">\n")
           .append(       "    <planned-calendar-delta id=\"grid7\" node-id=\"grid1\">\n")
           .append(       "      <property-change id=\"grid8\" property-name=\"grid5\" old-value=\"Epoch A\" new-value=\"Epoch B\"/>")
           .append(       "    </planned-calendar-delta>")
           .append(       "  </amendment>")
           .append(       "</study>");

        Epoch epoch = setId(50, setGridId("grid5", createNamedInstance("Epoch A", Epoch.class)));

        Add add = setGridId("grid4", Add.create(epoch, 0));

        Delta delta = setGridId("grid3", Delta.createDeltaFor(epoch, add));
        delta.setNode(calendar);

        Amendment amendment0 = setGridId("grid2", new Amendment());
        Amendment amendment1 = setGridId("grid6", new Amendment());
        amendment0.addDelta(delta);

        study.setAmendment(amendment1);

        expect(studyDao.getByGridId("grid0")).andReturn(study);
        expect(plannedCalendarDao.getByGridId("grid1")).andReturn(calendar);
        expect(amendmentDao.getByGridId("grid2")).andReturn(amendment0);
        expect(amendmentDao.getByGridId("grid6")).andReturn(null);

        expect(deltaDao.getByGridId("grid7")).andReturn(null);
        expect(changeDao.getByGridId("grid8")).andReturn(null);
        deltaService.apply(gridIdEq(study), gridIdEq(amendment1));
        replayMocks();

        Study actual = reader.read(toInputStream(buf));
        verifyMocks();

        PropertyChange actualPropertyChange = (PropertyChange) actual.getAmendment().getDeltas().get(0).getChanges().get(0);
        assertEquals("Wrong gridId", "grid8", actualPropertyChange.getGridId());
        assertEquals("Wrong old value", "Epoch A", actualPropertyChange.getOldValue());
        assertEquals("Wrong new value", "Epoch B", actualPropertyChange.getNewValue());
    }

    public void testGetAllNodesAndFindById() throws Exception{
        StringBuffer buf = new StringBuffer();
        buf.append("<first id=\"1\"><second id=\"2\"><third id=\"3\"/></second><fourth id=\"4\"/></first>");

        // create a SchemaFactory that conforms to W3C XML Schema
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // get a DOM factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        // create a new parser that validates documents against a schema
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(toInputStream(buf));

        List<Node> nodes = reader.getAllNodes(doc.getFirstChild());
        assertEquals("Wrong size", 4, nodes.size());

        Node node = reader.findNodeById(nodes, "4");
        assertEquals("wrong node", "fourth", ((Element) node).getNodeName());
    }

    /* Test Helpers */
    private InputStream toInputStream(StringBuffer buf) throws Exception {
        return new ByteArrayInputStream(buf.toString().getBytes());
    }

     ////// CUSTOM MATCHERS

    private static <T extends AbstractMutableDomainObject> T gridIdEq(T expectedNode) {
        EasyMock.reportMatcher(new GridIdMatcher<T>(expectedNode));
        return null;
    }

    private static class GridIdMatcher<T extends AbstractMutableDomainObject> implements IArgumentMatcher {
        private T expectedNode;

        public GridIdMatcher(T expectedNode) {
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
