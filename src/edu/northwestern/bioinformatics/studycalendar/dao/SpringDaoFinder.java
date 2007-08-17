package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.BeansException;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

/**
 * Builds an index of all the DAOs in the project and allows retrieval based on the types they
 * manage.
 *
 * @author Rhett Sutphin
 */
public class SpringDaoFinder implements BeanFactoryPostProcessor, DaoFinder {
    private Map<Class<? extends DomainObject>, DomainObjectDao<?>> byClass;

    public SpringDaoFinder() {
        byClass = new HashMap<Class<? extends DomainObject>, DomainObjectDao<?>>();
    }

    @SuppressWarnings({ "unchecked" })
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Map<String, DomainObjectDao<?>> daos = beanFactory.getBeansOfType(DomainObjectDao.class, false, false);
        for (DomainObjectDao<?> dao : daos.values()) {
            byClass.put(dao.domainClass(), dao);
        }
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends DomainObject> DomainObjectDao<T> findDao(Class<T> klass) {
        DomainObjectDao<T> match = (DomainObjectDao<T>) byClass.get(klass);
        if (match == null) throw new StudyCalendarSystemException("There is no DAO registered for %s", klass.getName());
        return match;
    }
}
