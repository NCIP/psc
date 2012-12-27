/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

/**
 * Variant of {@link MutatorFactory} suitable for use with all-in-memory revisions.
 * Does not provide DAOs to mutators, so they will have no opportunity to resolve
 * IDs.
 *
 * @see edu.northwestern.bioinformatics.studycalendar.domain.delta.Add#getChild
 * @see edu.northwestern.bioinformatics.studycalendar.domain.delta.Add#getChildId
 * @see edu.northwestern.bioinformatics.studycalendar.core.Fixtures
 * @author Rhett Sutphin
 */
public class MemoryOnlyMutatorFactory extends MutatorFactory {
    @Override
    protected <T extends PlanTreeNode<?>> DomainObjectDao<?> findDao(Class<T> klass) {
        return null;
    }

    @Override
    protected TemplateService getTemplateService() {
        return new TestingTemplateService();
    }

    @Override
    protected ScheduleService getScheduleService() {
        return null;
    }

    @Override
    protected SubjectService getSubjectService() {
        return null;
    }
}
