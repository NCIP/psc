/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.northwestern.bioinformatics.studycalendar.api.PlannedCalendarService;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Transactional
public class DefaultPlannedCalendarService implements PlannedCalendarService {
    private StudyDao studyDao;
    private PlannedCalendarDao plannedCalendarDao;
    private TemplateSkeletonCreator defaultTemplateCreator;
    private SiteDao siteDao;
    private StudySiteService studySiteService;

    public DefaultPlannedCalendarService() {
        defaultTemplateCreator = TemplateSkeletonCreator.BASIC;
    }

    public PlannedCalendar registerStudy(Study study) {
        if (study.getGridId() == null) throw createRegistrationError("study missing gridId");
        if (study.getName() == null) throw createRegistrationError("study missing name");
        PlannedCalendar existing = getPlannedCalendar(study);
        if (existing != null) {
            mergeSiteAssignments(study, existing.getStudy());
            studyDao.save(existing.getStudy());
            return existing;
        }

        Study registered;
        if (study.getPlannedCalendar() == null) {
            registered = defaultTemplateCreator.create(null);
        } else {
            registered = new Study();
            registered.setPlannedCalendar(study.getPlannedCalendar());
        }
        registered.setName(study.getName());
        registered.setGridId(study.getGridId());
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
        if (toRemove.size() > 0) studySiteService.removeStudyFromSites(target, toRemove);
    }

    private List<Site> loadOrCreate(List<Site> parameterSites) {
        List<Site> loaded = new ArrayList<Site>(parameterSites.size());
        for (Site parameterSite : parameterSites) {
            if (parameterSite.getGridId() == null) {
                throw createRegistrationError("site missing gridId");
            }
            Site loadedSite = siteDao.getByGridId(parameterSite.getGridId());
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
        if (study.getGridId() == null)
            throw new IllegalArgumentException("Cannot locate planned calendar for a study without a gridId");
        Study systemStudy = studyDao.getByGridId(study.getGridId());
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

    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }
}
