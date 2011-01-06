package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "user_actions")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {@Parameter(name = "sequence", value = "seq_user_actions_id")}
)
public class UserAction extends AbstractMutableDomainObject {
    private String description;
    private Integer csmUserId;
    private String context;
    private String actionType;
    private boolean undone;
    private User resolvedUser;

    public UserAction(String description, String context, String actionType, boolean undone, User resolvedUser) {
        this.description = description;
        this.context = context;
        this.actionType = actionType;
        this.undone = undone;
        setUser(resolvedUser);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCsmUserId() {
        return csmUserId;
    }

    public void setCsmUserId(Integer csmUserId) {
        this.csmUserId = csmUserId;
    }

    public UserAction() {
        this.undone = false;
    }

    /*
     TODO: I would prefer that this field's type be PscUser.  However, that would introduce
     a dependency on psc:authorization from psc:domain.  psc:authorization's legacy mode support
     forces a dependency on psc:domain, so the reverse dependency isn't possible until legacy
     mode can be completely removed.
    */

    @Transient
    public gov.nih.nci.security.authorization.domainobjects.User getUser() {
        if (resolvedUser != null) {
            return resolvedUser;
        } else if (getCsmUserId() == null) {
            return null;
        } else {
            throw new StudyCalendarSystemException("The actual manager user object has not been externally resolved for this assignment");
        }
    }

    public void setUser(gov.nih.nci.security.authorization.domainobjects.User csmUser) {
        this.resolvedUser = csmUser;
        if (csmUser != null) {
            setCsmUserId(csmUser.getUserId().intValue());
        } else {
            setCsmUserId(null);
        }
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public boolean isUndone() {
        return undone;
    }

    public void setUndone(boolean undone) {
        this.undone = undone;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final UserAction that = (UserAction) o;

        if (description != null ? !description.equals(that.getDescription()) : that.getDescription() != null)
            return false;
        if (csmUserId != null ? !csmUserId.equals(that.getCsmUserId()) : that.getCsmUserId() != null)
            return false;
        if (context != null ? !context.equals(that.getContext()) : that.getContext() != null) return false;
        if (actionType != null ? !actionType.equals(that.getActionType()) : that.getActionType() != null) return false;
        if (undone != that.isUndone()) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (description != null ? description.hashCode() : 0);
        result = 31 * result + (csmUserId != null ? csmUserId.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        result = 31 * result + (actionType != null ? actionType.hashCode() : 0);
        result = 31 * result + (undone ? 1 : 0);
        return result;
    }
}
