package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;

public abstract class BaseAbstractStudyCalendarXmlSerializer {
    public static final String PSC_NS = StudyXMLWriter.PSC_NS;
    public static final Namespace DEFAULT_NAMESPACE = DocumentHelper.createNamespace("", PSC_NS);

    //// Helper Methods
    protected Element element(String elementName) {
        // Using QName is the only way to attach the namespace to the element
        QName qNode = DocumentHelper.createQName(elementName, DEFAULT_NAMESPACE);
        return DocumentHelper.createElement(qNode);
    }

}
