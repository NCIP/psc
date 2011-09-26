package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class PlanTreeNodeXmlSerializerFactory implements BeanFactoryAware {
    private BeanFactory beanFactory;
    private static final String PLANNED_CALENDAR_SERIALIZER = "plannedCalendarXmlSerializer";
    private static final String EPOCH_SERIALIZER = "epochXmlSerializer";
    private static final String STUDY_SEGMENT_SERIALIZER = "studySegmentXmlSerializer";
    private static final String PERIOD_SERIALIZER = "periodXmlSerializer";
    private static final String PLANNED_ACTIVITY_SERIALIZER = "plannedActivityXmlSerializer";
    private static final String POPULATION_SERIALIZER = "populationXmlSerializer";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public StudyCalendarXmlSerializer createXmlSerializer(final Element node) {
        if (PlannedCalendarXmlSerializer.PLANNED_CALENDAR.equals(node.getName())) {
            return getXmlSerializer(PLANNED_CALENDAR_SERIALIZER);
        } else if (EpochXmlSerializer.EPOCH.equals(node.getName())) {
            return getXmlSerializer(EPOCH_SERIALIZER);
        } else if (StudySegmentXmlSerializer.STUDY_SEGMENT.equals(node.getName())) {
            return getXmlSerializer(STUDY_SEGMENT_SERIALIZER);
        } else if (PeriodXmlSerializer.PERIOD.equals(node.getName())) {
            return getXmlSerializer(PERIOD_SERIALIZER);
        } else if(PlannedActivityXmlSerializer.PLANNED_ACTIVITY.equals(node.getName())) {
            return getXmlSerializer(PLANNED_ACTIVITY_SERIALIZER);
        } else if(PopulationXmlSerializer.POPULATION.equals(node.getName())) {
            return getXmlSerializer(POPULATION_SERIALIZER);
        } else {
            throw new StudyCalendarError("Could not build XML serializer for element %s.", node.getName());
        }
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public StudyCalendarXmlSerializer createXmlSerializer(final Changeable node) {
        if (node instanceof PlannedCalendar) {
            return getXmlSerializer(PLANNED_CALENDAR_SERIALIZER);
        } else if (node instanceof Epoch) {
            return getXmlSerializer(EPOCH_SERIALIZER);
        } else if (node instanceof StudySegment) {
            return getXmlSerializer(STUDY_SEGMENT_SERIALIZER);
        } else if (node instanceof Period) {
            return getXmlSerializer(PERIOD_SERIALIZER);
        } else if (node instanceof PlannedActivity) {
            return getXmlSerializer(PLANNED_ACTIVITY_SERIALIZER);
        } else if (node instanceof Population) {
            return getXmlSerializer(POPULATION_SERIALIZER);
        } else {
            throw new StudyCalendarError("Could not build XML serializer for changeable %s.", node);
        }
    }

    // package level for testing
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    StudyCalendarXmlSerializer getXmlSerializer(String beanName) {
        return (StudyCalendarXmlSerializer) beanFactory.getBean(beanName);
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
