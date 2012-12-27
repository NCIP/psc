/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.importer;

import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Jalpa Patel
 */
public class GridIdentifierResolver {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private DaoFinder daoFinder;

    public <T extends MutableDomainObject> Boolean resolveGridId(Class<T> klass, String gridId) {
        StudyCalendarMutableDomainObjectDao<T> dao
            = (StudyCalendarMutableDomainObjectDao<T>) daoFinder.findDao(klass);
        T existing = dao.getByGridId(gridId);
        if (existing == null) {
            log.debug("Node {} with grid identifier {} doesn't exists in system", klass, gridId);
            return false;
        } else {
            log.debug("Node {} with grid identifier {} already exists in system", klass, gridId);
            return true;
        }
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
