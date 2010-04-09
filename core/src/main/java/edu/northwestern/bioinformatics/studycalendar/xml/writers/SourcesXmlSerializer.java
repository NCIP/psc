package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

/**
 * @author Jalpa Patel
 */
public class SourcesXmlSerializer  extends AbstractStudyCalendarXmlCollectionSerializer<Source> {

    @Override
    public Source readElement(Element element) {
        throw new UnsupportedOperationException("This serializer is write-only");
    }

    @Override
    protected XsdElement collectionRootElement() {
        return XsdElement.SOURCES;
    }

    @Override
    protected XsdElement rootElement() {
        return XsdElement.SOURCE;
    }

    @Override
    protected Element createElement(final Source source, final boolean inCollection) {
        Element sourceElement = rootElement().create();
        SOURCE_NAME.addTo(sourceElement, source.getName());
        if (source.getManualFlag() != null) {
            SOURCE_MANUAL_FLAG.addTo(sourceElement, source.getManualFlag());
        }
        return sourceElement;
    }
}
