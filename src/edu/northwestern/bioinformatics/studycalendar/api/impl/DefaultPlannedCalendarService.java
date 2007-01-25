package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.northwestern.bioinformatics.studycalendar.api.PlannedCalendarService;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rhett Sutphin
 */
@Transactional
public class DefaultPlannedCalendarService implements PlannedCalendarService {
    private StudyDao studyDao;
    private PlannedCalendarDao plannedCalendarDao;
    private TemplateSkeletonCreator defaultTemplateCreator;
    private SiteDao siteDao;
    private TemplateService templateService;

    public DefaultPlannedCalendarService() {
        defaultTemplateCreator = TemplateSkeletonCreator.BASIC;
    }

    public PlannedCalendar registerStudy(Study study) {
        if (study.getBigId() == null) throw createRegistrationError("study missing bigId");
        if (study.getName() == null) throw createRegistrationError("study missing name");
        PlannedCalendar existing = getPlannedCalendar(study);
        if (existing != null) {
            mergeSiteAssignments(study, existing.getStudy());
            studyDao.save(existing.getStudy());
            return existing;
        }

        Study registered = defaultTemplateCreator.create();
        registered.setName(study.getName());
        registered.setBigId(study.getBigId());
        mergeSiteAssignments(study, registered);
        studyDao.save(registered);

        return registered.getPlannedCalendar();
    }

    private void mergeSiteAssignments(Study source, Study target) {
        List<Site> sourceSites = loadOrCreate(source.getSites());
        List<Site> targetSites = target.getSites();
        if (targetSites.containsAll(sourceSites) && targetSites.size() == sourceSites.size()) {
            return;
        }

        for (Site sourceSite : sourceSites) {
            if (!targetSites.contains(sourceSite)) {
                target.addSite(sourceSite);
            }
        }

        List<Site> toRemove = new LinkedList<Site>();
        for (Site targetSite : targetSites) {
            if (!sourceSites.contains(targetSite)) {
                toRemove.add(targetSite);
            }
        }
        if (toRemove.size() > 0) templateService.removeTemplateFromSites(target, toRemove);
    }

    private List<Site> loadOrCreate(List<Site> parameterSites) {
        List<Site> loaded = new ArrayList<Site>(parameterSites.size());
        for (Site parameterSite : parameterSites) {
            if (parameterSite.getBigId() == null) {
                throw createRegistrationError("site missing bigId");
            }
            Site loadedSite = siteDao.getByBigId(parameterSite.getBigId());
            if (loadedSite == null) {
                if (parameterSite.getName() == null) {
                    throw createRegistrationError("new site missing name");
                }
                parameterSite.setId(null); // just in case
                siteDao.save(parameterSite);
                loaded.add(parameterSite);
            } else {
                loaded.add(loadedSite);
            }
        }
        return loaded;
    }

    private IllegalArgumentException createRegistrationError(String submsg) {
        return new IllegalArgumentException("Cannot register study: " + submsg);
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

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
