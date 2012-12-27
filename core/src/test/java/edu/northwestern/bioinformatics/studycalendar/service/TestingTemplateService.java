/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;

/**
 * @author Rhett Sutphin
*/
public class TestingTemplateService extends TemplateService {
    @Override
    public <P extends Parent> P findParent(Child<P> node) {
        return node.getParent();
    }
}
