package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import org.dom4j.Element;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.BeansException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlanTreeNodeXmlSerializerFactory implements BeanFactoryAware {
    private BeanFactory beanFactory;
    private static final String PLANNED_CALENDAR_SERIALIZER = "plannedCalendarXmlSerializer";
    private static final String EPOCH_SERIALIZER = "epochXmlSerializer";
    private static final String STUDY_SEGMENT_SERIALIZER = "studySegmentXmlSerializer";
    private static final String PERIOD_SERIALIZER = "periodXmlSerializer";
    private static final String PLANNED_ACTIVITY_SERIALIZER = "plannedActivityXmlSerializer";
    private static final String POPULATION_SERIALIZER = "populationXmlSerializer";

    private final Logger log = LoggerFactory.getLogger(getClass());

    public AbstractStudyCalendarXmlSerializer createXmlSerializer(final Element node) {
        if (PlannedCalendarXmlSerializer.PLANNED_CALENDAR.equals(node.getName())) {
            return getXmlSerialzier(PLANNED_CALENDAR_SERIALIZER);
        } else if (EpochXmlSerializer.EPOCH.equals(node.getName())) {
            return getXmlSerialzier(EPOCH_SERIALIZER);
        } else if (StudySegmentXmlSerializer.STUDY_SEGMENT.equals(node.getName())) {
            return getXmlSerialzier(STUDY_SEGMENT_SERIALIZER);
        } else if (PeriodXmlSerializer.PERIOD.equals(node.getName())) {
            return getXmlSerialzier(PERIOD_SERIALIZER);
        } else if(PlannedActivityXmlSerializer.PLANNED_ACTIVITY.equals(node.getName())) {
            return getXmlSerialzier(PLANNED_ACTIVITY_SERIALIZER);
        } else if(PopulationXmlSerializer.POPULATION.equals(node.getName())) {
            return getXmlSerialzier(POPULATION_SERIALIZER);
        } else {
            throw new StudyCalendarError("Problem importing template. Could not find node type %s", node.getName());
        }
    }

    public StudyCalendarXmlSerializer createXmlSerializer(final Changeable node) {
        if (node instanceof PlannedCalendar) {
            return getXmlSerialzier(PLANNED_CALENDAR_SERIALIZER);
        } else if (node instanceof Epoch) {
            return getXmlSerialzier(EPOCH_SERIALIZER);
        } else if (node instanceof StudySegment) {
            return getXmlSerialzier(STUDY_SEGMENT_SERIALIZER);
        } else if (node instanceof Period) {
            return getXmlSerialzier(PERIOD_SERIALIZER);
        } else if (node instanceof PlannedActivity) {
            return getXmlSerialzier(PLANNED_ACTIVITY_SERIALIZER);
        } else if (node instanceof Population) {
            return getXmlSerialzier(POPULATION_SERIALIZER);
        } else {
            throw new StudyCalendarError("Problem importing template. Cannot find Child Node for Change");
        }
    }

    private AbstractStudyCalendarXmlSerializer getXmlSerialzier(String beanName) {
        AbstractStudyCalendarXmlSerializer serializer = (AbstractStudyCalendarXmlSerializer) beanFactory.getBean(beanName);
        return serializer;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
