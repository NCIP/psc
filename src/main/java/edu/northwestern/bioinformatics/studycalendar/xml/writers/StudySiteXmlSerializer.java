package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.STUDY_SITE_SITE_NM;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.STUDY_SITE_STUDY_NM;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

/**
 * @author John Dzak
 */
public class StudySiteXmlSerializer extends AbstractStudyCalendarXmlSerializer<StudySite> {
    private final String STUDY_NOT_FOUND_MESSAGE = "Study '%s' not found. Please define a study that exists.";
    private final String SITE_NOT_FOUND_MESSAGE = "Site '%s' not found. Please define a site that exists.";

    private StudyDao studyDao;
    private SiteDao siteDao;

    public Element createElement(StudySite studySite) {
        Element elt = XsdElement.STUDY_SITE_LINK.create();
        STUDY_SITE_STUDY_NM.addTo(elt, studySite.getStudy().getName());
        STUDY_SITE_SITE_NM.addTo(elt, studySite.getSite().getName());
        return elt;
    }

    public StudySite readElement(Element element) {
        String studyName = element.attributeValue("study-name");
        String siteName = element.attributeValue("site-name");
            
        Study study = studyDao.getByAssignedIdentifier(studyName);
        if (study == null) { throw new StudyCalendarValidationException(STUDY_NOT_FOUND_MESSAGE, studyName); }

        Site site = siteDao.getByAssignedIdentifier(siteName);
        if (site == null) { throw new StudyCalendarValidationException(SITE_NOT_FOUND_MESSAGE, siteName); }

        StudySite studySite = study.getStudySite(site);
        if (studySite == null) {
            studySite = new StudySite();
            studySite.setStudy(study);
            studySite.setSite(site);
        }
        return studySite;

    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
