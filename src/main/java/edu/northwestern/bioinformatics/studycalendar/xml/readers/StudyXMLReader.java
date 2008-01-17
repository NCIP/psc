package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer.ASSIGNED_IDENTIFIER;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode.cast;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.Schema;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import static edu.nwu.bioinformatics.commons.CollectionUtils.firstElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Boolean.valueOf;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import static java.util.Collections.singletonList;

@Transactional
public class StudyXMLReader  {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private StudyDao studyDao;
    private DeltaDao deltaDao;
    private AmendmentDao amendmentDao;
    private ChangeDao changeDao;
    private TemplateService templateService;
    private SourceDao sourceDao;
    private DeltaService deltaService;
    private ActivityDao activityDao;
    private PlannedCalendarDao plannedCalendarDao;
    private StudyService studyService;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private Map<String, Class<? extends PlanTreeNode>> elementToPlanTreeNodeMapping = new HashMap<String, Class<? extends PlanTreeNode>>();

    {
        elementToPlanTreeNodeMapping.put(PLANNED_CALENDAR, PlannedCalendar.class);
        elementToPlanTreeNodeMapping.put(EPOCH, Epoch.class);
        elementToPlanTreeNodeMapping.put(STUDY_SEGMENT, StudySegment.class);
        elementToPlanTreeNodeMapping.put(PERIOD, Period.class);
        elementToPlanTreeNodeMapping.put(PLANNED_ACTIVITY, PlannedActivity.class);
    }

    public Study readAndSave(InputStream inputStream) {
        Study study = read(inputStream);
        studyService.save(study);
        return study;
    }

    protected Study read(InputStream dataFile) {
          Document doc;

          try {
              // create a SchemaFactory that conforms to W3C XML Schema
              SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

              javax.xml.validation.Schema schema = sf.newSchema(Schema.template.file());

              // get a DOM factory
              DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

              // configure the factory
              dbf.setNamespaceAware(true);

              // Ignore whitespace
              dbf.setIgnoringElementContentWhitespace(true);

              // set schema on the factory
              dbf.setSchema(schema);

              // create a new parser that validates documents against a schema
              DocumentBuilder db = dbf.newDocumentBuilder();

              doc = db.parse(dataFile);
          } catch (SAXException e) {
              throw new StudyCalendarValidationException("XML parsing failed", e);
          } catch (ParserConfigurationException e) {
              throw new StudyCalendarSystemException("XML parser setup failed", e);
          } catch (IOException e) {
              throw new StudyCalendarSystemException("Reading XML failed", e);
          }

          return parseStudy(doc);
      }


    protected Study parseStudy(Document doc) {
        Element element = doc.getDocumentElement();
        String gridId = element.getAttribute(ID);
        Study study = studyDao.getByGridId(gridId);
        if (study == null) {
            study = new Study();
            study.setGridId(gridId);
            study.setAssignedIdentifier(element.getAttribute(ASSIGNED_IDENTIFIER));
        }

        addPlannedCalendar(element, study);
        addAmendments(element, study);

        return study;
    }

    protected void addPlannedCalendar(Element parentElement, Study parent) {
        NodeList nodes = parentElement.getElementsByTagName(PLANNED_CALENDAR);

        Element element = ((Element)nodes.item(0));
        String gridId = element.getAttribute(ID);
        PlannedCalendar calendar = plannedCalendarDao.getByGridId(gridId);
        if (calendar == null) {
            calendar = new PlannedCalendar();
            calendar.setGridId(gridId);
            calendar.setStudy(parent);
            parent.setPlannedCalendar(calendar);
        }
    }


    protected void addAmendments(Element parentElement, Study parent) {
        List<Amendment> amendments = new ArrayList<Amendment>();

        for (Node node : list(parentElement.getElementsByTagName(AMENDMENT))) {
            Element element = (Element) node;

            String gridId = element.getAttribute(ID);
            Amendment amendment = amendmentDao.getByGridId(gridId);
            if (amendment == null) {
                amendment = new Amendment();
                amendment.setGridId(gridId);
                amendment.setName(element.getAttribute(NAME));
                amendment.setMandatory(valueOf(element.getAttribute(MANDATORY)));
                 String date = element.getAttribute(DATE);
                try {
                    amendment.setDate(formatter.parse(date));
                } catch (ParseException e) {
                    throw new StudyCalendarValidationException("Could not parse date \"%s\"", date);
                }

                String prevAmendmentGridId = element.getAttribute(PREVIOUS_AMENDMENT_ID);
                if (prevAmendmentGridId != null) {
                    for (Amendment searchAmendment : amendments) {
                        if (searchAmendment.getGridId().equals(prevAmendmentGridId)) {
                            amendment.setPreviousAmendment(searchAmendment);
                        }
                    }
                }
                addDeltas(element, amendment, parent);
                deltaService.apply(parent, amendment);
            }
            amendments.add(amendment);
        }

        Collections.reverse(amendments);
        parent.setAmendment(firstElement(amendments));
    }


