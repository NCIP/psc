package edu.northwestern.bioinformatics.studycalendar.utils.dataloaders;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author John Dzak
 */
public class ActivityXmlReader extends DefaultHandler {
    private List<Source> sources = new ArrayList<Source>();
    private Source tempSource;

    public List<Source> read(InputStream dataFile) {
        SAXParserFactory spf = SAXParserFactory.newInstance();

        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse(dataFile, this);

        } catch(SAXException se) {
            se.printStackTrace();
        } catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        return sources;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equalsIgnoreCase("Source")) {
            tempSource = new Source();
            tempSource.setName(attributes.getValue("name"));
            sources.add(tempSource);

        } else if (qName.equalsIgnoreCase("Activity")) {
            Activity activity = new Activity();
            activity.setName(attributes.getValue("name"));
            activity.setCode(attributes.getValue("code"));
            activity.setDescription(attributes.getValue("description"));
            activity.setType(ActivityType.getById(Integer.parseInt(attributes.getValue("type"))));

            activity.setSource(tempSource);
            tempSource.getActivities().add(activity);
        }
    }
    }
