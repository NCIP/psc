package edu.northwestern.bioinformatics.studycalendar.domain.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import gov.nih.nci.cabig.ctms.audit.domain.*;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;

/**
 * @author Jalpa Patel
 */
public class AuditEvent extends DataAuditEvent implements Comparable<AuditEvent> {
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

    public int compareTo(AuditEvent o) {
        return ComparisonTools.nullSafeCompare(
                o.getInfo().getTime(),this.getInfo().getTime());
    }
}
