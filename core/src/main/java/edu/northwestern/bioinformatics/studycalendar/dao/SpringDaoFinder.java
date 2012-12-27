/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.*;

/**
 * Builds an index of all the DAOs in the project and allows retrieval based on the types they
 * manage.
 *
 * @author Rhett Sutphin
 */
public class SpringDaoFinder implements BeanFactoryPostProcessor, DaoFinder {
   private final Logger log = LoggerFactory.getLogger(getClass());
    private SortedSet<DaoEntry> entries;

    public SpringDaoFinder() {
        entries = new TreeSet<DaoEntry>();
    }

    @SuppressWarnings({ "unchecked" })
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Map<String, DomainObjectDao> daos = beanFactory.getBeansOfType(DomainObjectDao.class, false, false);
        for (DomainObjectDao dao : daos.values()) {
            entries.add(new DaoEntry(dao));
        }
        log.debug("Registered DAOs: {}", entries);
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends DomainObject> DomainObjectDao<?> findDao(Class<T> domainClass) {
        for (DaoEntry entry : entries) {
            // since the entries are sorted by specificity, we can return the first match
            if (entry.matches(domainClass)) return entry.getDao();
        }
        throw new StudyCalendarSystemException("There is no DAO registered for %s", domainClass.getName());
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends ChangeableDao> List<ChangeableDao<?>> findStudyCalendarMutableDomainObjectDaos() {
        List<ChangeableDao<?>> daos = new ArrayList<ChangeableDao<?>>();
        for (DaoEntry entry : entries) {
            // since the entries are sorted by specificity, we can return the first match
            if (entry.matchesDaoSuperclas(ChangeableDao.class)) {
                daos.add((ChangeableDao)entry.getDao());
            }
        }
        if (daos.isEmpty()) {
            throw new StudyCalendarSystemException("There is no DAO registered for %s", StudyCalendarMutableDomainObjectDao.class.getName());
        } else {
            return daos;
        }
    }

    public <T extends MutableDomainObject> DeletableDomainObjectDao<?> findDeletableDomainObjectDao(Class<T> domainClass) {
        for (DaoEntry entry : entries) {
            // since the entries are sorted by specificity, we can return the first match
            if (DeletableDomainObjectDao.class.isAssignableFrom(entry.getDao().getClass()) && entry.matches(domainClass)) {
                return (DeletableDomainObjectDao) entry.getDao();
            }
        }
        throw new StudyCalendarSystemException("There is no DAO registered for %s", domainClass.getName());
    }

    private static class DaoEntry implements Comparable<DaoEntry> {
        private DomainObjectDao<?> dao;
        private final Logger log = LoggerFactory.getLogger(getClass());
        private int specificity; // depth from java.lang.Object to the DAO's domain class

        public DaoEntry(DomainObjectDao<?> dao) {
            this.dao = dao;
            specificity = countSuperclasses(dao.domainClass(), 0);
        }

        private int countSuperclasses(Class<?> klass, int count) {
            if (klass == null) return count;
            return countSuperclasses(klass.getSuperclass(), count + 1);
        }


        public boolean matchesDaoSuperclas(Class<?> domainClass) {
            return domainClass.isAssignableFrom(dao.getClass());
        }

        public boolean matches(Class<?> domainClass) {
            return getDao().domainClass().isAssignableFrom(domainClass);
        }

        // sort from more specific to less
        public int compareTo(DaoEntry o) {
            int specDiff = o.specificity - specificity;
            if (specDiff != 0) return specDiff;
            return dao.domainClass().getName().compareTo(o.dao.domainClass().getName());
        }

        public DomainObjectDao<?> getDao() {
            return dao;
        }

        @Override
        public String toString() {
            return new StringBuilder(getClass().getSimpleName())
                .append('[').append(getDao().getClass().getSimpleName())
                .append("; specificity: ").append(specificity).append(']')
                .toString();
        }
    }}