    protected void addDeltas(Element parentElement, Amendment amendment, Study study) {
        for (Node deltaNode : list(parentElement.getChildNodes())) {
            Element deltaElement = (Element) deltaNode;

            // Get Delta from datastore
            String deltaGridId = deltaElement.getAttribute(ID);
            Delta<? extends PlanTreeNode<?>> delta = deltaDao.getByGridId(deltaGridId);

            // Create Delta if not in datastore
            if (delta == null) {
                delta = createDelta(deltaElement, study);

                delta.setRevision(amendment);
                delta.setGridId(deltaGridId);
                amendment.addDelta(delta);

                addChanges(deltaElement, delta, study);
            }
        }
    }

    private Delta<? extends PlanTreeNode<?>> createDelta(Element element, Study study) {
        if (PLANNED_CALENDAR_DELTA.equals(element.getNodeName())) {
            PlannedCalendarDelta delta = new PlannedCalendarDelta();

            String gridId = element.getAttribute(NODE_ID);

            PlannedCalendar calendar = (PlannedCalendar) findPlanTreeNode(PlannedCalendar.class, element.getAttribute(NODE_ID), study);

            if (calendar == null) {
                throw new StudyCalendarError("Cannot find Node for: %s [%s]", PlannedCalendar.class.getName(), gridId);
            }

            delta.setNode(calendar);

            return delta;
        } else if (EPOCH_DELTA.equals(element.getNodeName())) {
            EpochDelta delta = new EpochDelta();

            String gridId = element.getAttribute(NODE_ID);

            Epoch epoch = (Epoch) findPlanTreeNode(Epoch.class, element.getAttribute(NODE_ID), study);

            if (epoch == null) {
                throw new StudyCalendarError("Cannot find Node for: %s [%s]", Epoch.class.getName(), gridId);
            }

            delta.setNode(epoch);

            return delta;
        } else if (STUDY_SEGMENT_DELTA.equals(element.getNodeName())) {
            StudySegmentDelta delta = new StudySegmentDelta();

            String gridId = element.getAttribute(NODE_ID);

            StudySegment segment = (StudySegment) findPlanTreeNode(StudySegment.class, element.getAttribute(NODE_ID), study);

            if (segment == null) {
                throw new StudyCalendarError("Cannot find Node for: %s [%s]", StudySegment.class.getName(), gridId);
            }

            delta.setNode(segment);

            return delta;
        } else if (PERIOD_DELTA.equals(element.getNodeName())) {
            PeriodDelta delta = new PeriodDelta();

            String gridId = element.getAttribute(NODE_ID);

            Period period = (Period) findPlanTreeNode(Period.class, element.getAttribute(NODE_ID), study);

            if (period == null) {
                throw new StudyCalendarError("Cannot find Node for: %s [%s]", Period.class.getName(), gridId);
            }

            delta.setNode(period);

            return delta;
        } else if (PLANNED_ACTIVITY_DELTA.equals(element.getNodeName())) {
            PlannedActivityDelta delta = new PlannedActivityDelta();

            String gridId = element.getAttribute(NODE_ID);

            PlannedActivity plannedActivity = (PlannedActivity) findPlanTreeNode(PlannedActivity.class, element.getAttribute(NODE_ID), study);

            if (plannedActivity == null) {
                throw new StudyCalendarError("Cannot find Node for: %s [%s]", PlannedActivity.class.getName(), gridId);
            }

            delta.setNode(plannedActivity);

            return delta;
        } else {
            throw new StudyCalendarError("Cannot find Delta Node for: %s", element.getNodeName());
        }
    }

