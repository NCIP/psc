package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.nwu.bioinformatics.commons.DateUtils;
import edu.nwu.bioinformatics.commons.testing.CoreTestCase;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Calendar;

/**
 * @author Saurabh Agrawal
 */
public class EpochDeltaXmlSerializerTest extends CoreTestCase {

    private EpochDeltaXmlSerializer epochDeltaXmlSerializer;

    protected PeriodDeltaXmlSerializer periodDeltaXmlSerializer;
    protected PlannedCalendar plannedCalendar;
    protected Activity activity1;
    private Activity activity2;
    protected PlannedActivity plannedActivity1;
    private PlannedActivity plannedActivity2;
    private Period period1, period2;
    protected StudySegment studySegment1;
    protected StudySegment studySegment2;
    private ChangeXmlSerializerFactory changeXmlSerializerFactory;
    private AddXmlSerializer addXmlSerializer;
    private Source source;
    private PlanTreeNodeXmlSerializerFactory planTreeNodeXmlSerializerFactory;
    private EpochXmlSerializer epochXmlSerializer;
    protected Add add1;
    protected PeriodDelta periodDelta;
    private StudySegmentXmlSerializer studySegmentXmlSerializer;
    private PeriodXmlSerializer periodXmlSerializer;
    private PlannedActivityXmlSerializer plannedActivityXmlSerializer;
    private ActivityXmlSerializer activityXmlSerializer;
    protected Activity activity3;
    protected StudySegmentDelta studySegmentDelta;

    private Amendment amendment;
    private EpochDelta epochDelta;
    private Epoch epoch1;
    private Element eDelta;

