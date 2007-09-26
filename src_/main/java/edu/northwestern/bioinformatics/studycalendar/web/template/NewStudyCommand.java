package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommand {
    private TemplateSkeletonCreator base;
    private StudyService studyService;

    public NewStudyCommand(StudyService studyService) {
        this.studyService = studyService;
    }

    public Study create() {
        Study study = getBase().create();
        studyService.save(study);
        return study;
    }

    public TemplateSkeletonCreator getBase() {
        if (base == null) return TemplateSkeletonCreator.BASIC;
        return base;
    }

    public void setBase(TemplateSkeletonCreator base) {
        this.base = base;
    }

}
