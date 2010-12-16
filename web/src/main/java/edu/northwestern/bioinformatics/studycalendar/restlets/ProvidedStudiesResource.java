package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.StudyListJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudyConsumer;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Jalpa Patel
 */
public class ProvidedStudiesResource extends AbstractCollectionResource<Study> {
    private StudyCalendarXmlCollectionSerializer<Study> xmlSerializer;
    private StudyConsumer studyConsumer;

    @Override
    public void doInit() {
        super.doInit();
        addAuthorizationsFor(Method.GET, STUDY_CREATOR);
       
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    public List<Study> getAllObjects() {
        String q = QueryParameters.Q.extractFrom(getRequest());
        return studyConsumer.search(q);
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        if (variant.getMediaType().includes(MediaType.APPLICATION_JSON)) {
            return new StudyListJsonRepresentation(getAllObjects());
        } else {
            return super.get(variant);
        }
    }

    @Override
    public StudyCalendarXmlCollectionSerializer<Study> getXmlSerializer() {
        return xmlSerializer;
    }

    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Study> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setStudyConsumer(StudyConsumer studyConsumer) {
        this.studyConsumer = studyConsumer;
    }
}

