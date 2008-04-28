package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommand {
    private TemplateSkeletonCreator base;
    private StudyService studyService;

               private final Logger log = LoggerFactory.getLogger(getClass());

    public NewStudyCommand(StudyService studyService) {
        this.studyService = studyService;
    }

    public Study create() {
        String newStudyName = studyService.getNewStudyName();
        Study study = getBase().create(newStudyName);

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
