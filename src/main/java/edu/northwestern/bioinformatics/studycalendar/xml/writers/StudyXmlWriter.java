package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

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
import java.util.Collections;
import java.util.SortedSet;

import org.w3c.dom.*;


public class StudyXmlWriter {
    public static final String SCHEMA_NAMESPACE = "http://bioinformatics.northwestern.edu/ns/psc";
    public static final String SCHEMA_LOCATION  = "http://bioinformatics.northwestern.edu/ns/psc/study.xsd";
    public static final String XML_SCHEMA       = "http://www.w3.org/2001/XMLSchema-instance";

    public static final String SCHEMA_NAMESPACE_ATTRIBUTE = "xmlns";
    public static final String SCHEMA_LOCATION_ATTRIBUTE  = "xmlns:schemaLocation";
    public static final String XML_SCHEMA_ATTRIBUTE       = "xmlns:xsi";

    /* Tag Element constants */
    public static final String ROOT = "study";
    public static final String PLANNDED_CALENDAR = "planned-calendar";
    public static final String AMENDMENT = "amendment";
    public static final String DELTA = "delta";
    public static final String ADD = "add";

    public static final String EPOCH = "epoch";
    public static final String STUDY_SEGMENT = "study-segment";
    public static final String PERIOD = "period";
    public static final String PLANNED_ACTIVITY = "planned-activity";

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

    protected Document createDocument() throws Exception{

        //get an instance of factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        //get an instance of builder
        DocumentBuilder db = dbf.newDocumentBuilder();

        //create an instance of DOM
        return db.newDocument();
    }

    // TODO: Break all these add methods into an ElementFactory
    protected void addStudy(Document document, Study study) {
        Element rootElement = document.createElement(ROOT);
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_NAMESPACE );
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", XML_SCHEMA_ATTRIBUTE, XML_SCHEMA );
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", SCHEMA_LOCATION_ATTRIBUTE, SCHEMA_LOCATION );

        rootElement.setAttribute(ID, study.getGridId());
        rootElement.setAttribute(ASSIGNED_IDENTIFIER, study.getAssignedIdentifier());

        addPlannedCalendar(document, study, rootElement);

        List<Amendment> allAmendments = new ArrayList(study.getAmendmentsList());
        if (study.getDevelopmentAmendment() != null) allAmendments.add(study.getDevelopmentAmendment());
        addAmendments(document, allAmendments, rootElement);

    }

    protected void addPlannedCalendar(Document document, Study study, Element rootElement) {
        if (study.getPlannedCalendar() != null) {
            Element plannedCalEle = document.createElement(PLANNDED_CALENDAR);
            plannedCalEle.setAttribute(ID, study.getPlannedCalendar().getGridId());

            document.appendChild(rootElement);
            rootElement.appendChild(plannedCalEle);
        }
    }

    protected void addAmendments(Document document, List<Amendment> amendments, Element parent) {
        for (Amendment amendment : amendments) {
            Element element = document.createElement(AMENDMENT);

            element.setAttribute(NAME, amendment.getName());
            element.setAttribute(MANDATORY, Boolean.toString(amendment.isMandatory()));
            element.setAttribute(ID, amendment.getGridId());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            element.setAttribute(DATE, formatter.format(amendment.getDate()));

            parent.appendChild(element);

            addDeltas(document, amendment.getDeltas(), element);

        }
    }

    protected void addDeltas(Document document, List<Delta<?>> deltas, Element parent) {
        for (Delta<?> delta : deltas) {
            Element element = document.createElement(DELTA);

            element.setAttribute(ID, delta.getGridId());
            parent.appendChild(element);

            addChanges(document, delta.getChanges(), element);
        }
    }

    protected void addChanges(Document document, List<Change> changes, Element parent) {
        for (Change change : changes) {
            if ((ChangeAction.ADD).equals(change.getAction())) {
                Element element = document.createElement(ADD);
                element.setAttribute(ID, change.getGridId());
                element.setAttribute(INDEX, ((Add) change).getIndex().toString());
                parent.appendChild(element);

                addNode(document, ((ChildrenChange) change).getChild(), element);
            }
        }
    }

    protected void addNode(Document document, PlanTreeNode<?> child, Element parent) {
        if (child instanceof Epoch) {
            Epoch epoch = (Epoch) child;
            Element element = document.createElement(EPOCH);
            element.setAttribute(NAME, epoch.getName());
            element.setAttribute(ID, epoch.getGridId());
            parent.appendChild(element);

            addStudySegments(document, epoch.getStudySegments(), element);
        } else if (child instanceof StudySegment) {
            StudySegment segment = (StudySegment) child;
            Element element = document.createElement(STUDY_SEGMENT);
            element.setAttribute(NAME, segment.getName());
            element.setAttribute(ID, segment.getGridId());
            parent.appendChild(element);

            addPeriods(document, segment.getPeriods(), parent);
        } else if (child instanceof Period) {
            Period period = (Period) child;
            Element element = document.createElement(PERIOD);
            element.setAttribute(NAME, period.getName());
            element.setAttribute(ID, period.getGridId());
            parent.appendChild(element);

            addPlannedActivities(document, period.getPlannedActivities(), parent);
        } else if (child instanceof PlannedActivity) {
            PlannedActivity activity = (PlannedActivity) child;
            Element element = document.createElement(PLANNED_ACTIVITY);
            element.setAttribute(ID, activity.getGridId());

            parent.appendChild(element);
        }
    }

    protected void addStudySegments(Document document, List<StudySegment> studySegments, Element parent) {
        for(StudySegment studySegment : studySegments) {
            addNode(document, studySegment, parent);
        }
    }

    protected void addPeriods(Document document, SortedSet<Period> periods, Element parent) {
        for(Period period : periods) {
            addNode(document, period, parent);
        }
    }

    protected void addPlannedActivities(Document document, List<PlannedActivity> plannedActivities, Element parent) {
        for (PlannedActivity activity : plannedActivities) {
            addNode(document, activity, parent);
        }
    }

    protected String convertToString(Document document) throws Exception {
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
