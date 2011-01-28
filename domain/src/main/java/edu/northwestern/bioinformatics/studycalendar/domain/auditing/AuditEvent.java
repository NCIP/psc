package edu.northwestern.bioinformatics.studycalendar.domain.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;
import gov.nih.nci.cabig.ctms.audit.domain.*;
import gov.nih.nci.cabig.ctms.audit.util.AuditUtil;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
import org.hibernate.EntityMode;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;

import javax.persistence.Transient;
import java.util.Date;

/**
 * @author Jalpa Patel
 */
public class AuditEvent extends DataAuditEvent implements Comparable<AuditEvent> {
    private static final EntityMode ENTITY_MODE = EntityMode.POJO;
    private static final String HIBERNATE_BACK_REF_STRING = "Backref";
    private volatile static ThreadLocal<UserAction> userAction = new ThreadLocal<UserAction>();
    private String userActionId;
    private Operation operation;

    public AuditEvent() {}

    public AuditEvent(Object entity, Operation operation, DataAuditInfo info) {
        super(entity, operation, info);
        this.operation = operation;
    }

    public AuditEvent(Object entity, Operation operation, DataAuditInfo info, UserAction userAction) {
        super(entity, operation, info);
        this.operation = operation;
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

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @Transient
    public void appendEventValues(final Type propertyType, final String propertyName,
                                   final Object previousState, final Object currentState) {
        if (ignoreCurrentStateForType(propertyType) || propertyName.indexOf(HIBERNATE_BACK_REF_STRING) > 0) {
            return;
        } else if (propertyType.isComponentType()) {
            addComponentValues((AbstractComponentType) propertyType, propertyName, previousState, currentState);
        } else {
            if (ignoreStates(previousState, currentState)) {
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
        Object[] componentPrevState = getComponentState(propertyType, previousState);
        Object[] componentCurState = getComponentState(propertyType, currentState);

        if (ignoreStates(componentPrevState, componentCurState)) {
            return;
        } else {
            int index = componentCurState == null ? componentPrevState.length : componentCurState.length;
            for (int i=0; i<index; i++) {
                String compPropertyName = propertyName.concat(".").concat(propertyType.getPropertyNames()[i]);
                if (componentPrevState != null && componentCurState != null) {
                    if (ComparisonTools.nullSafeEquals(componentCurState[i], componentPrevState[i])) {
                        return;
                    }
                }
                addValue(new DataAuditEventValue(compPropertyName,
                    componentPrevState == null ? null : scalarAuditableValue(componentPrevState[i]),
                    componentCurState == null ? null : scalarAuditableValue(componentCurState[i])));
            }
        }
    }

    private Object[] getComponentState(AbstractComponentType propertyType, Object previousState) {
        return previousState == null ? null : propertyType.getPropertyValues(previousState,
                ENTITY_MODE);
    }

    private boolean ignoreStates(Object prevState, Object curState) {
        return prevState == null && curState == null;
    }

    private boolean ignoreStates(Object[] componentPrevState, Object[] componentCurState) {
        return componentPrevState == null && componentCurState == null;
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
            if (propertyValue instanceof Date) {
                return DateFormat.getISO8601Format().format(propertyValue);
            } else {
                return propertyValue.toString();
            }
        }
    }

    public int compareTo(AuditEvent o) {
        return ComparisonTools.nullSafeCompare(
                o.getInfo().getTime(),this.getInfo().getTime());
    }
}
