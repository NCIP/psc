package edu.northwestern.bioinformatics.studycalendar.domain;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class AdverseEvent extends AbstractDomainObjectWithBigId {
    private Date detectionDate;
    private String description;

    public Date getDetectionDate() {
        return detectionDate;
    }

    public void setDetectionDate(Date detectionDate) {
        this.detectionDate = detectionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
