package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.utils.FormatTools;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Saurabh Agrawal
 */
@Entity
@Table(name = "notifications")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {
        @Parameter(name = "sequence", value = "seq_notifications_id")
                }
)
public class Notification extends AbstractMutableDomainObject {

    private boolean dismissed;
    private String title;
    private String message;
    private boolean actionRequired = true;

    private StudySubjectAssignment assignment;

    public Notification(AdverseEvent adverseEvent) {
        if (adverseEvent != null) {
            setActionRequired(true);
            if (adverseEvent.getDetectionDate() != null) {
                title = "Serious Adverse Event on " + FormatTools.formatDate(adverseEvent.getDetectionDate());
            }
            message = adverseEvent.getDescription();
        }
    }

    public Notification() {

    }

    @ManyToOne
    public StudySubjectAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(StudySubjectAssignment assignment) {
        this.assignment = assignment;
    }

    public boolean isDismissed() {
        return dismissed;
    }

    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public boolean isActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(final boolean actionRequired) {
        this.actionRequired = actionRequired;
    }


}
