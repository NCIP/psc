package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public abstract class AbstractStudyCalendarXmlCollectionSerializer<R>
    extends AbstractStudyCalendarXmlSerializer<R>
    implements StudyCalendarXmlCollectionSerializer<R>
{

    /**
     * Create the root element for the collection view of this object.
     */
    protected abstract Element createCollectionRootElement();

    /**
     * Implement this method instead of {@link #createElement}.
     */
    protected abstract Element createElement(R r, boolean inCollection);

    @Override
    public final Element createElement(R r) {
        return createElement(r, false);
    }

    public Document createDocument(Collection<R> rs) {
        Document document = DocumentHelper.createDocument();
        Element element = createCollectionRootElement();

        for (R r : rs) {
            element.add(createElement(r, true));
        }

        configureRootElement(element);

        document.add(element);

        return document;
    }

    public String createDocumentString(Collection<R> rs) {
        return createDocumentString(createDocument(rs));
    }

}
