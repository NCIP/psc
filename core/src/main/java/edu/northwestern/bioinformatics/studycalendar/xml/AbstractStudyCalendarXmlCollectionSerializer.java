/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    protected Element createCollectionRootElement() {
        return collectionRootElement().create();
    }

    protected abstract XsdElement collectionRootElement();

    /**
     * Implement this method instead of {@link #createElement}.
     * @param inCollection provides a hint about whether the element will be part of a
     *   collection or not.  No matter the value, this method must always return an
     *   element matching {@link #rootElement}.
     */
    protected abstract Element createElement(R r, boolean inCollection);

    // TODO: might be useful to move this up
    protected abstract XsdElement rootElement();

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

    public Collection<R> readCollectionDocument(InputStream in) {
        Element collectionRoot = deserializeDocument(in).getRootElement();
        return readCollectionElement(collectionRoot);
    }

    public Collection<R> readCollectionOrSingleDocument(InputStream in) {
        Document document = deserializeDocument(in);
        if (document.getRootElement().getName().equals(collectionRootElement().xmlName())) {
            return readCollectionDocument(document);
        } else {
            return Collections.singleton(readDocument(document));
        }
    }

    public Collection<R> readCollectionDocument(Document document) {
        Element collectionRoot = document.getRootElement();
        return readCollectionElement(collectionRoot);
    }


    /**
     * This default implementation discards the collection element and passes every
     * subelement matching {@link #rootElement} to {@link #readElement}.
     */
    @SuppressWarnings({ "unchecked" })
    public Collection<R> readCollectionElement(Element collectionRoot) {
        List<Element> children = collectionRoot.elements(rootElement().xmlName());
        List<R> items = new ArrayList<R>(children.size());
        for (Element child : children) {
            items.add(readElement(child));
        }
        return items;
    }
}
