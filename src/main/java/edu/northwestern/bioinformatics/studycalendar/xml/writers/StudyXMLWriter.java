package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer.STUDY;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer.ASSIGNED_IDENTIFIER;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.beans.factory.annotation.Required;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import static java.lang.String.valueOf;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author John Dzak
 */
public class StudyXMLWriter {
    private DaoFinder daoFinder;
    public static final String XML_NS = "http://www.w3.org/2000/xmlns/";
    public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String PSC_NS = "http://bioinformatics.northwestern.edu/ns/psc";
    public static final String SCHEMA_LOCATION  = "http://bioinformatics.northwestern.edu/ns/psc/psc.xsd";

    public static final String SCHEMA_NAMESPACE_ATTRIBUTE = "xmlns";
    public static final String SCHEMA_LOCATION_ATTRIBUTE  = "xsi:schemaLocation";
    public static final String XML_SCHEMA_ATTRIBUTE       = "xmlns:xsi";

    /* Tag Element constants */
    public static final String PLANNED_CALENDAR_DELTA = "planned-calendar-delta";
    public static final String EPOCH_DELTA = "epoch-delta";
    public static final String STUDY_SEGMENT_DELTA = "study-segment-delta";
    public static final String PERIOD_DELTA = "period-delta";
    public static final String PLANNED_ACTIVITY_DELTA = "planned-activity-delta";
    public static final String ADD = "add";
    public static final String REMOVE = "remove";
    public static final String REORDER = "reorder";
    public static final String PROPERTY_CHANGE = "property-change";

    public static final String AMENDMENT = "amendment";
    public static final String PLANNED_CALENDAR = "planned-calendar";
    public static final String EPOCH = "epoch";
    public static final String STUDY_SEGMENT = "study-segment";
    public static final String PERIOD = "period";
    public static final String PLANNED_ACTIVITY = "planned-activity";
    public static final String ACTIVITY = "activity";
    public static final String SOURCE = "source";

    /* Tag Attribute constants */
    public static final String ID = "id";
    public static final String DATE = "date";
    public static final String NAME = "name";
    public static final String INDEX = "index";
    public static final String MANDATORY = "mandatory";

    public static final String NODE_ID = "node-id";
    public static final String DAY = "day";
    public static final String DETAILS = "details";
    public static final String CONDITION = "condition";
    public static final String DESCRIPTION = "description";
    public static final String SOURCE_ID = "source-id";
    public static final String TYPE_ID = "type-id";
    public static final String CODE = "code";
    public static final String CHILD_ID = "child-id";
    public static final String NEW_INDEX = "new-index";
    public static final String OLD_INDEX = "old-index";
    public static final String PROPERTY_NAME = "property-name";
    public static final String OLD_VALUE = "old-value";
    public static final String NEW_VALUE = "new-value";
    public static final String PREVIOUS_AMENDMENT_ID = "previous-amendment-id";

    private static final Map<String, String[]> optionalAttributes = new HashMap<String, String[]>();

    {
      optionalAttributes.put(AMENDMENT, new String[]{PREVIOUS_AMENDMENT_ID});
      optionalAttributes.put(PLANNED_ACTIVITY, new String[] {DETAILS, CONDITION});
      optionalAttributes.put(PERIOD, new String[] {NAME});
      optionalAttributes.put(ACTIVITY, new String[] {DESCRIPTION, SOURCE_ID});
    }


    public StudyXMLWriter() { }

