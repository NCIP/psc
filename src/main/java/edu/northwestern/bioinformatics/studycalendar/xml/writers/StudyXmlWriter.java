package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import org.w3c.dom.*;


public class StudyXmlWriter {
    public static String ROOT = "study";

    public static final String PLANNDED_CALENDAR = "planned-calendar";
    public static final String AMENDMENT = "amendment";
    public static final String PLANNED_CALENDAR_DELTA = "planned-calendar-delta";
    public static final String ADD = "add";
    public static final String EPOCH = "epoch";
    public static final String STUDY_SEGMENT = "study-segment";


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

        rootElement.setAttribute("assigned-identifier", study.getAssignedIdentifier());
        rootElement.setAttribute("grid-id", study.getGridId());

        addPlannedCalendar(document, study, rootElement);


        addAmendments(document, study, rootElement);

    }

    private void addPlannedCalendar(Document document, Study study, Element rootElement) {
        if (study.getPlannedCalendar() != null) {
            Element plannedCalEle = document.createElement(PLANNDED_CALENDAR);
            plannedCalEle.setAttribute("grid-id", study.getPlannedCalendar().getGridId());

            document.appendChild(rootElement);
            rootElement.appendChild(plannedCalEle);
        }
    }

    private void addAmendments(Document document, Study study, Element rootElement) {
        for (Amendment amendment : study.getAmendmentsList()) {
            Element element = document.createElement(AMENDMENT);

            element.setAttribute("name", amendment.getName());
            element.setAttribute("mandatory", Boolean.toString(amendment.isMandatory()));
            element.setAttribute("grid-id", amendment.getGridId());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            element.setAttribute("date", formatter.format(amendment.getDate()));

            rootElement.appendChild(element);

            addDeltas(document, amendment, element);

        }
    }

    private void addDeltas(Document document, Amendment amendment, Element amendmentElement) {
        for (Delta<?> delta :  amendment.getDeltas()) {
            if (delta instanceof PlannedCalendarDelta) {
                Element element = document.createElement(PLANNED_CALENDAR_DELTA);
                element.setAttribute("grid-id", delta.getGridId());
                amendmentElement.appendChild(element);

                addChanges(document, delta, element);
            }
        }
    }

    private void addChanges(Document document, Delta<?> delta, Element deltaElement) {
        for (Change change : delta.getChanges()) {
            if ((ChangeAction.ADD).equals(change.getAction())) {
                Element element = document.createElement(ADD);
                element.setAttribute("grid-id", change.getGridId());
                deltaElement.appendChild(element);

                addNode(document, (ChildrenChange) change, element);
            }
        }
    }

    private void addNode(Document document, ChildrenChange change, Element changeElement) {
        PlanTreeNode<?> child = change.getChild();
        if (child instanceof Epoch) {
            Epoch epoch = (Epoch) child;
            Element element = document.createElement(EPOCH);
            element.setAttribute("name", epoch.getName());
            element.setAttribute("grid-id", epoch.getGridId());
            changeElement.appendChild(element);

            addStudySegments(document, epoch, element);
        }
    }

    private void addStudySegments(Document document, Epoch epoch, Element epochElement) {
        for(StudySegment studySegment : epoch.getStudySegments()) {
            Element element = document.createElement(STUDY_SEGMENT);
            element.setAttribute("name", studySegment.getName());
            element.setAttribute("grid-id", studySegment.getGridId());

            epochElement.appendChild(element);
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
