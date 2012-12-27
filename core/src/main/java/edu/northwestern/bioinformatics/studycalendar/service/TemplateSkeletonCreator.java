/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

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
 