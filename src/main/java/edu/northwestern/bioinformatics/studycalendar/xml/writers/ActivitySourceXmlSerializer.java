package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttributes;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElements;
import org.dom4j.Element;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ActivitySourceXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<Source> {
    private ActivityXmlSerializer activitySerializer;

    public ActivitySourceXmlSerializer() {
        activitySerializer = new ActivityXmlSerializer(true);
    }

    @Override
    protected Element createCollectionRootElement() {
        return XsdElements.ACTIVITY_SOURCES.create();
    }

    @Override
    protected Element createElement(Source source, boolean inCollection) {
        Element elt = XsdElements.ACTIVITY_SOURCE.create();
        XsdAttributes.ACTIVITY_NAME.addTo(elt, source.getName());
        for (Activity a : source.getActivities()) {
            elt.add(activitySerializer.createElement(a));
        }
        return elt;
    }

    @Override
    public Source readElement(Element element) {
        Source source = new Source();
        source.setName(XsdAttributes.ACTIVITY_NAME.from(element));
        for (Element aElt : (List<Element>) element.elements("activity")) {
            source.addActivity(activitySerializer.readElement(aElt));
        }
        return source;
    }
}
