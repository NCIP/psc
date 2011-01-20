package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.dao.auditing.AuditEventDao;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jalpa Patel
 */

@Transactional
public class UserActionService {
    private UserActionDao userActionDao;
    private AuditEventDao auditEventDao;
    private ApplicationSecurityManager applicationSecurityManager;


    public List<UserAction> getUndoableActions(String context) {
        List<UserAction> userActions = userActionDao.getUserActionsByContext(context);
        Collections.reverse(userActions);
        List<UserAction> undoable = new ArrayList<UserAction>();

        for (UserAction ua : userActions) {
            if (isUndoableUserAction(ua)) {
                undoable.add(ua);
            }
        }
        return undoable;
    }

    private boolean isUndoableUserAction(UserAction ua) {
        if (!ua.getUser().getName().equals(applicationSecurityManager.getUserName())) {
            return false;
        } else if (ua.isUndone()) {
            return false;
        }
        return isUndoableActionWithAuditEvent(ua);
    }

    private boolean isUndoableActionWithAuditEvent(UserAction ua) {
        List<AuditEvent> events = auditEventDao.getAuditEventsByUserActionId(ua.getGridId());
        for (AuditEvent ae : events) {
            List<AuditEvent> laterEvents = auditEventDao.getAuditEventsWithLaterTimeStamp(
                        ae.getReference().getClassName(), ae.getReference().getId(), ae.getInfo().getTime());
            for (AuditEvent laterEvent : laterEvents) {
                if (laterEvent.getUserActionId() == null) {
                    return false;
                } else {
                    UserAction ua1 = userActionDao.getByGridId(laterEvent.getUserActionId());
                    if (ua1 != null) {
                        if (!ua1.getUser().getName().equals(ua.getUser().getName())) {
                            return false;
                        } else if (!ua1.getContext().equals(ua.getContext())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Required
    public void setUserActionDao(UserActionDao userActionDao) {
        this.userActionDao = userActionDao;
    }

    @Required
    public void setAuditEventDao(AuditEventDao auditEventDao) {
        this.auditEventDao = auditEventDao;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }
}
