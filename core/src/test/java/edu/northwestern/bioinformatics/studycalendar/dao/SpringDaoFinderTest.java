package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import gov.nih.nci.cabig.ctms.dao.AbstractDomainObjectDao;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.AbstractImmutableDomainObject;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.TypeHelper;
import org.hibernate.classic.Session;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import javax.naming.NamingException;
import javax.naming.Reference;
import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class SpringDaoFinderTest extends StudyCalendarTestCase {
    private SpringDaoFinder finder;
    private DefaultListableBeanFactory factory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        finder = new SpringDaoFinder();
        factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition("sessionFactory", new RootBeanDefinition(StubSessionFactory.class));
        factory.registerBeanDefinition("testOneDao", new RootBeanDefinition(TestOneDao.class, RootBeanDefinition.AUTOWIRE_BY_TYPE));
        factory.registerBeanDefinition("testTwoDao", new RootBeanDefinition(TestTwoDao.class, RootBeanDefinition.AUTOWIRE_BY_TYPE));
        factory.registerBeanDefinition("testTwoSubDao", new RootBeanDefinition(TestTwoPointOneDao.class, RootBeanDefinition.AUTOWIRE_BY_TYPE));
        factory.registerBeanDefinition("testDeletableDao", new RootBeanDefinition(TestDeletableDao.class, RootBeanDefinition.AUTOWIRE_BY_TYPE));
        factory.registerBeanDefinition("testChangeableDao", new RootBeanDefinition(TestChangeableDao.class, RootBeanDefinition.AUTOWIRE_BY_TYPE));
        factory.registerBeanDefinition("someOtherBean", new RootBeanDefinition(Object.class));
        finder.postProcessBeanFactory(factory);
    }

    public void testFindWhenItExists() throws Exception {
        DomainObjectDao<?> actualOne = finder.findDao(TestOne.class);
        assertNotNull(actualOne);
        assertEquals(actualOne.getClass(), TestOneDao.class);

        DomainObjectDao<?> actualTwo = finder.findDao(TestTwo.class);
        assertNotNull(actualTwo);
        assertEquals(actualTwo.getClass(), TestTwoDao.class);
    }

    public void testFindWhenItDoesNotExist() throws Exception {
        try {
            finder.findDao(NoDao.class);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("There is no DAO registered for " + NoDao.class.getName(), e.getMessage());
        }
    }

    public void testFindForUnregisteredSubclass() throws Exception {
        DomainObjectDao<?> actual = finder.findDao(TestOnePointOne.class);
        assertNotNull(actual);
        assertEquals(TestOneDao.class, actual.getClass());
    }

    public void testFindForRegisteredSubclass() throws Exception {
        DomainObjectDao<?> actual = finder.findDao(TestTwoPointOne.class);
        assertNotNull(actual);
        assertEquals(TestTwoPointOneDao.class, actual.getClass());
    }

    private static class TestOne extends AbstractImmutableDomainObject { }
    private static class TestOnePointOne extends TestOne { }
    private static class TestTwo extends AbstractMutableDomainObject { }
    private static class TestTwoPointOne extends TestTwo { }
    private static class NoDao extends AbstractImmutableDomainObject { }

    private static class TestOneDao extends AbstractDomainObjectDao<TestOne> {
        @Override public Class<TestOne> domainClass() { return TestOne.class; }
    }
    private static class TestTwoDao extends AbstractDomainObjectDao<TestTwo> {
        @Override public Class<TestTwo> domainClass() { return TestTwo.class; }
    }
    private static class TestTwoPointOneDao extends AbstractDomainObjectDao<TestTwoPointOne> {
        @Override public Class<TestTwoPointOne> domainClass() { return TestTwoPointOne.class; }
    }


    public void testFindDeletableDomainObjectDao() throws Exception {
        DeletableDomainObjectDao<?> actual = finder.findDeletableDomainObjectDao(TestDeletable.class);
        assertNotNull(actual);
        assertEquals(TestDeletableDao.class, actual.getClass());
    }
    private static class TestDeletable extends AbstractMutableDomainObject {}
    private static class TestDeletableDao extends AbstractDomainObjectDao<TestDeletable> implements DeletableDomainObjectDao<TestDeletable> {
        @Override public Class<TestDeletable> domainClass() { return TestDeletable.class;}

        public void delete(TestDeletable deletableOne) {}

        public void deleteAll(List<TestDeletable> t) {}

        public void save(TestDeletable t) {}

        public TestDeletable getByGridId(String s) {return null;}
    }

    public void testFindChangeableDomainObjectDao() throws Exception {
        List<ChangeableDao<?>> actual = finder.findStudyCalendarMutableDomainObjectDaos();
        assertEquals(1, actual.size());
        assertEquals(TestChangeableDao.class, actual.get(0).getClass());
    }

    private static class TestChangeable extends AbstractMutableDomainObject implements Changeable {
        public boolean isDetached() {return false;}
        public boolean isMemoryOnly() {return false;}
        public void setMemoryOnly(boolean memoryOnly) {}
        public Changeable transientClone() {return null;}
    }

    private static class TestChangeableDao extends ChangeableDao<TestChangeable> {
        @Override public Class<TestChangeable> domainClass() { return TestChangeable.class;}
        public void deleteOrphans() {}
        public void delete(TestChangeable testChangeable) {}
        public void deleteAll(List<TestChangeable> t) {}
    }

    private static class StubSessionFactory implements SessionFactory {
        public Session openSession(Connection connection) {
            throw new UnsupportedOperationException("openSession not implemented");
        }

        public Session openSession(Interceptor interceptor) throws HibernateException {
            throw new UnsupportedOperationException("openSession not implemented");
        }

        public Session openSession(Connection connection, Interceptor interceptor) {
            throw new UnsupportedOperationException("openSession not implemented");
        }

        public Session openSession() throws HibernateException {
            throw new UnsupportedOperationException("openSession not implemented");
        }

        public Session getCurrentSession() throws HibernateException {
            throw new UnsupportedOperationException("getCurrentSession not implemented");
        }

        public ClassMetadata getClassMetadata(Class aClass) throws HibernateException {
            throw new UnsupportedOperationException("getClassMetadata not implemented");
        }

        public ClassMetadata getClassMetadata(String string) throws HibernateException {
            throw new UnsupportedOperationException("getClassMetadata not implemented");
        }

        public CollectionMetadata getCollectionMetadata(String string) throws HibernateException {
            throw new UnsupportedOperationException("getCollectionMetadata not implemented");
        }

        public Map getAllClassMetadata() throws HibernateException {
            throw new UnsupportedOperationException("getAllClassMetadata not implemented");
        }

        public Map getAllCollectionMetadata() throws HibernateException {
            throw new UnsupportedOperationException("getAllCollectionMetadata not implemented");
        }

        public Statistics getStatistics() {
            throw new UnsupportedOperationException("getStatistics not implemented");
        }

        public void close() throws HibernateException {
            throw new UnsupportedOperationException("close not implemented");

        }

        public boolean isClosed() {
            throw new UnsupportedOperationException("isClosed not implemented");
        }

        public void evict(Class aClass) throws HibernateException {
            throw new UnsupportedOperationException("evict not implemented");

        }

        public void evict(Class aClass, Serializable serializable) throws HibernateException {
            throw new UnsupportedOperationException("evict not implemented");

        }

        public void evictEntity(String string) throws HibernateException {
            throw new UnsupportedOperationException("evictEntity not implemented");

        }

        public void evictEntity(String string, Serializable serializable) throws HibernateException {
            throw new UnsupportedOperationException("evictEntity not implemented");

        }

        public void evictCollection(String string) throws HibernateException {
            throw new UnsupportedOperationException("evictCollection not implemented");

        }

        public void evictCollection(String string, Serializable serializable) throws HibernateException {
            throw new UnsupportedOperationException("evictCollection not implemented");

        }

        public void evictQueries() throws HibernateException {
            throw new UnsupportedOperationException("evictQueries not implemented");

        }

        public void evictQueries(String string) throws HibernateException {
            throw new UnsupportedOperationException("evictQueries not implemented");

        }

        public StatelessSession openStatelessSession() {
            throw new UnsupportedOperationException("openStatelessSession not implemented");
        }

        public StatelessSession openStatelessSession(Connection connection) {
            throw new UnsupportedOperationException("openStatelessSession not implemented");
        }

        public Set getDefinedFilterNames() {
            throw new UnsupportedOperationException("getDefinedFilterNames not implemented");
        }

        public FilterDefinition getFilterDefinition(String string) throws HibernateException {
            throw new UnsupportedOperationException("getFilterDefinition not implemented");
        }

        public Reference getReference() throws NamingException {
            throw new UnsupportedOperationException("getReference not implemented");
        }

        public Cache getCache() {
            throw new UnsupportedOperationException("getCache not implemented");
        }

        public boolean containsFetchProfileDefinition(String s) {
            throw new UnsupportedOperationException("containsFetchProfileDefinition not implemented");
        }

        public TypeHelper getTypeHelper() {
            throw new UnsupportedOperationException("getTypeHelper not implemented");
        }
    }
}
