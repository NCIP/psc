package edu.northwestern.bioinformatics.studycalendar.configuration;

import org.apache.commons.collections15.EnumerationUtils;
import org.apache.felix.cm.PersistenceManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.SQLException;
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
@Transactional(readOnly = true)
public class PscFelixPersistenceManager extends HibernateDaoSupport implements PersistenceManager {
    public boolean exists(final String pid) {
        return (Boolean) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
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
            out.put(property.getName(), property.getValue());
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
            if (!byPid.containsKey(property.getServicePid())) {
                byPid.put(property.getServicePid(), new Hashtable());
            }
            byPid.get(property.getServicePid()).put(property.getName(), property.getValue());
        }
        System.out.println(byPid.values());
        return new Vector<Dictionary>(byPid.values()).elements();
    }

    @SuppressWarnings({"unchecked"})
    @Transactional(readOnly = false)
    public void store(String pid, Dictionary values) throws IOException {
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
            if (!existingKeys.contains(newKey)) {
                getHibernateTemplate().save(
                    OsgiConfigurationProperty.create(pid, newKey, values.get(newKey)));
            }
        }
    }

    @Transactional(readOnly = false)
    public void delete(final String pid) throws IOException {
        // bulk delete doesn't cascade for some reason
        getHibernateTemplate().deleteAll(getProperties(pid));
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
