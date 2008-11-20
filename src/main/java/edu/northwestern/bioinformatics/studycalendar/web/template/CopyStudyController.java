package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Saurabh Agrawal
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class CopyStudyController extends PscAbstractCommandController<CopyStudyCommand> {
    private StudyService studyService;
    private StudyDao studyDao;
    private DeltaService deltaService;

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new CopyStudyCommand(studyService);
    }

    @Override
    protected ModelAndView handle(CopyStudyCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        int studyId = ServletRequestUtils.getRequiredIntParameter(request, "study");
        Integer selectedAmendmentId = ServletRequestUtils.getIntParameter(request, "amendment");
        Study study = studyDao.getById(studyId);
        Study revisedStudy = study;

        if (study != null) {
            Amendment amendment = null;
            if (selectedAmendmentId == null) {
                amendment = study.getAmendment();
            } else if (study.getDevelopmentAmendment() != null && selectedAmendmentId.equals(study.getDevelopmentAmendment().getId())) {
                amendment = study.getDevelopmentAmendment();
                revisedStudy = deltaService.revise(study, amendment);

            }


            if (amendment == null) {
                errors.reject("Can not find amendment for given amendment id:" + amendment);
                return null;
            }
            Study copiedStudy = command.create(revisedStudy);
            return getControllerTools().redirectToCalendarTemplate(copiedStudy.getId(), null, copiedStudy.getDevelopmentAmendment().getId());


        } else {
            errors.reject("Can not find study for given id:" + studyId);
        }
        return null;
    }

    @Required
    public void setDeltaService(final DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setStudyDao(final StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }
}