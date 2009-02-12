package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;

/**
 * @author Rhett Sutphin
*/
public interface TemplateSkeletonCreator {
    TemplateSkeletonCreator BLANK = new TemplateSkeletonCreatorImpl.Blank();
    TemplateSkeletonCreator BASIC = new TemplateSkeletonCreatorImpl.Basic();

    Study create(String studyName);
}
 