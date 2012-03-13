package edu.northwestern.bioinformatics.studycalendar.osgi.felixcm.internal;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.hibernate.StudyCalendarNamingStrategy;
import org.apache.commons.collections15.EnumerationUtils;
import org.apache.felix.cm.PersistenceManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author Rhett Sutphin
 */
public class PscFelixPersistenceManager extends HibernateDaoSupport implements PersistenceManager {
    private static final Collection<String> EXCLUDED_PROPERTIES =
        Arrays.asList("service.bundleLocation");
    private HibernateTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    ////// DECLARATIVE SERVICE LIFECYCLE METHODS

    protected void initializeSessionFactory(DataSource dataSource) throws Exception {
        AnnotationSessionFactoryBean sessionFactoryBean = new AnnotationSessionFactoryBean();
        sessionFactoryBean.setNamingStrategy(new StudyCalendarNamingStrategy());
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setAnnotatedClasses(new Class[] { OsgiConfigurationProperty.class });
        sessionFactoryBean.afterPropertiesSet();
        setSessionFactory(sessionFactoryBean.getObject());

        transactionManager = new HibernateTransactionManager(getSessionFactory());
        transactionManager.afterPropertiesSet();

        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.afterPropertiesSet();
    }

    protected void destroySessionFactory() {
        getSessionFactory().close();
        transactionManager = null;
        transactionTemplate = null;
    }

    ////// IMPLEMENT PersistenceManager

    public boolean exists(final String pid) {
        return getHibernateTemplate().execute(new HibernateCallback<Boolean>() {
            public Boolean doInHibernate(Session session) throws HibernateException, SQLException {
                Long count = (Long) session.createQuery(
                    "select COUNT(*) from OsgiConfigurationProperty p where p.servicePid = :pid"
                ).setString("pid", pid).uniqueResult();
                return count > 0;
            }
        });
    }

    @SuppressWarnings({"unchecked"})
    public Dictionary load(String pid) throws IOException {
        List<OsgiConfigurationProperty> list = getProperties(pid);
        if (list.isEmpty()) {
            throw new IOException("No configuration for PID=\"" + pid + "\"");
        }
        Dictionary out = new Hashtable();
        for (OsgiConfigurationProperty property : list) {
            if (!EXCLUDED_PROPERTIES.contains(property.getName())) {
                out.put(property.getName(), property.getValue());
            }
        }
        return out;
    }

    @SuppressWarnings({"unchecked"})
    public Enumeration getDictionaries() throws IOException {
        // ordering is all for testing purposes
        List<OsgiConfigurationProperty> all
            = getHibernateTemplate().find("from OsgiConfigurationProperty p order by p.servicePid, p.name");
        Map<String, Dictionary> byPid = new LinkedHashMap<String, Dictionary>();
        for (OsgiConfigurationProperty property : all) {
            if (!EXCLUDED_PROPERTIES.contains(property.getName())) {
                if (!byPid.containsKey(property.getServicePid())) {
                    byPid.put(property.getServicePid(), new Hashtable());
                }
                byPid.get(property.getServicePid()).put(property.getName(), property.getValue());
            }
        }
        return new Vector<Dictionary>(byPid.values()).elements();
    }

    @SuppressWarnings({"unchecked"})
    public void store(final String pid, final Dictionary values) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                doStore(pid, values);
            }
        });
    }

    @SuppressWarnings({"unchecked"})
    private void doStore(String pid, Dictionary values) {
        List<OsgiConfigurationProperty> existingProperties = getProperties(pid);
        Collection<String> newKeys = EnumerationUtils.toList(values.keys());
        Collection<String> existingKeys = new HashSet<String>();
        for (OsgiConfigurationProperty existingProperty : existingProperties) {
            String existingKey = existingProperty.getName();
            if (newKeys.contains(existingKey)) {
                existingProperty.setValue(values.get(existingKey));
                existingKeys.add(existingKey);
            } else {
                getHibernateTemplate().delete(existingProperty);
            }
        }
        for (String newKey : newKeys) {
            if (!EXCLUDED_PROPERTIES.contains(newKey) && !existingKeys.contains(newKey)) {
                getHibernateTemplate().save(
                    OsgiConfigurationProperty.create(pid, newKey, values.get(newKey)));
            }
        }
    }

    public void delete(final String pid) throws IOException {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                // bulk delete doesn't cascade for some reason
                getHibernateTemplate().deleteAll(getProperties(pid));
            }
        });
    }

    @SuppressWarnings({"unchecked"})
    private List<OsgiConfigurationProperty> getProperties(final String pid) {
        return (List<OsgiConfigurationProperty>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery(
                    "from OsgiConfigurationProperty p where p.servicePid = :pid"
                ).setString("pid", pid).list();
            }
        });
    }
}
