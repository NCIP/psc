package edu.northwestern.bioinformatics.studycalendar.domain.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import gov.nih.nci.cabig.ctms.audit.util.AuditUtil;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
import org.hibernate.EntityMode;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;

import javax.persistence.Transient;

/**
 * @author Jalpa Patel
 */
public class AuditEvent extends DataAuditEvent{
    private static final EntityMode ENTITY_MODE = EntityMode.POJO;
    private static final String HIBERNATE_BACK_REF_STRING = "Backref";
    private volatile static ThreadLocal<UserAction> userAction = new ThreadLocal<UserAction>();
    private String userActionId;

    public AuditEvent(Object entity, Operation operation, DataAuditInfo info) {
        super(entity, operation, info);
    }

    public AuditEvent(Object entity, Operation operation, DataAuditInfo info, UserAction userAction) {
        super(entity, operation, info);
        this.userActionId = userAction == null ? null : userAction.getGridId();
    }

    public static UserAction getUserAction() {
        return userAction.get();
    }

    public static void setUserAction(final UserAction ua) {
        userAction.set(ua);
    }

    public String getUserActionId() {
        return userActionId;
    }

    public void setUserActionId(String userActionId) {
        this.userActionId = userActionId;
    }

    @Transient
    public void appendEventValues(final Type propertyType, final String propertyName,
                                   final Object previousState, final Object currentState) {
        if (ignoreCurrentStateForType(propertyType) || propertyName.indexOf(HIBERNATE_BACK_REF_STRING) > 0) {
            return;
        } else if (propertyType.isComponentType()) {
            addComponentValues((AbstractComponentType) propertyType, propertyName, previousState, currentState);
        } else {
            if (previousState == null && currentState == null) {
                return;
            } else {
                String prevValue = scalarAuditableValue(previousState);
                String curValue = scalarAuditableValue(currentState);
                if (prevValue != null && curValue != null && prevValue.equals(curValue)) {
                    return;
                }
                addValue(new DataAuditEventValue(propertyName, prevValue, curValue));
            }
        }
    }

    private void addComponentValues(final AbstractComponentType propertyType,
                                  final String propertyName, final Object previousState, final Object currentState) {
        Object[] componentPrevState = previousState == null ? null : propertyType.getPropertyValues(previousState,
            ENTITY_MODE);
        Object[] componentCurState = currentState == null ? null : propertyType.getPropertyValues(currentState,
            ENTITY_MODE);

        if (componentPrevState == null && componentCurState == null) {
            return;
        } else {
            int index = componentCurState == null ? componentPrevState.length : componentCurState.length;
            for (int i=0; i<index; i++) {
                String compPropertyName = propertyName.concat(".").concat(propertyType.getPropertyNames()[i]);
                if (componentPrevState != null && componentCurState != null) {
                    if (!ComparisonTools.nullSafeEquals(componentCurState[i], componentPrevState[i])) {
                        addValue(new DataAuditEventValue(compPropertyName,
                            scalarAuditableValue(componentPrevState[i]),
                            scalarAuditableValue(componentCurState[i])));
                    }
                } else {
                    addValue(new DataAuditEventValue(compPropertyName,
                        componentPrevState == null ? null : scalarAuditableValue(componentPrevState[i]),
                        componentCurState == null ? null : scalarAuditableValue(componentCurState[i])));
                }
            }
        }
    }

    private boolean ignoreCurrentStateForType(final Type type) {
        return type instanceof CollectionType;
    }

    private String scalarAuditableValue(final Object propertyValue) {
        if (propertyValue == null) {
            return null;
        } else if (AuditUtil.getObjectId(propertyValue) != null) {
            Integer id = AuditUtil.getObjectId(propertyValue);
            return id.toString();
        } else {
            return propertyValue.toString();
        }
    }
}
