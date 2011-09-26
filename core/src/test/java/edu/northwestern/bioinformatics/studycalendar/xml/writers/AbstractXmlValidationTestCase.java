package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaNodeType;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.EpochDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PeriodDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedActivityDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.StudySegmentDelta;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.springframework.beans.factory.BeanFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createAddChange;

/**
 * @author Saurabh Agrawal
 */
public abstract class AbstractXmlValidationTestCase extends StudyCalendarTestCase {

    protected static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    protected Study study;
    private PlannedCalendar calendar;
    private Population population;
    protected Element ePopulation;
    protected Amendment amendment;
    protected PlannedCalendarDelta plannedCalendarDelta;
    protected DeltaXmlSerializer periodDeltaXmlSerializer;
    protected PlannedCalendar plannedCalendar;
    protected Activity activity1;
    private Activity activity2;
    protected PlannedActivity plannedActivity1;
    private PlannedActivity plannedActivity2;
    protected PlannedActivity plannedActivity3;
    private Period period1;
    private Period period2;
    protected Period period3;
    private Period period4;
    protected StudySegment studySegment1;
    protected StudySegment studySegment2;
    private DeltaXmlSerializer plannedCalendarDeltaXmlSerializer;
    private ChangeXmlSerializerFactory changeXmlSerializerFactory;
    private AddXmlSerializer addXmlSerializer;
    private ReorderXmlSerializer reorderXmlSerializer;
    private RemoveXmlSerializer removeXmlSerializer;
    private PropertyChangeXmlSerializer propertyChangeXmlSerializer;
    private Source source;
    private ChangeableXmlSerializerFactory changeableXmlSerializerFactory;
    protected Epoch epoch1;
    private EpochXmlSerializer epochXmlSerializer;
    protected Add add1;
    protected Reorder reorder;
    protected Amendment expectedDevelopmentAmendment;
    protected PeriodDelta periodDelta;
    protected Add add2;
    protected PropertyChange propertyChange1;
    protected PropertyChange propertyChange2;
    private StudySegmentXmlSerializer studySegmentXmlSerializer;
    private PeriodXmlSerializer periodXmlSerializer;
    private PlannedActivityXmlSerializer plannedActivityXmlSerializer;
    private ActivityXmlSerializer activityXmlSerializer;
    protected Activity activity3;
    protected StudySegmentDelta studySegmentDelta;
    protected DeltaXmlSerializer studySegmentDeltaXmlSerializer;
    protected Add add3;
    protected EpochDelta epochDelta;
    protected DeltaXmlSerializer epochDeltaXmlSerializer;
    protected DeltaXmlSerializerFactory deltaSerializerFactory;
    protected AmendmentXmlSerializer amendmentSerializer;
    protected PopulationXmlSerializer populationSerializer;
    protected PlannedCalendarXmlSerializer plannedCalendarSerializer;
    protected AmendmentXmlSerializer developmentAmendmentSerializer;
    protected Amendment firstAmendment;

    protected StudyXmlSerializer studyXmlserializer;
    private BeanFactory beanFactory;