    @Override
    protected void setUp() throws Exception {

        super.setUp();
        source = new Source();
        source.setName("NU Sample Activities");

        activity1 = Fixtures.createActivity("CBC", "CBC", source, ActivityType.DISEASE_MEASURE);
        activity2 = Fixtures.createActivity("CBC", "CBC", null, ActivityType.LAB_TEST);
        activity3 = Fixtures.createActivity("Azacitidine", "100001", source, ActivityType.INTERVENTION);
        activity3.setDescription("");

        plannedActivity1 = new PlannedActivity();
        plannedActivity1.setGridId("4def4863-ab9c-4bbe-97ce-b06ceaac3209");
        plannedActivity1.setActivity(activity1);
        plannedActivity1.setDay(1);


        plannedActivity2 = new PlannedActivity();
        plannedActivity2.setGridId("86b374f7-a04f-4086-bdbb-3f81692c828d");
        plannedActivity2.setActivity(activity2);
        plannedActivity2.setDay(8);

        period1 = Fixtures.createPeriod("period1", 1, Duration.Unit.day, 21, 4);
        period1.setGridId("494db4ea-6d29-4a88-96c7-128d7947fbce");
        period1.getPlannedActivities().add(plannedActivity1);
        period1.getPlannedActivities().add(plannedActivity2);

        period2 = Fixtures.createPeriod("Transplant", 0, Duration.Unit.day, 15, 1);
        period2.setGridId("9922be01-5d8a-4077-8fd4-3236b8fe040b");


        studySegment1 = Fixtures.createNamedInstance("A", StudySegment.class);
        studySegment1.setGridId("0400e3b7-76b6-4ecb-88a1-61dcb5ecbae9");

        studySegment2 = Fixtures.createNamedInstance("Transplant", StudySegment.class);
        studySegment2.setGridId("111f8f6c-3d5e-4bf8-99bf-1b7122ace4d0");

        studySegment1.getPeriods().add(period1);
        studySegment1.getPeriods().add(period2);
        studySegment2.getPeriods().add(period2);


        epoch1 = edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance("Treatment", Epoch.class);
        epoch1.addStudySegment(studySegment1);
        epoch1.addStudySegment(studySegment2);
        epoch1.setGridId("690361c1-433e-4a25-bfe2-09db0ce2edab");

        add1 = edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAddChange(1, 0);
        add1.setChild(epoch1);
        add1.setGridId("cb6e3130-9d2e-44e8-80ac-170d1875db5c");


        epochDelta = new EpochDelta(epoch1);
        epochDelta.setGridId("6b2d06a3-f521-4ef8-9cae-3eb73f6f6bf4");
        epochDelta.addChange(add1);


        activityXmlSerializer = new ActivityXmlSerializer();
        plannedActivityXmlSerializer = new PlannedActivityXmlSerializer();
        plannedActivityXmlSerializer.setActivityXmlSerializer(activityXmlSerializer);

        periodXmlSerializer = new PeriodXmlSerializer() {
            @Override
            protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
                return plannedActivityXmlSerializer;
            }
        };
        studySegmentXmlSerializer = new StudySegmentXmlSerializer() {
            @Override
            protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
                return periodXmlSerializer;
            }
        };

        epochXmlSerializer = new EpochXmlSerializer() {
            @Override
            protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
                return studySegmentXmlSerializer;
            }
        };
        planTreeNodeXmlSerializerFactory = new PlanTreeNodeXmlSerializerFactory() {

            @Override
            public AbstractPlanTreeNodeXmlSerializer createXmlSerializer(Element node) {
                return epochXmlSerializer;

            }

            @Override
            public AbstractPlanTreeNodeXmlSerializer createXmlSerializer(PlanTreeNode<?> node) {
                return epochXmlSerializer;
            }
        };
        addXmlSerializer = new AddXmlSerializer() {
            @Override
            protected PlanTreeNodeXmlSerializerFactory getPlanTreeNodeSerializerFactory() {
                return planTreeNodeXmlSerializerFactory;
            }
        };
        changeXmlSerializerFactory = new ChangeXmlSerializerFactory() {
            @Override
            public AbstractChangeXmlSerializer createXmlSerializer(Element eChange, PlanTreeNode<?> deltaNode) {
                return addXmlSerializer;


            }

            @Override
            public AbstractChangeXmlSerializer createXmlSerializer(Change change, PlanTreeNode<?> deltaNode) {
                return addXmlSerializer;

            }
        };


        epochDeltaXmlSerializer = new EpochDeltaXmlSerializer() {
            @Override
            public ChangeXmlSerializerFactory getChangeXmlSerializerFactory() {
                return changeXmlSerializerFactory;
            }
        };


        amendment = Fixtures.createAmendment("[Second Expected]", DateUtils.createDate(2008, Calendar.AUGUST, 16), true);

        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);

    }

    public void testValidteElement() {
        assertFalse(StringUtils.isBlank(epochDeltaXmlSerializer.validate(amendment, eDelta)));
        assertEquals(String.format("\n released amendment present in the system does have  any delta matching with provied grid id %s and node id  %s of delta \n",
                epochDelta.getGridId(), epoch1.getGridId()), epochDeltaXmlSerializer.validate(amendment, eDelta).toString());

        amendment.addDelta(epochDelta);
        assertTrue(StringUtils.isBlank(epochDeltaXmlSerializer.validate(amendment, eDelta)));

    }


    public void testValidateElementForDifferentNumberOfChanges() {

        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        assertEquals(String.format("Imported document has different number of Changes for following delta.  Please make sure changes are identical and they are in same order.\n" + eDelta.asXML())
                , epochDeltaXmlSerializer.validate(amendment, eDelta).toString());


    }

    public void testValidateForInValidAddChangeContent() throws IOException, SAXException {
        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);

        ((Epoch) add1.getChild()).getStudySegments().get(0).getPeriods().last().getPlannedActivities().get(0).setActivity(activity3);

        assertTrue(epochDeltaXmlSerializer.validate(amendment, eDelta).contains("activities  are different for PlannedActivity"));


    }

    public void testValidateForInValidPropertyChangeAttributes() throws IOException, SAXException {

        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);


        add1.setIndex(5);


        assertTrue(epochDeltaXmlSerializer.validate(amendment, eDelta).contains("index is different. expected:5 , found (in imported document) :0"));


    }

    public void testValidateForIdenticalDelta() throws IOException, SAXException {

        epochDelta.addChange(add1);
        amendment.addDelta(epochDelta);
        eDelta = epochDeltaXmlSerializer.createElement(epochDelta);


        assertTrue(StringUtils.isBlank(epochDeltaXmlSerializer.validate(amendment, eDelta)));


    }
}
