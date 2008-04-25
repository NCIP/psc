package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.utils.FormatTools;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

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

    private boolean dismissed = false;
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

    /**
     * only for Hibernate.
     * <p/>
     * <li>If you want to create notifications for Adverse event, use {@link edu.northwestern.bioinformatics.studycalendar.domain.Notification#Notification(AdverseEvent)}</li>
     * <li>If you want to create notifications for Amendments,
     * use {@link edu.northwestern.bioinformatics.studycalendar.domain.Notification#Notification(edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval)}</li>
     * <li>If you want to create notifications for Activity,
     * use {@link edu.northwestern.bioinformatics.studycalendar.domain.Notification#Notification(ScheduledActivity)}</li>
     */
    public Notification() {

    }

    /**
     * Create notification message  for reconsents
     *
     * @param reconsentEvent
     */
    public Notification(final ScheduledActivity reconsentEvent) {

        if (reconsentEvent != null) {
            title = "Reconsent scheduled for " + FormatTools.formatDate(new Date());
            message = "/pages/cal/scheduleActivity?event=" + reconsentEvent.getId();
        }

    }

    public Notification(final AmendmentApproval amendmentApproval) {
        if (amendmentApproval != null && amendmentApproval.getAmendment() != null) {

            Amendment amendment = amendmentApproval.getAmendment();
            title = "Schedule amended according to " + amendment.getDisplayName();

            StudySite studySite = amendmentApproval.getStudySite();

            if (studySite != null && studySite.getStudy() != null) {
                message = "/pages/cal/template/amendments?study=" + amendmentApproval.getStudySite().getStudy().getId()
                        + "#amendment=" + amendment.getId();
            }
            //Since the schedule is already amended, no action is required.
            actionRequired = false;
        }
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
