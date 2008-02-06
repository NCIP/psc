package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import java.util.Collection;

/**
 * @author Saurabh Agrawal
 */
public class ActivitySourcesXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<Collection<Source>> {
    private ActivitySourceXmlSerializer activitySourceXmlSerializer;

    public ActivitySourcesXmlSerializer() {
        activitySourceXmlSerializer = new ActivitySourceXmlSerializer();
    }

    @Override
    protected XsdElement collectionRootElement() {
        return XsdElement.ACTIVITY_SOURCES;
    }

    @Override
    protected XsdElement rootElement() {
        return XsdElement.ACTIVITY_SOURCE;
    }

    @Override
    protected Element createElement(Collection<Source> sources, boolean inCollection) {


        Element element = XsdElement.ACTIVITY_SOURCES.create();
        for (Source source : sources) {
            element.add(activitySourceXmlSerializer.createElement(source));
        }
        return element;
    }

    @Override
    public Collection<Source> readElement(Element element) {
        throw new UnsupportedOperationException("This serializer is write-only");

    }
}

