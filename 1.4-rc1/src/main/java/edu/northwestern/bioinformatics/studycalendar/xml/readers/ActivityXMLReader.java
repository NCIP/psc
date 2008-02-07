package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author John Dzak
 */
public class ActivityXMLReader extends DefaultHandler {
    private List<Source> sources = new ArrayList<Source>();
    private Source currentSource;

    public List<Source> read(InputStream dataFile) throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        
        sp.parse(dataFile, this);

        return sources;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equalsIgnoreCase("Source")) {
            currentSource = new Source();
            currentSource.setName(attributes.getValue("name"));
            sources.add(currentSource);

        } else if (qName.equalsIgnoreCase("Activity")) {
            Activity activity = new Activity();
            activity.setName(attributes.getValue("name"));
            activity.setCode(attributes.getValue("code"));
            activity.setDescription(attributes.getValue("description"));
            activity.setType(ActivityType.getById(Integer.parseInt(attributes.getValue("type-id"))));

            activity.setSource(currentSource);
            currentSource.getActivities().add(activity);
        }
    }
}