    protected void addChanges(Element parent, Delta<?> delta, Study study) {
        for (Node addNode : list(parent.getChildNodes())) {
            Element element = (Element) addNode;

             // Get Add if Delta Exists
            String gridId = element.getAttribute(ID);
            Change change = changeDao.getByGridId(gridId);

            if (change == null) {
                change = createChange(element, study);

                change.setGridId(gridId);
                delta.addChange(change);

                addChildToChange(element, change, study);
            }
        }
    }

    private Change createChange(Element element, Study study) {
        if (ADD.equals(element.getNodeName())) {
            return new Add();
        } else if(REMOVE.equals(element.getNodeName()) ){
            Remove remove = new Remove();

            // Find Child Element
            String childGridId = element.getAttribute(CHILD_ID);
            Element changeElement = getElementById(element, childGridId);
            PlanTreeNode<?> planTreeNode = findPlanTreeNode(changeElement, childGridId, study);

            remove.setChild(planTreeNode);
            remove.setChildId(planTreeNode.getId());

            return remove;
        } else if (REORDER.equals(element.getNodeName())) {
            Reorder reorder = new Reorder();

            // Find Child Element
            String childGridId = element.getAttribute(CHILD_ID);
            Element changeElement = getElementById(element, childGridId);
            PlanTreeNode<?> planTreeNode = findPlanTreeNode(changeElement, childGridId, study);

            reorder.setChild(planTreeNode);
            reorder.setChildId(planTreeNode.getId());
            reorder.setOldIndex(new Integer(element.getAttribute(OLD_INDEX)));
            reorder.setNewIndex(new Integer(element.getAttribute(NEW_INDEX)));

            return reorder;
        } else if (PROPERTY_CHANGE.equals(element.getNodeName())) {
            PropertyChange propertyChange = new PropertyChange();

            propertyChange.setGridId(element.getAttribute(CHILD_ID));
            propertyChange.setPropertyName(element.getAttribute(PROPERTY_NAME));
            propertyChange.setOldValue(element.getAttribute(OLD_VALUE));
            propertyChange.setNewValue(element.getAttribute(NEW_VALUE));

            return propertyChange;
        } else {
            throw new StudyCalendarError("Cannot find Change Node for: %s", element.getNodeName());
        }
    }


    protected void addChildToChange(Element parent, Change change, Study study) {
        for (Node childNode : list(parent.getChildNodes())) {
            Element element = (Element) childNode;

            if (change instanceof ChildrenChange) {

                PlanTreeNode<?> child = findPlanTreeNode(element, element.getAttribute(ID), study);

                if (child == null) {
                    child = createPlanTreeNode(element, null);

                    ((ChildrenChange) change).setChild(child);
                    addChildrenToPlanTreeNode(element, child, study);
                }
            }
        }
    }

    protected void addChildrenToPlanTreeNode(Element parent, PlanTreeNode<?> parentTreeNode, Study study) {
        for (Node childNode : list(parent.getChildNodes())) {
            Element element = (Element) childNode;

            PlanTreeNode<?> child = findPlanTreeNode(element, element.getAttribute(ID), study);

            if (child == null) {
                child = createPlanTreeNode(element, null);

                cast(parentTreeNode).addChild(child);

                if (child instanceof PlanTreeInnerNode) {
                    addChildrenToPlanTreeNode(element, child, study);
                }
            }
        }
    }

    private PlanTreeNode<?> createPlanTreeNode(Element childElement, PlanTreeInnerNode parent ) {
        Class<? extends PlanTreeNode> clazz = getPlanTreeNodeClass(childElement);
        PlanTreeNode<?> planTreeNode = createInstance(clazz);
        if (planTreeNode instanceof Epoch) {
            planTreeNode.setGridId(childElement.getAttribute(ID));
            ((Epoch)planTreeNode).setName(childElement.getAttribute(NAME));
        } else if (STUDY_SEGMENT.equals(childElement.getNodeName())) {
            planTreeNode.setGridId(childElement.getAttribute(ID));
            ((StudySegment) planTreeNode).setName(childElement.getAttribute(NAME));
        } else if  (PERIOD.equals(childElement.getNodeName())) {
            planTreeNode.setGridId(childElement.getAttribute(ID));
            ((Period)planTreeNode).setName(childElement.getAttribute(NAME));
        } else if (PLANNED_ACTIVITY.equals(childElement.getNodeName())) {
            planTreeNode.setGridId(childElement.getAttribute(ID));
            ((PlannedActivity)planTreeNode).setDay(new Integer(childElement.getAttribute(DAY)));
            ((PlannedActivity)planTreeNode).setDetails(childElement.getAttribute(DETAILS));
            ((PlannedActivity)planTreeNode).setCondition(childElement.getAttribute(CONDITION));

            addActivity(childElement, ((PlannedActivity)planTreeNode));
        } else {
            throw new StudyCalendarError("Cannot find Child Node for: %s", childElement.getNodeName());
        }
        return planTreeNode;
    }

