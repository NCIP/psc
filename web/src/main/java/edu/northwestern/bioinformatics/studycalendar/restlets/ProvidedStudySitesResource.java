package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudySiteConsumer;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;

/**
 * @author John Dzak
 */
public class ProvidedStudySitesResource extends AbstractCollectionResource<Study> {
    private StudyCalendarXmlCollectionSerializer<Study> xmlSerializer;

    private StudySiteConsumer studySiteConsumer;

    @Override
    public void doInit() {
        super.doInit();
    }

    @Override
    public List<Study> getAllObjects() {
        String siteId = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
        if (siteId != null) {
            Site site = new Site();
            site.setAssignedIdentifier(siteId);
            return collectStudies(studySiteConsumer.refresh(site));
        }

        return null;
    }

    /////// Helpers
    @SuppressWarnings({ "unchecked" })
    private List<Study> collectStudies(List<StudySite> studySites) {
        return new ArrayList<Study>(CollectionUtils.collect(studySites, new Transformer() {
            public Object transform(Object o) {
                return ((StudySite) o).getStudy();
            }
        }));
    }


    /////// Bean Setters
    @Override
    @Required
    public StudyCalendarXmlCollectionSerializer<Study> getXmlSerializer() {
        return xmlSerializer;
    }

    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Study> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setStudySiteConsumer(StudySiteConsumer studySiteConsumer) {
        this.studySiteConsumer = studySiteConsumer;
    }
}

