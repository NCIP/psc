/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public interface DaoFinder {
    /**
     * Locates the DAO for the given class.  If there's no matching DAO, throw an exception.
     */
    <T extends DomainObject> DomainObjectDao<?> findDao(Class<T> klass);

    <T extends ChangeableDao> List<ChangeableDao<?>> findStudyCalendarMutableDomainObjectDaos();
}
