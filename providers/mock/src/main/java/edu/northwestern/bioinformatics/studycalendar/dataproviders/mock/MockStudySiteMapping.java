package edu.northwestern.bioinformatics.studycalendar.dataproviders.mock;

/**
 * @author Rhett Sutphin
 */
public class MockStudySiteMapping {
    private String studyNct;
    private String siteIdentifier;

    public MockStudySiteMapping(String studyNct, String siteIdentifier) {
        this.studyNct = studyNct;
        this.siteIdentifier = siteIdentifier;
    }

    public String getStudyNct() {
        return studyNct;
    }

    public String getSiteIdentifier() {
        return siteIdentifier;
    }
}
