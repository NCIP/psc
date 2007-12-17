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
import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.*;


public class StudyXmlWriter {
    public static String ROOT = "study";

    /* Tag Element constants */
    public static final String PLANNDED_CALENDAR = "planned-calendar";
    public static final String AMENDMENT = "amendment";
    public static final String PLANNED_CALENDAR_DELTA = "planned-calendar-delta";
    public static final String EPOCH_DELTA = "epoch-delta";
    public static final String ADD = "add";
    public static final String EPOCH = "epoch";
    public static final String STUDY_SEGMENT = "study-segment";

    /* Tag Attribute constants */
    public static final String ID = "id";
    public static final String DATE = "date";
    public static final String NAME = "name";
    public static final String INDEX = "index";
    public static final String MANDATORY = "mandatory";
    public static final String ASSIGNED_IDENTIFIER = "assigned-identifier";


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

    // TODO: Break all these add methods into an ElementFactory
    private void addStudy(Document document, Study study) {
        Element rootElement = document.createElement(ROOT);
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://bioinformatics.northwestern.edu/ns/psc/study.xsd" );
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:schemaLocation", "http://bioinformatics.northwestern.edu/ns/psc/study.xsd" );

        rootElement.setAttribute(ID, study.getGridId());
        rootElement.setAttribute(ASSIGNED_IDENTIFIER, study.getAssignedIdentifier());

        addPlannedCalendar(document, study, rootElement);


        addAmendments(document, study, rootElement);

    }

    private void addPlannedCalendar(Document document, Study study, Element rootElement) {
        if (study.getPlannedCalendar() != null) {
            Element plannedCalEle = document.createElement(PLANNDED_CALENDAR);
            plannedCalEle.setAttribute(ID, study.getPlannedCalendar().getGridId());

            document.appendChild(rootElement);
            rootElement.appendChild(plannedCalEle);
        }
    }

    private void addAmendments(Document document, Study study, Element rootElement) {
        List<Amendment> allAmendments = new ArrayList(study.getAmendmentsList());
        allAmendments.add(study.getDevelopmentAmendment());

        for (Amendment amendment : allAmendments) {
            Element element = document.createElement(AMENDMENT);

            element.setAttribute(NAME, amendment.getName());
            element.setAttribute(MANDATORY, Boolean.toString(amendment.isMandatory()));
            element.setAttribute(ID, amendment.getGridId());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            element.setAttribute(DATE, formatter.format(amendment.getDate()));

            rootElement.appendChild(element);

            addDeltas(document, amendment, element);

        }
    }

    private void addDeltas(Document document, Amendment amendment, Element amendmentElement) {
        for (Delta<?> delta :  amendment.getDeltas()) {
            if (delta instanceof PlannedCalendarDelta) {
                Element element = document.createElement(PLANNED_CALENDAR_DELTA);
                element.setAttribute(ID, delta.getGridId());
                amendmentElement.appendChild(element);

                addChanges(document, delta, element);
            }

            if (delta instanceof EpochDelta) {
                Element element = document.createElement(EPOCH_DELTA);
                element.setAttribute(ID, delta.getGridId());
                amendmentElement.appendChild(element);

                addChanges(document, delta, element);
            }
        }
    }

    private void addChanges(Document document, Delta<?> delta, Element deltaElement) {
        for (Change change : delta.getChanges()) {
            if ((ChangeAction.ADD).equals(change.getAction())) {
                Element element = document.createElement(ADD);
                element.setAttribute(ID, change.getGridId());
                element.setAttribute(INDEX, ((Add) change).getIndex().toString());
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
            element.setAttribute(NAME, epoch.getName());
            element.setAttribute(ID, epoch.getGridId());
            changeElement.appendChild(element);

            addStudySegments(document, epoch, element);
        }
    }

    private void addStudySegments(Document document, Epoch epoch, Element epochElement) {
        for(StudySegment studySegment : epoch.getStudySegments()) {
            Element element = document.createElement(STUDY_SEGMENT);
            element.setAttribute(NAME, studySegment.getName());
            element.setAttribute(ID, studySegment.getGridId());

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
