package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;

import java.util.Date;

/**
 * @author Rhett Sutphin
*/
public interface TemplateSkeletonCreator {
    TemplateSkeletonCreator BLANK = new TemplateSkeletonCreatorImpl.Blank();
    TemplateSkeletonCreator BASIC = new TemplateSkeletonCreatorImpl.Basic();

    Study create();
}

 