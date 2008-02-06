package edu.northwestern.bioinformatics.studycalendar.xml.writers;

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
    public Element createElement(StudySite studySite) {
        Element elt = XsdElement.STUDY_SITE_LINK.create();
        STUDY_SITE_STUDY_NM.addTo(elt, studySite.getStudy().getName());
        STUDY_SITE_SITE_NM.addTo(elt, studySite.getSite().getName());
        
        return elt;
    }

    public StudySite readElement(Element element) {
        throw new UnsupportedOperationException();
    }
}
