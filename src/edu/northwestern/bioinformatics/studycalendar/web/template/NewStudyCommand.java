package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommand {
    private StudyDao studyDao;
    private TemplateSkeletonCreator base;

    public NewStudyCommand(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public Study create() {
        Study study = getBase().create();
        studyDao.save(study);
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
