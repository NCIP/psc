/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.ChangeableDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This service provides orchestration methods for incrementally building up templates.
 * Methods should be added here for complex template modifications (more than just
 * adding/removing/moving/updating single nodes).  For simpler things, you can just
 * directly invoke {@link AmendmentService#updateDevelopmentAmendment}. 
 *
 * @see TemplateService
 * @author Rhett Sutphin
 */
@Transactional(readOnly = false)
public class TemplateDevelopmentService {
    private TemplateService templateService;
    private AmendmentService amendmentService;
    private DeltaService deltaService;
    private DaoFinder daoFinder;
    private StudyDao studyDao;
    private StudyService studyService;
    private AmendmentDao amendmentDao;


    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Copies the given period to the target segment (by updating the development amendment for
     * the study to which the target segment belongs).
     * @param source
     * @param target
     * @return the newly created copy period.  It will have already been added to <code>target</code>.
     */
    public Period copyPeriod(Period source, StudySegment target) {
        Study sourceStudy = templateService.findStudy(source);
        Study targetStudy = templateService.findStudy(target);
        boolean intraStudy = sourceStudy.equals(targetStudy);

        if (intraStudy) {
            source = deltaService.revise(source);
        }

        Period copy = source.clone();
        copy.clearIds();
        copy.setParent(null);

        // convert Populations to comments
        if (!intraStudy) {
            for (PlannedActivity pa : copy.getPlannedActivities()) {
                updateCopiedPlannedActivity(pa, sourceStudy);
            }
        }

        amendmentService.updateDevelopmentAmendmentAndSave(target, Add.create(copy));
        return copy;
    }



    public void purgeOrphanTemplateElements(){
        List<ChangeableDao<?>> daos = daoFinder.findStudyCalendarMutableDomainObjectDaos();
        for (ChangeableDao dao : daos) {
            dao.deleteOrphans();
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteDevelopmentAmendment(Study study) {
        purgeOrphanTemplateElements();
        deleteDevelopmentAmendment(study.getDevelopmentAmendment());
        if (study.getAmendment() == null) {
            templateService.delete(study.getPlannedCalendar());
            studyDao.delete(study);
        } else {
            study.setDevelopmentAmendment(null);
            studyService.save(study);
        }
    }

    /**
     * Deletes the development amendment for the designated study.  Even if the
     * study has no released amendment, it does not delete the study and the study's
     * planned calendar.
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteDevelopmentAmendmentOnly(Study study) {
        deleteDevelopmentAmendment(study.getDevelopmentAmendment());
        study.setDevelopmentAmendment(null);
        studyService.save(study);
    }

    private void deleteDevelopmentAmendment(Amendment dev) {
        if (dev != null) {
            for (Delta<?> delta : dev.getDeltas()) {
                deltaService.delete(delta);
            }
            amendmentDao.delete(dev);
        }
    }


    private void updateCopiedPlannedActivity(PlannedActivity pa, Study sourceStudy) {
        if (pa.getPopulation() != null) {
            String msg = createCopiedWithPopulationMessage(sourceStudy, pa);
            if (pa.getDetails() == null) {
                pa.setDetails(msg);
            } else {
                pa.setDetails(
                    new StringBuilder(pa.getDetails()).append(" (").append(msg).append(')').toString()
                );
            }
            pa.setPopulation(null);
        }
    }

    private String createCopiedWithPopulationMessage(Study study, PlannedActivity pa) {
        return new StringBuilder().append("Copied from ")
            .append(study.getAssignedIdentifier())
            .append(", where it was restricted to ")
            .append(pa.getPopulation().getAbbreviation())
            .append(": ")
            .append(pa.getPopulation().getName())
            .toString();
    }

    ////// CONFIGURATION

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

}
