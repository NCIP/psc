package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.northwestern.bioinformatics.studycalendar.api.PlannedCalendarService;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;

/**
 * @author Rhett Sutphin
 */
public class DefaultPlannedCalendarService implements PlannedCalendarService {
    private StudyDao studyDao;
    private PlannedCalendarDao plannedCalendarDao;
    private TemplateSkeletonCreator defaultTemplateCreator;

    public DefaultPlannedCalendarService() {
        defaultTemplateCreator = TemplateSkeletonCreator.BASIC;
    }

    public PlannedCalendar registerStudy(Study study) {
        if (study.getBigId() == null) throw new IllegalArgumentException("Cannot register study: no bigId");
        if (study.getName() == null) throw new IllegalArgumentException("Cannot register study: no name");
        PlannedCalendar existing = getPlannedCalendar(study);
        if (existing != null) return existing;

        Study registered = defaultTemplateCreator.create();
        registered.setName(study.getName());
        registered.setBigId(study.getBigId());
        studyDao.save(registered);

        return registered.getPlannedCalendar();
    }

    public PlannedCalendar getPlannedCalendar(Study study) {
        if (study.getBigId() == null) throw new IllegalArgumentException("Cannot locate planned calendar for a study without a bigId");
        Study systemStudy = studyDao.getByBigId(study.getBigId());
        if (systemStudy == null) return null;

        plannedCalendarDao.initialize(systemStudy.getPlannedCalendar());
        return systemStudy.getPlannedCalendar();
    }

    ////// CONFIGURATION

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setPlannedCalendarDao(PlannedCalendarDao plannedCalendarDao) {
        this.plannedCalendarDao = plannedCalendarDao;
    }

    public void setDefaultTemplateCreator(TemplateSkeletonCreator defaultTemplateCreator) {
        this.defaultTemplateCreator = defaultTemplateCreator;
    }

    // getter for testing
    TemplateSkeletonCreator getDefaultTemplateCreator() {
        return defaultTemplateCreator;
    }
}