    protected void setUp() throws Exception {
        super.setUp();
        beanFactory = registerMockFor(BeanFactory.class);

        source = new Source();
        source.setName("NU Sample Activities");

        activity1 = Fixtures.createActivity("CBC", "CBC", source, Fixtures.createActivityType("DISEASE_MEASURE"));
        activity2 = Fixtures.createActivity("CBC", "CBC", source, Fixtures.createActivityType("LAB_TEST"));
        activity3 = Fixtures.createActivity("Azacitidine", "100001", source, Fixtures.createActivityType("INTERVENTION"));
        activity3.setDescription("");

        plannedActivity1 = new PlannedActivity();
        plannedActivity1.setGridId("4def4863-ab9c-4bbe-97ce-b06ceaac3209");
        plannedActivity1.setActivity(activity1);
        plannedActivity1.setDay(1);

        plannedActivity3 = new PlannedActivity();
        plannedActivity3.setGridId("1672abe3-44e9-40a7-af32-1fd4f0b9cd54");
        plannedActivity3.setActivity(activity3);
        plannedActivity3.setDay(11);
        plannedActivity3.setDetails("Subcutaneously once daily");

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

        period3 = new Period();
        period3.setGridId("04fed30f-3cda-4843-936a-b5d9d9d07492");


        period4 = Fixtures.createPeriod(null, 1, Duration.Unit.day, 24, 3);
        period4.setGridId("773f67c7-8c9a-4c39-b9db-efd9a2e93e83");
        period4.addPlannedActivity(plannedActivity3);

        studySegment1 = Fixtures.createNamedInstance("A", StudySegment.class);
        studySegment1.setGridId("0400e3b7-76b6-4ecb-88a1-61dcb5ecbae9");

        studySegment2 = Fixtures.createNamedInstance("Transplant", StudySegment.class);
        studySegment2.setGridId("111f8f6c-3d5e-4bf8-99bf-1b7122ace4d0");

        studySegment1.getPeriods().add(period1);
        studySegment1.getPeriods().add(period2);
        studySegment2.getPeriods().add(period2);


        epoch1 = Fixtures.createNamedInstance("Treatment", Epoch.class);
        epoch1.addStudySegment(studySegment1);
        epoch1.addStudySegment(studySegment2);
        epoch1.setGridId("690361c1-433e-4a25-bfe2-09db0ce2edab");

        add1 = Fixtures.createAddChange(1, 0);
        add1.setChild(epoch1);
        add1.setGridId("cb6e3130-9d2e-44e8-80ac-170d1875db5c");

        reorder = Reorder.create(epoch1, 2, 1);
        reorder.setGridId("2c845525-46cb-4c05-a0d8-9de0e866b61c");

        plannedCalendar = new PlannedCalendar();
        plannedCalendar.setGridId("846046d8-088f-4504-95db-1fc1a6c5fba5");

        plannedCalendarDelta = new PlannedCalendarDelta(plannedCalendar);
        plannedCalendarDelta.setGridId("59124f7c-b8d2-4ee5-8a54-b767673df42d");
        plannedCalendarDelta.addChange(add1);
        plannedCalendarDelta.addChange(reorder);

        add2 = createAddChange(1, null);
        add2.setChild(plannedActivity3);
        add2.setGridId("2e79d035-f641-46c6-b35e-9bbe3f487177");


        add3 = createAddChange(1, null);
        add3.setChild(period4);
        add3.setGridId("796d9a10-8cac-4810-b487-d5a651b0c603");

        propertyChange1 = PropertyChange.create("name", "Cycle", "Cycle 1");
        propertyChange1.setGridId("ce171370-8fde-4c85-915e-75207135e690");


        periodDelta = new PeriodDelta(period3);
        periodDelta.setGridId("0f1d1e5b-410a-4120-81fc-7c95eee590fe");
        periodDelta.addChange(add2);

        periodDelta.addChange(propertyChange1);

        studySegmentDelta = new StudySegmentDelta(studySegment1);
        studySegmentDelta.setGridId("9ff37859-fa10-4632-972b-915246d0bd03");
        studySegmentDelta.addChange(add3);

        epochDelta = new EpochDelta(epoch1);
        epochDelta.setGridId("6b2d06a3-f521-4ef8-9cae-3eb73f6f6bf4");
        epochDelta.addChange(add1);


        calendar = Fixtures.setGridId("grid1", new PlannedCalendar());
        population = Fixtures.createPopulation("MP", "My Population");


        amendment = Fixtures.createAmendment("[Second Expected]", edu.nwu.bioinformatics.commons.DateUtils.createDate(2008, Calendar.AUGUST, 16), true);
        firstAmendment = Fixtures.createAmendment("[First]", edu.nwu.bioinformatics.commons.DateUtils.createDate(2008, Calendar.JANUARY, 2), true);


        study = createStudy();


        activityXmlSerializer = new ActivityXmlSerializer();

        plannedActivityXmlSerializer = new PlannedActivityXmlSerializer();
        plannedActivityXmlSerializer.setActivityXmlSerializer(activityXmlSerializer);
        plannedActivityXmlSerializer.setActivityReferenceXmlSerializer(new ActivityReferenceXmlSerializer());
        plannedActivityXmlSerializer.setPlannedActivityLabelXmlSerializer(new PlannedActivityLabelXmlSerializer());

        periodXmlSerializer = new PeriodXmlSerializer();
        periodXmlSerializer.setChildXmlSerializer(plannedActivityXmlSerializer);

        studySegmentXmlSerializer = new StudySegmentXmlSerializer();
        studySegmentXmlSerializer.setChildXmlSerializer(periodXmlSerializer);

        epochXmlSerializer = new EpochXmlSerializer();
        epochXmlSerializer.setChildXmlSerializer(studySegmentXmlSerializer);

        changeableXmlSerializerFactory = new ChangeableXmlSerializerFactory() {

            @Override
            public AbstractStudyCalendarXmlSerializer createXmlSerializer(Element node) {
                if (PlannedCalendarXmlSerializer.PLANNED_CALENDAR.equals(node.getName())) {
                    return null;
                } else if (EpochXmlSerializer.EPOCH.equals(node.getName())) {
                    return epochXmlSerializer;
                } else if (StudySegmentXmlSerializer.STUDY_SEGMENT.equals(node.getName())) {
                    return studySegmentXmlSerializer;
                } else if (PeriodXmlSerializer.PERIOD.equals(node.getName())) {
                    return periodXmlSerializer;
                } else if (PlannedActivityXmlSerializer.PLANNED_ACTIVITY.equals(node.getName())) {
                    return plannedActivityXmlSerializer;
                } else {
                    throw new StudyCalendarError("Problem importing template. Could not find node type %s", node.getName());
                }
            }

            public StudyCalendarXmlSerializer createXmlSerializer(final Changeable node) {
                if (node instanceof Epoch) {
                    return epochXmlSerializer;
                } else if (node instanceof StudySegment) {
                    return studySegmentXmlSerializer;
                } else if (node instanceof Period) {
                    return periodXmlSerializer;
                } else if (node instanceof PlannedActivity) {
                    return plannedActivityXmlSerializer;
                } else {
                    throw new StudyCalendarError("Problem importing template. Cannot find Child Node for Change");
                }
            }
        };
        reorderXmlSerializer = new ReorderXmlSerializer();
        reorderXmlSerializer.setChangeableXmlSerializerFactory(changeableXmlSerializerFactory);
        addXmlSerializer = new AddXmlSerializer();
        addXmlSerializer.setChangeableXmlSerializerFactory(changeableXmlSerializerFactory);
        propertyChangeXmlSerializer = new PropertyChangeXmlSerializer();
        propertyChangeXmlSerializer.setChangeableXmlSerializerFactory(changeableXmlSerializerFactory);
        changeXmlSerializerFactory = new ChangeXmlSerializerFactory() {
            @Override
            public ChangeXmlSerializer createXmlSerializer(Element eChange) {
                if ((AddXmlSerializer.ADD).equals(eChange.getName())) {
                    return addXmlSerializer;
                } else if ((RemoveXmlSerializer.REMOVE).equals(eChange.getName())) {
                    return removeXmlSerializer;
                } else if ((ReorderXmlSerializer.REORDER).equals(eChange.getName())) {
                    return reorderXmlSerializer;
                } else if ((PropertyChangeXmlSerializer.PROPERTY_CHANGE).equals(eChange.getName())) {
                    return propertyChangeXmlSerializer;
                } else {
                    throw new StudyCalendarError("Problem processing template. Change is not recognized: %s", eChange.getName());
                }

            }

            @Override
            public ChangeXmlSerializer createXmlSerializer(Change change) {
                if ((ChangeAction.ADD).equals(change.getAction())) {
                    return addXmlSerializer;
                } else if ((ChangeAction.REMOVE).equals(change.getAction())) {
                    return removeXmlSerializer;
                } else if (ChangeAction.REORDER.equals(change.getAction())) {
                    return reorderXmlSerializer;
                } else if (ChangeAction.CHANGE_PROPERTY.equals(change.getAction())) {
                    return propertyChangeXmlSerializer;
                } else {
                    throw new StudyCalendarError("Problem processing template. Change is not recognized: %s", change.getAction());
                }
            }
        };

        plannedCalendarDeltaXmlSerializer =
            DefaultDeltaXmlSerializer.create(DeltaNodeType.PLANNED_CALENDAR, changeXmlSerializerFactory);
        studySegmentDeltaXmlSerializer =
            DefaultDeltaXmlSerializer.create(DeltaNodeType.STUDY_SEGMENT, changeXmlSerializerFactory);
        periodDeltaXmlSerializer =
            DefaultDeltaXmlSerializer.create(DeltaNodeType.PERIOD, changeXmlSerializerFactory);
        epochDeltaXmlSerializer =
            DefaultDeltaXmlSerializer.create(DeltaNodeType.EPOCH, changeXmlSerializerFactory);

        deltaSerializerFactory = new DeltaXmlSerializerFactory() {
            @Override
            public DeltaXmlSerializer createXmlSerializer(Element delta) {
                if (XsdElement.PLANNED_CALENDAR_DELTA.xmlName().equals(delta.getName())) {
                    return plannedCalendarDeltaXmlSerializer;
                } else if (XsdElement.EPOCH_DELTA.xmlName().equals(delta.getName())) {
                    return epochDeltaXmlSerializer;
                } else if (XsdElement.STUDY_SEGMENT_DELTA.xmlName().equals(delta.getName())) {
                    return studySegmentDeltaXmlSerializer;
                } else if (XsdElement.PERIOD_DELTA.xmlName().equals(delta.getName())) {
                    return periodDeltaXmlSerializer;
                } else if (XsdElement.PLANNED_ACTIVITY_DELTA.xmlName().equals(delta.getName())) {
                    return null;
                } else {
                    throw new StudyCalendarError("Problem importing template. Could not find delta type %s", delta.getName());
                }

            }

            @Override
            public DeltaXmlSerializer createXmlSerializer(Delta delta) {
                if (delta instanceof PlannedCalendarDelta) {
                    return plannedCalendarDeltaXmlSerializer;
                } else if (delta instanceof EpochDelta) {
                    return epochDeltaXmlSerializer;
                } else if (delta instanceof StudySegmentDelta) {
                    return studySegmentDeltaXmlSerializer;
                } else if (delta instanceof PeriodDelta) {
                    return periodDeltaXmlSerializer;
                } else if (delta instanceof PlannedActivityDelta) {
                    return null;
                } else {
                    throw new StudyCalendarError("Problem importing template. Could not find delta type");
                }

            }
        };
        amendmentSerializer = new AmendmentXmlSerializer() {
            public DeltaXmlSerializerFactory getDeltaXmlSerializerFactory() {
                return deltaSerializerFactory;
            }

        };
        studyXmlserializer = new StudyXmlSerializer() {
            protected PlannedCalendarXmlSerializer getPlannedCalendarXmlSerializer() {
                return plannedCalendarSerializer;
            }

            public AmendmentXmlSerializer getAmendmentSerializer(Study study) {
//                amendmentSerializer.setStudy(study);
                return amendmentSerializer;
            }

            protected AmendmentXmlSerializer getDevelopmentAmendmentSerializer(Study study) {
                developmentAmendmentSerializer.setStudy(study);
                developmentAmendmentSerializer.setDevelopmentAmendment(true);
                return developmentAmendmentSerializer;
            }
        };

        populationSerializer = new PopulationXmlSerializer();
        plannedCalendarSerializer = new PlannedCalendarXmlSerializer();
        plannedCalendarSerializer.setEpochXmlSerializer(epochXmlSerializer);
        developmentAmendmentSerializer = new AmendmentXmlSerializer();
        developmentAmendmentSerializer.setStudy(study);

       changeableXmlSerializerFactory.setBeanFactory(beanFactory);
    }


    private Study createStudy() {
        Study study = Fixtures.createNamedInstance("Study A", Study.class);
        study.setPlannedCalendar(calendar);
        study.addPopulation(population);
        return study;
    }


}
