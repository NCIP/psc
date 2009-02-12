package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.MediaType;
import org.restlet.service.MetadataService;

/**
 * @author Saurabh Agrawal
 * @crated Jan 16, 2009
 */
public class PscMetadataService extends MetadataService {

    public PscMetadataService() {
        super();
        addExtension("ics", MediaType.TEXT_CALENDAR);
    }
}
