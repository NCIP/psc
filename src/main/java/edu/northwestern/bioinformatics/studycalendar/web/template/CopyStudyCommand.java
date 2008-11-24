package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;

/**
 * @author Saurabh Agrawal
 */
public class CopyStudyCommand {
	private StudyService studyService;

	public CopyStudyCommand(StudyService studyService) {
		this.studyService = studyService;
	}

	public Study create(Study study, final Integer selectedAmendmentId) {

		Study copiedStudy = studyService.copy(study, selectedAmendmentId);

		return copiedStudy;

	}

}