    @Deprecated // use the spring-configured instance
    public StudyXMLWriter(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    public String createStudyXML(Study study) {
        Document document = createDocument();

        addStudy(document, study);

        return convertToString(document);

    }

    protected Document createDocument() {
        try {
            //get an instance of factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            //get an instance of builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //create an instance of DOM
            return db.newDocument();
        } catch (ParserConfigurationException e) {
            throw new StudyCalendarSystemException("Could not create XML document for serialization", e);
        }
    }

    // TODO: Break all these add methods into an ElementFactory
    protected void addStudy(Document document, Study study) {
        Element element = document.createElementNS(PSC_NS, STUDY);
        element.setAttributeNS(XML_NS, SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS);
        element.setAttributeNS(XML_NS, XML_SCHEMA_ATTRIBUTE, XSI_NS);
        element.setAttributeNS(XSI_NS, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS + ' ' + SCHEMA_LOCATION);

        setAttrib(element, ASSIGNED_IDENTIFIER, study.getAssignedIdentifier());

        addPlannedCalendar(document, study, element);

        List<Amendment> allAmendments = new ArrayList<Amendment>(study.getAmendmentsList());

        // Remove since deelopment amendment add/remove/merge logic hasn't been implmented yet
        //if (study.getDevelopmentAmendment() != null) allAmendments.add(study.getDevelopmentAmendment());

        addAmendments(document, allAmendments, element);
    }

    protected void addPlannedCalendar(Document document, Study study, Element parent) {
        if (study.getPlannedCalendar() != null) {
            Element element = document.createElementNS(PSC_NS, PLANNED_CALENDAR);
            setAttrib(element, ID, study.getPlannedCalendar().getGridId());

            document.appendChild(parent);
            parent.appendChild(element);
        }
    }

    protected void addAmendments(Document document, List<Amendment> amendments, Element parent) {
        for (Amendment amendment : amendments) {
            Element element = document.createElementNS(PSC_NS, AMENDMENT);

            setAttrib(element, NAME, amendment.getName());
            setAttrib(element, MANDATORY, Boolean.toString(amendment.isMandatory()));
            setAttrib(element, ID, amendment.getGridId());

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            setAttrib(element, DATE, formatter.format(amendment.getDate()));

            if (amendment.getPreviousAmendment() != null) {
                setAttrib(element, PREVIOUS_AMENDMENT_ID, amendment.getPreviousAmendment().getGridId());
            }

            parent.appendChild(element);

            addDeltas(document, amendment.getDeltas(), element);

        }
    }

    protected void addDeltas(Document document, List<Delta<?>> deltas, Element parent) {
        for (Delta<?> delta : deltas) {
            Element element;
            if (delta instanceof PlannedCalendarDelta) {
                element = document.createElementNS(PSC_NS, PLANNED_CALENDAR_DELTA);
            } else if (delta instanceof EpochDelta) {
                element = document.createElementNS(PSC_NS, EPOCH_DELTA);
            } else if (delta instanceof StudySegmentDelta) {
                element = document.createElementNS(PSC_NS, STUDY_SEGMENT_DELTA);
            } else if (delta instanceof PeriodDelta) {
                element = document.createElementNS(PSC_NS, PERIOD_DELTA);
            } else if (delta instanceof PlannedActivityDelta) {
                element = document.createElementNS(PSC_NS, PLANNED_ACTIVITY_DELTA);
            } else {
                throw new StudyCalendarError("Delta is not recognized: %s", delta.getClass());
            }

            setAttrib(element, ID, delta.getGridId());
            setAttrib(element, NODE_ID, delta.getNode().getGridId());
            parent.appendChild(element);


            addChanges(document, delta.getChanges(), element, getChildClass(delta.getNode()));
        }
    }

    protected void addChanges(Document document, List<Change> changes, Element parent, Class<?> childClass) {
        for (Change change : changes) {
            if ((ChangeAction.ADD).equals(change.getAction())) {
                Element element = document.createElementNS(PSC_NS, ADD);
                setAttrib(element, ID, change.getGridId());
                Add add = ((Add) change);
                if (add.getIndex() != null) {
                    setAttrib(element, INDEX, add.getIndex().toString());
                }
                parent.appendChild(element);

                addChild(document, ((PlanTreeNode<?>) getChild(((ChildrenChange) change).getChildId(), childClass)), element);
            } else if ((ChangeAction.REMOVE).equals(change.getAction())) {
                Element element = document.createElementNS(PSC_NS, REMOVE);
                setAttrib(element, ID, change.getGridId());
                setAttrib(element, CHILD_ID, ((PlanTreeNode<?>) getChild((((Remove) change).getChildId()), childClass)).getGridId());
                parent.appendChild(element);
            } else if (ChangeAction.REORDER.equals(change.getAction())) {
                Element element = document.createElementNS(PSC_NS, REORDER);
                setAttrib(element, ID, change.getGridId());
                setAttrib(element, CHILD_ID, ((PlanTreeNode<?>) getChild((((Reorder) change).getChildId()), childClass)).getGridId());
                setAttrib(element, OLD_INDEX, ((Reorder) change).getOldIndex().toString());
                setAttrib(element, NEW_INDEX, ((Reorder) change).getNewIndex().toString());
                parent.appendChild(element);
            } else if (ChangeAction.CHANGE_PROPERTY.equals(change.getAction())) {
                Element element = document.createElementNS(PSC_NS, PROPERTY_CHANGE);
                setAttrib(element, ID, change.getGridId());
                setAttrib(element, PROPERTY_NAME, ((PropertyChange) change).getPropertyName());
                setAttrib(element, OLD_VALUE, ((PropertyChange) change).getOldValue());
                setAttrib(element, NEW_VALUE, ((PropertyChange) change).getNewValue());
                parent.appendChild(element);
            } else {
                throw new StudyCalendarError("Change is not recognized: %s", change.getAction());
            }
        }
    }

    protected void addChild(Document document, PlanTreeNode<?> child, Element parent) {
        // TODO: there's a lot of duplicate code here which could be factored out
        if (child instanceof Epoch) {
            Epoch epoch = (Epoch) child;
            Element element = document.createElementNS(PSC_NS, EPOCH);
            setAttrib(element, NAME, epoch.getName());
            setAttrib(element, ID, epoch.getGridId());
            parent.appendChild(element);

            addChildren(document, epoch.getChildren(), element);
        } else if (child instanceof StudySegment) {
            StudySegment segment = (StudySegment) child;
            Element element = document.createElementNS(PSC_NS, STUDY_SEGMENT);
            setAttrib(element, NAME, segment.getName());
            setAttrib(element, ID, segment.getGridId());
            parent.appendChild(element);

            addChildren(document, segment.getChildren(), element);
        } else if (child instanceof Period) {
            Period period = (Period) child;
            Element element = document.createElementNS(PSC_NS, PERIOD);
            setAttrib(element, NAME, period.getName());
            setAttrib(element, ID, period.getGridId());
            parent.appendChild(element);

            addChildren(document, period.getPlannedActivities(), element);
        } else if (child instanceof PlannedActivity) {
            PlannedActivity plannedActivity = (PlannedActivity) child;
            Element element = document.createElementNS(PSC_NS, PLANNED_ACTIVITY);
            setAttrib(element, ID, plannedActivity.getGridId());
            setAttrib(element, DAY, plannedActivity.getDay().toString());
            setAttrib(element, DETAILS, plannedActivity.getDetails());
            setAttrib(element, CONDITION, plannedActivity.getCondition());

            parent.appendChild(element);
            addActivity(document, plannedActivity.getActivity(), element);
        }
    }

    protected void addChildren(Document document, Collection<? extends PlanTreeNode<?>> children, Element parent) {
        for (PlanTreeNode<?> child : children) {
            addChild(document, child, parent);
        }
    }

    protected void addActivity(Document document, Activity activity, Element parent) {
        if (activity.getSource() == null) {
            throw new StudyCalendarError("Source for activity %s (%s) is required and value is null", activity.getName(), activity.getGridId());
        }

        Element element = document.createElementNS(PSC_NS, ACTIVITY);

        setAttrib(element, ID, activity.getGridId());
        setAttrib(element, NAME, activity.getName());
        setAttrib(element, DESCRIPTION, activity.getDescription());
        setAttrib(element, TYPE_ID, valueOf(activity.getType().getId()));
        setAttrib(element, CODE, activity.getCode());

        parent.appendChild(element);
        addSource(document, activity.getSource(), element);
    }

    protected void addSource(Document document, Source source, Element parent) {
        Element element = document.createElementNS(PSC_NS, SOURCE);

        setAttrib(element, ID, source.getGridId());
        setAttrib(element, NAME, source.getName());

        parent.appendChild(element);
    }


    /* XML Helpers */
    protected String convertToString(Document document) {
        StreamResult result;
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            //initialize StreamResult with File object to save to file
            result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new StudyCalendarSystemException("Could not transform XML objects into text", e);
        }

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

    public DomainObject getChild(Integer childId, Class childClass) {
        DomainObjectDao<?> dao = daoFinder.findDao(childClass);
        return dao.getById(childId);
    }

     public Class getChildClass(PlanTreeNode<?> node) {
         Class childClass = null;
         if (node instanceof PlanTreeInnerNode) {
             childClass = ((PlanTreeInnerNode) node).childClass();
         }
         return childClass;
     }

    ////// CONFIGURATION

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
