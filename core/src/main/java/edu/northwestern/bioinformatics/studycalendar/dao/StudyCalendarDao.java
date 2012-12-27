/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import gov.nih.nci.cabig.ctms.dao.AbstractDomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.hibernate.criterion.DetachedCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarDao<T extends DomainObject> extends AbstractDomainObjectDao<T> {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected DetachedCriteria criteria() {
        return DetachedCriteria.forClass(domainClass());
    }
}
