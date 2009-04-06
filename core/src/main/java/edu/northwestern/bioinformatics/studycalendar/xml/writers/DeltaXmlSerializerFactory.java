package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.EpochDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PeriodDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedActivityDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PopulationDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.StudyDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.StudySegmentDelta;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class DeltaXmlSerializerFactory implements BeanFactoryAware {
    private final String PLANNED_CALENDAR_DELTA_SERIALIZER = "plannedCalendarDeltaXmlSerializer";
    private final String EPOCH_DELTA_SERIALIZER = "epochDeltaXmlSerializer";
    private final String STUDY_SEGMENT_DELTA_SERIALIZER = "studySegmentDeltaXmlSerializer";
    private final String PERIOD_DELTA_SERIALIZER = "periodDeltaXmlSerializer";
    private final String PLANNED_ACTIVITY_DELTA_SERIALIZER = "plannedActivityDeltaXmlSerializer";
    private final String POPULATION_DELTA_SERIALIZER = "populationDeltaXmlSerializer";

    private final String STUDY_DELTA_SERIALIZER = "studyDeltaXmlSerializer";

    private BeanFactory beanFactory;
    private Study study;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public AbstractDeltaXmlSerializer createXmlSerializer(final Delta delta) {
        if (delta instanceof PlannedCalendarDelta) {
            return getXmlSerialzier(PLANNED_CALENDAR_DELTA_SERIALIZER);
        } else if (delta instanceof EpochDelta) {
            return getXmlSerialzier(EPOCH_DELTA_SERIALIZER);
        } else if (delta instanceof StudySegmentDelta) {
            return getXmlSerialzier(STUDY_SEGMENT_DELTA_SERIALIZER);
        } else if (delta instanceof PeriodDelta) {
            return getXmlSerialzier(PERIOD_DELTA_SERIALIZER);
        } else if (delta instanceof PlannedActivityDelta) {
            return getXmlSerialzier(PLANNED_ACTIVITY_DELTA_SERIALIZER);
        } else if (delta instanceof PopulationDelta) {
            return getXmlSerialzier(POPULATION_DELTA_SERIALIZER);
        } else if (delta instanceof StudyDelta) {
            return getXmlSerialzier(STUDY_DELTA_SERIALIZER);
        }
        else {
            throw new StudyCalendarError("Problem importing template. Could not find delta type");
        }
    }

    public AbstractDeltaXmlSerializer createXmlSerializer(final Element delta) {
        if (PlannedCalendarDeltaXmlSerializer.PLANNED_CALENDAR_DELTA.equals(delta.getName())) {
            return getXmlSerialzier(PLANNED_CALENDAR_DELTA_SERIALIZER);
        } else if (EpochDeltaXmlSerializer.EPOCH_DELTA.equals(delta.getName())) {
            return getXmlSerialzier(EPOCH_DELTA_SERIALIZER);
        } else if (StudySegmentDeltaXmlSerializer.STUDY_SEGMENT_DELTA.equals(delta.getName())) {
            return getXmlSerialzier(STUDY_SEGMENT_DELTA_SERIALIZER);
        } else if (PeriodDeltaXmlSerializer.PERIOD_DELTA.equals(delta.getName())) {
            return getXmlSerialzier(PERIOD_DELTA_SERIALIZER);
        } else if(PlannedActivityDeltaXmlSerializer.PLANNED_ACTIVITY_DELTA.equals(delta.getName())) {
            return getXmlSerialzier(PLANNED_ACTIVITY_DELTA_SERIALIZER);
        } else if (PopulationDeltaXmlSerializer.POPULATION_DELTA.equals(delta.getName())) {
            return getXmlSerialzier(POPULATION_DELTA_SERIALIZER);
        }  else if (StudyDeltaXmlSerializer.STUDY_DELTA.equals(delta.getName())) {
            return getXmlSerialzier(STUDY_DELTA_SERIALIZER);
        }else {
            throw new StudyCalendarError("Problem importing template. Could not find delta type %s", delta.getName());
        }
    }

    private AbstractDeltaXmlSerializer getXmlSerialzier(String beanName) {
        AbstractDeltaXmlSerializer serializer = (AbstractDeltaXmlSerializer) beanFactory.getBean(beanName);
        serializer.setStudy(study);
        return serializer;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
