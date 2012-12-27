/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import gov.nih.nci.cagrid.common.Utils;
import org.apache.axis.description.TypeDesc;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Rhett Sutphin
 */
public class XmlHelper {
    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    static {
        FACTORY.setNamespaceAware(true);
    }

    public static Document parseDocument(String xml) {
        try {
            return FACTORY.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (SAXException e) {
            throw new StudyCalendarValidationException("Invalid input XML:\n" + xml, e);
        } catch (IOException e) {
            throw new StudyCalendarSystemException("Reading xml string failed", e);
        } catch (ParserConfigurationException e) {
            throw new StudyCalendarSystemException("Parsing XML failed\n" + xml, e);
        }
    }

    public static InputStream getWsddStream(Class<?> clientClass) {
        InputStream wsdd = clientClass.getResourceAsStream("client-config.wsdd");
        if (wsdd == null) {
            throw new IllegalStateException("Could not find client-config.wsdd relative to " + clientClass.getName());
        }
        return wsdd;
    }

    public static String axisObjectToXmlString(Object o, Class<?> clientClass) {
        StringWriter sw = new StringWriter();
        try {
            Utils.serializeObject(o,
                cleanQName(getTypeDesc(o).getXmlType()),
                sw, getWsddStream(clientClass));
        } catch (Exception e) {
            throw new StudyCalendarSystemException("Could not serialize %s", e, o);
        }
        return sw.toString();
    }

    private static QName cleanQName(QName in) {
        return new QName(in.getNamespaceURI(), in.getLocalPart().replaceAll("(<|>)", ""));
    }

    private static TypeDesc getTypeDesc(Object o) {
        Class<?> clazz = o.getClass();
        String methodName = "getTypeDesc";
        try {
            Method getTypeDesc = clazz.getMethod(methodName);
            return (TypeDesc) getTypeDesc.invoke(null);
        } catch (NoSuchMethodException e) {
            throw new StudyCalendarError(
                "%s unexpectedly does not not have a %s method", e,
                clazz.getName(), methodName);
        } catch (IllegalAccessException e) {
            throw new StudyCalendarSystemException(
                "Getting type desc for %s failed", e, clazz.getName());
        } catch (InvocationTargetException e) {
            throw new StudyCalendarSystemException(
                "Getting type desc for %s failed", e, clazz.getName());
        }
    }

    public static Element marshalSingleJaxbObject(Object o) {
        try {
            Marshaller marshaller = JAXBContext.newInstance(o.getClass()).createMarshaller();
            Document target = FACTORY.newDocumentBuilder().newDocument();
            marshaller.marshal(o, target);
            return target.getDocumentElement();
        } catch (JAXBException e) {
            throw new StudyCalendarSystemException("Failed to marshal an object using JAXB", e);
        } catch (ParserConfigurationException e) {
            throw new StudyCalendarSystemException("Failed to marshal an object to a DOM using JAXB", e);
        }
    }

    public static Object unmarshalSingleJaxbObject(Element element, Class<?> type) {
        try {
            return JAXBContext.newInstance(type).createUnmarshaller().unmarshal(element);
        } catch (JAXBException e) {
            throw new StudyCalendarSystemException(
                "Failed to unmarshal from %s as %s", e, element, type.getName());
        }
    }
}
