package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
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

	@Override
	protected Object getCommand(HttpServletRequest request) throws Exception {
		return new CopyStudyCommand(studyService);
	}

	@Override
	protected ModelAndView handle(CopyStudyCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
		int studyId = ServletRequestUtils.getRequiredIntParameter(request, "study");
		Integer selectedAmendmentId = ServletRequestUtils.getIntParameter(request, "amendment");
		Study study = studyDao.getById(studyId);

		if (study != null) {
			try {
				Study copiedStudy = command.create(study, selectedAmendmentId);
				return getControllerTools().redirectToCalendarTemplate(copiedStudy.getId(), null, copiedStudy.getDevelopmentAmendment().getId());
			} catch (StudyCalendarValidationException scve) {
				log.error(scve.getMessage());
				errors.reject(scve.getMessage());
			}


		} else {
			String errorMessage = "Can not find study for given id:" + studyId;
			log.error(errorMessage);

			errors.reject(errorMessage);
		}
		return null;
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