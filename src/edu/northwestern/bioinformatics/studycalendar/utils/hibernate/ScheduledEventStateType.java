package edu.northwestern.bioinformatics.studycalendar.utils.hibernate;

import edu.nwu.bioinformatics.commons.ComparisonUtils;

import org.hibernate.usertype.CompositeUserType;
import org.hibernate.type.Type;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import edu.northwestern.bioinformatics.studycalendar.utils.hibernate.ControlledVocabularyObjectType;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.DatedScheduledEventState;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Date;
import java.io.Serializable;

/**
 * Hibernate {@link org.hibernate.usertype.UserType} that loads
 * {@link edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState}
 * values for the
 * {@link edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent#getCurrentState}
 * property polymorphically.
 *
 * @author Rhett Sutphin
 */
public class ScheduledEventStateType implements CompositeUserType {
    private static Log log = HibernateTypeUtils.getLog(ScheduledEventStateType.class);
    private static final Type MODE_TYPE = Hibernate.custom(ControlledVocabularyObjectType.class, new String[] { "enumClass" }, new String[] { ScheduledEventMode.class.getName() });

    private static final String[] PROPERTY_NAMES = new String[] { "mode", "reason", "date" };
    private static final Type[] PROPERTY_TYPES = new Type[] { MODE_TYPE, Hibernate.STRING, Hibernate.TIMESTAMP };
    private static final int MODE_INDEX   = 0;
    private static final int REASON_INDEX = 1;
    private static final int DATE_INDEX   = 2;

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
        return ScheduledEventState.class;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return ComparisonUtils.nullSafeEquals(x, y);
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        // we don't know the owner yet, but that's okay because the MODE_TYPE doesn't use it.
        ScheduledEventMode mode = (ScheduledEventMode) MODE_TYPE.nullSafeGet(rs, names[MODE_INDEX], session, null);
        ScheduledEventState loaded = createStateObject(mode);
        if (loaded == null) return null;

        loaded.setReason(rs.getString(names[REASON_INDEX]));
        if (loaded instanceof DatedScheduledEventState) {
            ((DatedScheduledEventState) loaded).setDate(rs.getTimestamp(names[DATE_INDEX]));
        }
        return loaded;
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        ScheduledEventState toSet = (ScheduledEventState) value;
        java.sql.Date date = null;
        String reason = null;
        ScheduledEventMode mode = null;
        if (toSet != null) {
            mode = toSet.getMode();
            reason = toSet.getReason();
            if (toSet instanceof DatedScheduledEventState) {
                Date dateToSet = ((DatedScheduledEventState) toSet).getDate();
                date = dateToSet == null ? null : new java.sql.Date(dateToSet.getTime());
            }
        }

        HibernateTypeUtils.logBind(log, index + DATE_INDEX, date);
        st.setDate(index + DATE_INDEX, date);

        HibernateTypeUtils.logBind(log, index + REASON_INDEX, reason);
        st.setString(index + REASON_INDEX, reason);

        MODE_TYPE.nullSafeSet(st, mode, index + MODE_INDEX, session);
    }

    private ScheduledEventState createStateObject(ScheduledEventMode mode) {
        if (ScheduledEventMode.SCHEDULED == mode) {
            return new Scheduled();
        } else if (ScheduledEventMode.OCCURRED == mode) {
            return new Occurred();
        } else if (ScheduledEventMode.CANCELED == mode) {
            return new Canceled();
        } else {
            return null;
        }
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
        return value == null ? null : ((ScheduledEventState) value).clone();
    }
}
