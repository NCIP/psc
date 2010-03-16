package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.MediaType;
import org.restlet.service.MetadataService;

/**
 * @author Saurabh Agrawal
 * @crated Jan 16, 2009
 */
public class PscMetadataService extends MetadataService {
    public static final MediaType TEXT_CSV = new MediaType("text/csv", "CSV document");

    public PscMetadataService() {
        super();
        addExtension("ics", MediaType.TEXT_CALENDAR);
        addExtension("csv", TEXT_CSV);
    }
}
