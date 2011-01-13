package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import java.util.Collection;
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
    protected XsdElement collectionRootElement() {
        return XsdElement.ACTIVITY_SOURCES;
    }

    @Override
    protected XsdElement rootElement() {
        return XsdElement.ACTIVITY_SOURCE;
    }

    @Override
    protected Element createElement(Source source, boolean inCollection) {
        Element sourceElement = rootElement().create();
        XsdAttribute.ACTIVITY_SOURCE_NAME.addTo(sourceElement, source.getName());
        for (Activity a : source.getActivities()) {
            sourceElement.add(activitySerializer.createElement(a));
        }
        return sourceElement;
    }

    @Override
    public Source readElement(Element element) {
        Source source = new Source();
        source.setName(XsdAttribute.ACTIVITY_NAME.from(element));
        for (Element aElt : (List<Element>) element.elements("activity")) {
            Activity activity = activitySerializer.readElement(aElt);
            if (source.getActivities()!=null){
                 for (Activity a : source.getActivities()) {
                    if (activity.getName().equals(a.getName()) || activity.getCode().equals(a.getCode())) {
                       throw new StudyCalendarValidationException("Name and Code must be unique for activities within same source. Please correct activity with name = " + activity.getName());
                    }
                 }
            }
            source.addActivity(activity);
        }
        return source;
    }

    public Element createElement(Collection<Source> rs) {
        Element element = createCollectionRootElement();

        for (Source r : rs) {
            element.add(createElement(r, true));
        }

        return element;
    }
}
