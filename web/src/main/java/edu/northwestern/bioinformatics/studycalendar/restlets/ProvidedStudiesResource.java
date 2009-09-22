package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudyConsumer;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Method;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * @author Jalpa Patel
 */
public class ProvidedStudiesResource extends AbstractCollectionResource<Study> {
    private StudyCalendarXmlCollectionSerializer<Study> xmlSerializer;
    private StudyConsumer studyConsumer;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.STUDY_COORDINATOR);
    }

    public List<Study> getAllObjects() {
        String q = QueryParameters.Q.extractFrom(getRequest());
        return studyConsumer.search(q);
    }

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

