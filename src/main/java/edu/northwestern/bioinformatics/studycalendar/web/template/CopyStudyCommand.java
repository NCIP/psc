package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;

/**
 * @author Saurabh Agrawal
 */
public class CopyStudyCommand {
    private StudyService studyService;

    public CopyStudyCommand(StudyService studyService) {
        this.studyService = studyService;
    }

    public Study create(Study study) {

        Study copiedStudy = studyService.copy(study);

        return copiedStudy;

    }

}