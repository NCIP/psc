package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * @author Rhett Sutphin
 */
public interface WithBigId {
    /**
     * @return the grid-scoped unique identifier for this object
     */
    String getBigId();

    /**
     * Specify the grid-scoped unique identifier for this object
     * @param bigId
     */
    void setBigId(String bigId);
}
