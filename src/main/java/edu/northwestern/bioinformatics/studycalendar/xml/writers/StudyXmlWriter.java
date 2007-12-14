package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.io.StringWriter;

import org.w3c.dom.*;


public class StudyXmlWriter {
    public static String ROOT = "study";

    public static String PLANNDED_CALENDAR = "planned-calendar";
    public static String AMENDMENT = "amendment";
    public static String DELTA = "delta";
    public static String ADD = "add";


    public String createStudyXml(Study study) throws Exception {
        Document document = createDocument();

        addStudy(document, study);

        return convertToString(document);

    }

    private Document createDocument() throws Exception{

        //get an instance of factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        //get an instance of builder
        DocumentBuilder db = dbf.newDocumentBuilder();

        //create an instance of DOM
        return db.newDocument();
    }

    private void addStudy(Document document, Study study) {
        Element rootElement = document.createElement(ROOT);
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://bioinformatics.northwestern.edu/ns/psc/study.xsd" );
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:schemaLocation", "http://bioinformatics.northwestern.edu/ns/psc/study.xsd" );


        Element plannedCalEle = document.createElement(PLANNDED_CALENDAR);

        document.appendChild(rootElement);
        rootElement.appendChild(plannedCalEle);

        addAmendments(document, study, rootElement);

    }

    private void addAmendments(Document document, Study study, Element rootElement) {
        for (Amendment amendment : study.getAmendmentsList()) {
            Element element = document.createElement(AMENDMENT);
            rootElement.appendChild(element);

            addDeltas(document, amendment, element);

        }
    }

    private void addDeltas(Document document, Amendment amendment, Element amendmentElement) {
        for (Delta<?> delta :  amendment.getDeltas()) {
            Element element = document.createElement(DELTA);
            amendmentElement.appendChild(element);

            addChanges(document, delta, element);
        }
    }

    private void addChanges(Document document, Delta<?> delta, Element deltaElement) {
        for (Change change : delta.getChanges()) {
            if ((ChangeAction.ADD).equals(change.getAction())) {
                Element element = document.createElement(ADD);
                deltaElement.appendChild(element);
            }
        }
    }

    private String convertToString(Document document) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        //initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(document);
        transformer.transform(source, result);

        return result.getWriter().toString();
    }

}
