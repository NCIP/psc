/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;

/**
 * @author John Dzak
 */
public class StudySiteXmlSerializer extends AbstractStudyCalendarXmlSerializer<StudySite> {
    @Override
    public Element createElement(StudySite studySite) {
        Element elt = XsdElement.STUDY_SITE_LINK.create();
        STUDY_SITE_STUDY_IDENTIFIER.addTo(elt, studySite.getStudy().getName());
        STUDY_SITE_SITE_IDENTIFIER.addTo(elt, studySite.getSite().getName());
        return elt;
    }

    @Override
    public StudySite readElement(Element element) {
        String studyIdent = STUDY_SITE_STUDY_IDENTIFIER.from(element);
        String siteIdent = STUDY_SITE_SITE_IDENTIFIER.from(element);
            
        Study study = new Study();
        study.setAssignedIdentifier(studyIdent);
        Site site = new Site();
        site.setAssignedIdentifier(siteIdent);

        StudySite studySite = new StudySite();
        studySite.setStudy(study);
        studySite.setSite(site);
        return studySite;
    }
}
