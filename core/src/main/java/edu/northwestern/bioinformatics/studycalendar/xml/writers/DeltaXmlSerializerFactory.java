package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
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

    private final Logger log = LoggerFactory.getLogger(getClass());

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public DeltaXmlSerializer createXmlSerializer(final Delta delta) {
        if (delta instanceof PlannedCalendarDelta) {
            return getXmlSerializer(PLANNED_CALENDAR_DELTA_SERIALIZER);
        } else if (delta instanceof EpochDelta) {
            return getXmlSerializer(EPOCH_DELTA_SERIALIZER);
        } else if (delta instanceof StudySegmentDelta) {
            return getXmlSerializer(STUDY_SEGMENT_DELTA_SERIALIZER);
        } else if (delta instanceof PeriodDelta) {
            return getXmlSerializer(PERIOD_DELTA_SERIALIZER);
        } else if (delta instanceof PlannedActivityDelta) {
            return getXmlSerializer(PLANNED_ACTIVITY_DELTA_SERIALIZER);
        } else if (delta instanceof PopulationDelta) {
            return getXmlSerializer(POPULATION_DELTA_SERIALIZER);
        } else if (delta instanceof StudyDelta) {
            return getXmlSerializer(STUDY_DELTA_SERIALIZER);
        } else {
            throw new StudyCalendarError("Could not build XML serializer for delta %s.", delta);
        }
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public DeltaXmlSerializer createXmlSerializer(final Element delta) {
        if (PlannedCalendarDeltaXmlSerializer.PLANNED_CALENDAR_DELTA.equals(delta.getName())) {
            return getXmlSerializer(PLANNED_CALENDAR_DELTA_SERIALIZER);
        } else if (EpochDeltaXmlSerializer.EPOCH_DELTA.equals(delta.getName())) {
            return getXmlSerializer(EPOCH_DELTA_SERIALIZER);
        } else if (StudySegmentDeltaXmlSerializer.STUDY_SEGMENT_DELTA.equals(delta.getName())) {
            return getXmlSerializer(STUDY_SEGMENT_DELTA_SERIALIZER);
        } else if (PeriodDeltaXmlSerializer.PERIOD_DELTA.equals(delta.getName())) {
            return getXmlSerializer(PERIOD_DELTA_SERIALIZER);
        } else if(PlannedActivityDeltaXmlSerializer.PLANNED_ACTIVITY_DELTA.equals(delta.getName())) {
            return getXmlSerializer(PLANNED_ACTIVITY_DELTA_SERIALIZER);
        } else if (PopulationDeltaXmlSerializer.POPULATION_DELTA.equals(delta.getName())) {
            return getXmlSerializer(POPULATION_DELTA_SERIALIZER);
        }  else if (StudyDeltaXmlSerializer.STUDY_DELTA.equals(delta.getName())) {
            return getXmlSerializer(STUDY_DELTA_SERIALIZER);
        } else {
            throw new StudyCalendarError(
                "Could not build XML serializer for element %s.", delta.getName());
        }
    }

    // package level for testing
    DeltaXmlSerializer getXmlSerializer(String beanName) {
        return (DeltaXmlSerializer) beanFactory.getBean(beanName);
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
