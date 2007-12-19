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
import java.util.*;

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
    public static final String NODE_ID = "node-id";
    public static final String DAY = "day";
    public static final String DETAILS = "details";
    public static final String CONDITION = "condition";

    private static final Map<String, String[]> optionalAttributes = new HashMap<String, String[]>();
    {
      optionalAttributes.put(PLANNED_ACTIVITY, new String[] {DETAILS, CONDITION});
    };


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

        setAttrib(rootElement, ID, study.getGridId());
        setAttrib(rootElement, ASSIGNED_IDENTIFIER, study.getAssignedIdentifier());

        addPlannedCalendar(document, study, rootElement);

        List<Amendment> allAmendments = new ArrayList(study.getAmendmentsList());
        if (study.getDevelopmentAmendment() != null) allAmendments.add(study.getDevelopmentAmendment());
        addAmendments(document, allAmendments, rootElement);

    }

    protected void addPlannedCalendar(Document document, Study study, Element rootElement) {
        if (study.getPlannedCalendar() != null) {
            Element element = document.createElement(PLANNDED_CALENDAR);
            setAttrib(element, ID, study.getPlannedCalendar().getGridId());

            document.appendChild(rootElement);
            rootElement.appendChild(element);
        }
    }

    protected void addAmendments(Document document, List<Amendment> amendments, Element parent) {
        for (Amendment amendment : amendments) {
            Element element = document.createElement(AMENDMENT);

            setAttrib(element, NAME, amendment.getName());
            setAttrib(element, MANDATORY, Boolean.toString(amendment.isMandatory()));
            setAttrib(element, ID, amendment.getGridId());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            setAttrib(element, DATE, formatter.format(amendment.getDate()));

            parent.appendChild(element);

            addDeltas(document, amendment.getDeltas(), element);

        }
    }

    protected void addDeltas(Document document, List<Delta<?>> deltas, Element parent) {
        for (Delta<?> delta : deltas) {
            Element element = document.createElement(DELTA);

            setAttrib(element, ID, delta.getGridId());
            setAttrib(element, NODE_ID, delta.getNode().getGridId());
            parent.appendChild(element);

            addChanges(document, delta.getChanges(), element);
        }
    }

    protected void addChanges(Document document, List<Change> changes, Element parent) {
        for (Change change : changes) {
            if ((ChangeAction.ADD).equals(change.getAction())) {
                Element element = document.createElement(ADD);
                setAttrib(element, ID, change.getGridId());
                setAttrib(element, INDEX, ((Add) change).getIndex().toString());
                parent.appendChild(element);

                addNode(document, ((ChildrenChange) change).getChild(), element);
            }
        }
    }

    protected void addNode(Document document, PlanTreeNode<?> child, Element parent) {
        if (child instanceof Epoch) {
            Epoch epoch = (Epoch) child;
            Element element = document.createElement(EPOCH);
            setAttrib(element, NAME, epoch.getName());
            setAttrib(element, ID, epoch.getGridId());
            parent.appendChild(element);

            addStudySegments(document, epoch.getStudySegments(), element);
        } else if (child instanceof StudySegment) {
            StudySegment segment = (StudySegment) child;
            Element element = document.createElement(STUDY_SEGMENT);
            setAttrib(element, NAME, segment.getName());
            setAttrib(element, ID, segment.getGridId());
            parent.appendChild(element);

            addPeriods(document, segment.getPeriods(), parent);
        } else if (child instanceof Period) {
            Period period = (Period) child;
            Element element = document.createElement(PERIOD);
            setAttrib(element, NAME, period.getName());
            setAttrib(element, ID, period.getGridId());
            parent.appendChild(element);

            addPlannedActivities(document, period.getPlannedActivities(), parent);
        } else if (child instanceof PlannedActivity) {
            PlannedActivity activity = (PlannedActivity) child;
            Element element = document.createElement(PLANNED_ACTIVITY);
            setAttrib(element, ID, activity.getGridId());
            setAttrib(element, DAY, activity.getDay().toString());
            setAttrib(element, DETAILS, activity.getDetails());
            setAttrib(element, CONDITION, activity.getCondition());

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

   private void setAttrib(Element element, String name, String value) {
       if (value != null) {
           element.setAttribute(name, value);
           return;
       }

       String tagName = element.getTagName();
       String[] optionalAttribs = optionalAttributes.get(tagName);
       if (optionalAttribs != null) {
           for (String optionalAttrib : optionalAttribs) {
               if (name.equals(optionalAttrib)) return;
           }
       }

       throw new StudyCalendarError("Attribute is required and value is null: %s", name);
   }

}
