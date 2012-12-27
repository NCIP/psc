/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools.hibernate;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.slf4j.Logger;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Hibernate {@link org.hibernate.usertype.UserType} that loads
 * {@link edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState}
 * values for the
 * {@link edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity#getCurrentState}
 * property polymorphically.
 *
 * @author Rhett Sutphin
 */
public class ScheduledActivityStateType implements CompositeUserType {
    private static Logger log = HibernateTypeUtils.getLog(ScheduledActivityStateType.class);
    private static final Type MODE_TYPE = Hibernate.custom(ControlledVocabularyObjectType.class, new String[] { "enumClass" }, new String[] { ScheduledActivityMode.class.getName() });

    private static final String[] PROPERTY_NAMES = new String[] { "mode", "reason", "date", "withTime" };
    private static final Type[] PROPERTY_TYPES = new Type[] { MODE_TYPE, Hibernate.STRING, Hibernate.TIMESTAMP, Hibernate.BINARY };
    private static final int MODE_INDEX   = 0;
    private static final int REASON_INDEX = 1;
    private static final int DATE_INDEX   = 2;
    private static final int WITH_TIME_INDEX   = 3;

    public String[] getPropertyNames() { return PROPERTY_NAMES; }

    public Type[] getPropertyTypes() { return PROPERTY_TYPES; }

    public Object getPropertyValue(Object component, int property) throws HibernateException {
        String name = getPropertyNames()[property];
        if (PropertyUtils.isReadable(component, name)) {
            try {
                return PropertyUtils.getProperty(component, name);
            } catch (IllegalAccessException e) {
                throw new HibernateException("Failed to read property " + name + " from " + component, e);
            } catch (InvocationTargetException e) {
                throw new HibernateException("Failed to read property " + name + " from " + component, e);
            } catch (NoSuchMethodException e) {
                throw new HibernateException("Failed to read property " + name + " from " + component, e);
            }
        } else {
            return null;
        }
    }

    public void setPropertyValue(Object component, int property, Object value) throws HibernateException {
        String name = getPropertyNames()[property];
        if (PropertyUtils.isWriteable(component, name)) {
            try {
                PropertyUtils.setProperty(component, name, value);
            } catch (NoSuchMethodException e) {
                throw new HibernateException("Failed to set property " + name + " on " + component + " with value " + value, e);
            } catch (IllegalAccessException e) {
                throw new HibernateException("Failed to set property " + name + " on " + component + " with value " + value, e);
            } catch (InvocationTargetException e) {
                throw new HibernateException("Failed to set property " + name + " on " + component + " with value " + value, e);
            }
        }
    }

    public Class returnedClass() {
        return ScheduledActivityState.class;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return ComparisonTools.nullSafeEquals(x, y);
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        // we don't know the owner yet, but that's okay because the MODE_TYPE doesn't use it.
        ScheduledActivityMode mode = (ScheduledActivityMode) MODE_TYPE.nullSafeGet(rs, names[MODE_INDEX], session, null);
        ScheduledActivityState loaded = createStateObject(mode);
        if (loaded == null) return null;

        loaded.setReason(rs.getString(names[REASON_INDEX]));
        loaded.setDate(rs.getTimestamp(names[DATE_INDEX]));
        loaded.setWithTime(rs.getBoolean(names[WITH_TIME_INDEX]));

        return loaded;
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        ScheduledActivityState toSet = (ScheduledActivityState) value;
        Timestamp date = null;
        String reason = null;
        ScheduledActivityMode mode = null;
        Boolean withTime = false;
        if (toSet != null) {
            mode = toSet.getMode();
            reason = toSet.getReason();

            Date dateToSet = toSet.getDate();
            date = dateToSet == null ? null : new Timestamp(dateToSet.getTime());
            if (toSet.getWithTime()) {
                withTime = true;
            }
        }

        HibernateTypeUtils.logBind(log, index + DATE_INDEX, date);
        st.setTimestamp(index + DATE_INDEX, date);

        HibernateTypeUtils.logBind(log, index + WITH_TIME_INDEX, withTime);
        st.setBoolean(index + WITH_TIME_INDEX, withTime);

        HibernateTypeUtils.logBind(log, index + REASON_INDEX, reason);
        st.setString(index + REASON_INDEX, reason);

        MODE_TYPE.nullSafeSet(st, mode, index + MODE_INDEX, session);
    }

    private ScheduledActivityState createStateObject(ScheduledActivityMode mode) {
        return mode == null ? null : mode.createStateInstance();
    }

    public Object replace(Object original, Object target, SessionImplementor session, Object owner) throws HibernateException {
        return deepCopy(original);
    }

    public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
        return cached;
    }

    public Serializable disassemble(Object value, SessionImplementor session) throws HibernateException {
        return (Serializable) value;
    }

    public boolean isMutable() {
        return true;
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value == null ? null : ((ScheduledActivityState) value).clone();
    }
}
