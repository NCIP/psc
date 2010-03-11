package edu.northwestern.bioinformatics.studycalendar.service.importer;

import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * @author Jalpa Patel
 */
public class GridIdentifierResolver<T extends MutableDomainObject>{
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private DaoFinder daoFinder;

    public Boolean resolveGridId(Class<T> klass, String gridId) {
        StudyCalendarMutableDomainObjectDao<T> dao
            = (StudyCalendarMutableDomainObjectDao<T>) daoFinder.findDao(klass);
        List<T> nodes = dao.getAll();
        for (T node: nodes) {
            if (gridId != null && gridId.equals(node.getGridId())) {
                log.debug("Node {} with grid identifier {} already exists in system", klass, gridId);
                return true;
            }
        }
        log.debug("Node {} with grid identifier {} doesn't exists in system", klass, gridId);
        return false;
    }

    public DaoFinder getDaoFinder() {
        return daoFinder;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
