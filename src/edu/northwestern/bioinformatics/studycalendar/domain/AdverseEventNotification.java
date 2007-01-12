package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * @author Rhett Sutphin
 */
public class AdverseEventNotification extends AbstractDomainObject {
    private AdverseEvent adverseEvent;
    private boolean dismissed;

    public AdverseEvent getAdverseEvent() {
        return adverseEvent;
    }

    public void setAdverseEvent(AdverseEvent adverseEvent) {
        this.adverseEvent = adverseEvent;
    }

    public boolean isDismissed() {
        return dismissed;
    }

    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
    }
}
