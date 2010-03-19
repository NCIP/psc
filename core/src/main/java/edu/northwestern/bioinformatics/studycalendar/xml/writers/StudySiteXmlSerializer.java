package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

/**
 * @author John Dzak
 */
public class StudySiteXmlSerializer extends AbstractStudyCalendarXmlSerializer<StudySite> {

    public Element createElement(StudySite studySite) {
        Element elt = XsdElement.STUDY_SITE_LINK.create();
        STUDY_SITE_STUDY_NAME.addTo(elt, studySite.getStudy().getName());
        STUDY_SITE_SITE_NAME.addTo(elt, studySite.getSite().getName());
        return elt;
    }

    public StudySite readElement(Element element) {
        String studyName = element.attributeValue("study-name");
        String siteName = element.attributeValue("site-name");
            
        Study study = new Study();
        study.setAssignedIdentifier(studyName);
        Site site = new Site();
        site.setName(siteName);
        StudySite studySite = new StudySite();
        studySite.setStudy(study);
        studySite.setSite(site);
        return studySite;
    }
}