    private void addActivity(Element parentElement, PlannedActivity parent ) {
        Element element = (Element) parentElement.getFirstChild();

        String code = element.getAttribute(CODE);
        String sourceName = ((Element) element.getFirstChild()).getAttribute(NAME);

        Activity activity = activityDao.getByCodeAndSourceName(code, sourceName);
        if (activity == null) {
            activity =  new Activity();
            activity.setGridId(element.getAttribute(ID));
            activity.setName(element.getAttribute(NAME));
            activity.setDescription(element.getAttribute(DESCRIPTION));
            activity.setType(ActivityType.getById(Integer.parseInt(element.getAttribute(TYPE_ID))));
            activity.setCode(code);

            addSource(element, activity);
            activityDao.save(activity);
        }
        parent.setActivity(activity);
    }

    private void addSource(Element parentElement, Activity parent ) {
        Element element = (Element) parentElement.getFirstChild();

        String name = element.getAttribute(NAME);
        Source source = sourceDao.getByName(name);
        if (source == null) {
            source =  new Source();
            source.setName(name);
            source.setGridId(element.getAttribute(ID));
            parent.setSource(source);
            source.getActivities().add(parent);
            sourceDao.save(source);
        }
    }

    /* Class Helpers */

    private Class<? extends PlanTreeNode> getPlanTreeNodeClass(Element childElement) {
        Class<? extends PlanTreeNode> clazz = elementToPlanTreeNodeMapping.get(childElement.getNodeName());
        if (clazz == null) {
            throw new StudyCalendarError("Cannot find Child Node for: %s", childElement.getNodeName());
        }
        return clazz;
    }

    private PlanTreeNode<?> findPlanTreeNode(Class<? extends PlanTreeNode> aClass, String gridId, Study study) {
        PlanTreeNode<?> param = createInstance(aClass);
        param.setGridId(gridId);

        return templateService.findEquivalentChild(study, param);
    }

    private <T extends PlanTreeNode> T createInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new StudyCalendarError("Could not import from template XML", e);
        } catch (IllegalAccessException e) {
            throw new StudyCalendarError("Could not import from template XML", e);
        }
    }

    private PlanTreeNode<?> findPlanTreeNode(Element childElement, String gridId, Study study) {
        Class<? extends PlanTreeNode> clazz = getPlanTreeNodeClass(childElement);
        return findPlanTreeNode(clazz, gridId, study);
    }


    protected Element getElementById(Node node, String string) {
        List<Node> nodes = getAllNodes(node.getOwnerDocument().getFirstChild());
        Node found = findNodeById(nodes, string);

        return (Element) found;
    }

    protected List<Node> getAllNodes(Node node) {
        if (!node.hasChildNodes())
            return singletonList(node);

        List<Node> master = new ArrayList<Node>();
        master.add(node);

        List<Node> children = new NodeListCollection(node.getChildNodes());
        for (Node child : children) {
            master.addAll(getAllNodes(child));
        }

        return master ;
    }

    protected Node findNodeById(List<Node> nodes, String id) {
        for (Node node : nodes) {
            Element element = (Element) node;
            if (element.getAttribute(ID).equals(id)) {
                return node;
            }
        }
        return null;
    }

    private List<Node> list(NodeList nodelist) {
        return new NodeListCollection(nodelist);
    }



    private class NodeListCollection extends AbstractList<Node> {

        private NodeList nodeList;

        public NodeListCollection(NodeList nodeList) {
            this.nodeList = nodeList;
        }

        public Node get(int i) {
            return nodeList.item(i);
        }

        public int size() {
            return nodeList.getLength();
        }

    }

    /* Dao and Service Setters */
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    public void setDeltaDao(DeltaDao deltaDao) {
        this.deltaDao = deltaDao;
    }
    public void setChangeDao(ChangeDao changeDao) {
        this.changeDao = changeDao;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public void setPlannedCalendarDao(PlannedCalendarDao plannedCalendarDao) {
        this.plannedCalendarDao = plannedCalendarDao;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }
}
