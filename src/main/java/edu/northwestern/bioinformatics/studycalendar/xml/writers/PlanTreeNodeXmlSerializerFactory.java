package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import org.dom4j.Element;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.BeansException;

public class PlanTreeNodeXmlSerializerFactory implements BeanFactoryAware {
    private BeanFactory beanFactory;
    private Study study;
    private static final String PLANNED_CALENDAR_SERIALIZER = "plannedCalendarXmlSerializer";
    private static final String EPOCH_SERIALIZER = "epochXmlSerializer";
    private static final String STUDY_SEGMENT_SERIALIZER = "studySegmentXmlSerializer";
    private static final String PERIOD_SERIALIZER = "periodXmlSerializer";
    private static final String PLANNED_ACTIVITY_SERIALIZER = "plannedActivityXmlSerializer";

    // TODO: add to application context and get serializers from application context
    public AbstractPlanTreeNodeXmlSerializer createXmlSerializer(final Element node) {
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
        } else {
            throw new StudyCalendarError("Problem importing template. Could not find node type %s", node.getName());
        }
    }

    public AbstractPlanTreeNodeXmlSerializer createXmlSerializer(final PlanTreeNode<?> node) {
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
        } else {
            throw new StudyCalendarError("Problem importing template. Cannot find Child Node for Change");
        }
    }

    private AbstractPlanTreeNodeXmlSerializer getXmlSerialzier(String beanName) {
        AbstractPlanTreeNodeXmlSerializer serializer = (AbstractPlanTreeNodeXmlSerializer) beanFactory.getBean(beanName);
        serializer.setStudy(study);
        return serializer;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
