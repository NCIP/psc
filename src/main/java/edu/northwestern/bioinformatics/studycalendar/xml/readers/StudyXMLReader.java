package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import static org.apache.commons.collections.CollectionUtils.union;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.EPOCH;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ADD;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.INDEX;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.NAME;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.NODE_ID;

import static java.lang.Boolean.valueOf;

import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.PLANNDED_CALENDAR;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ASSIGNED_IDENTIFIER;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ID;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.commons.collections.CollectionUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;
import java.text.SimpleDateFormat;

public class StudyXMLReader  {
    private StudyDao studyDao;
    private DeltaDao deltaDao;
    private AmendmentDao amendmentDao;
    private PlannedCalendarDao plannedCalendarDao;
    private ChangeDao changeDao;
    private EpochDao epochDao;
    private AmendmentService amendmentService;
    private StudySegmentDao studySegmentDao;
    private PeriodDao periodDao;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public Study read(InputStream dataFile) throws Exception {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        //Using factory get an instance of document builder
        DocumentBuilder db = dbf.newDocumentBuilder();

        //parse using builder to get DOM representation of the XML file
        Document dom = db.parse(dataFile);

        return parseStudy(dom);
    }

    protected Study parseStudy(Document doc) throws Exception {
        Element element = doc.getDocumentElement();
        String gridId = element.getAttribute(ID);
        Study study = studyDao.getByGridId(gridId);
        if (study == null) {
            study = new Study();
            study.setGridId(gridId);
            study.setAssignedIdentifier(element.getAttribute(ASSIGNED_IDENTIFIER));
        }

        // Parse and set PlannedCalendar
        PlannedCalendar calendar = parsePlannedCalendar(doc, study);
        study.setPlannedCalendar(calendar);

        // Parse and set Amendment
        Amendment amendment = parseAmendment(doc, study);
        study.setAmendment(amendment);

        return study;
    }

    protected PlannedCalendar parsePlannedCalendar(Document doc, Study study) throws Exception {
        NodeList nodes = doc.getElementsByTagName(PLANNDED_CALENDAR);

        Element element = ((Element)nodes.item(0));
        String gridId = element.getAttribute(ID);
        PlannedCalendar calendar = plannedCalendarDao.getByGridId(gridId);
        if (calendar == null) {
            calendar = new PlannedCalendar();
            calendar.setGridId(gridId);
            calendar.setStudy(study);
        }

        return calendar;
    }


    protected Amendment parseAmendment(Document doc, Study study) throws Exception {
        List<Amendment> amendments = new ArrayList<Amendment>();

        NodeList nodes = doc.getElementsByTagName(StudyXMLWriter.AMENDMENT);
        for (int i=0; i < nodes.getLength(); i++) {

            Element element = ((Element)nodes.item(i));

            String gridId = element.getAttribute(ID);
            Amendment amendment = amendmentDao.getByGridId(gridId);
            if (amendment == null) {
                amendment = new Amendment();
                amendment.setGridId(gridId);
                amendment.setName(element.getAttribute(NAME));
                amendment.setMandatory(valueOf(element.getAttribute(StudyXMLWriter.MANDATORY)));
                amendment.setDate(formatter.parse(element.getAttribute(StudyXMLWriter.DATE)));

                String prevAmendmentGridId = element.getAttribute(StudyXMLWriter.PREVIOUS_AMENDMENT_ID);
                if (prevAmendmentGridId != null) {
                    for (Amendment searchAmendment : amendments) {
                        if (searchAmendment.getGridId().equals(prevAmendmentGridId)) {
                            amendment.setPreviousAmendment(searchAmendment);
                        }
                    }
                }
            }
            amendments.add(amendment);

            List<Delta<?>> deltas = parseDeltas(doc, amendment);
            amendment.setDeltas(deltas);
        }

        if (amendments.isEmpty()) {
            return null;
        }

        return (amendments.size() == 0) ? amendments.get(0) : amendments.get(amendments.size() - 1);
    }


    // TODO: Use Delta.createDeltaFor method
    protected List<Delta<?>> parseDeltas(Document doc, Amendment amendment) {
        List<Delta<?>> deltas = new ArrayList<Delta<?>>();

        NodeList allDeltaNodes = doc.getElementsByTagName(StudyXMLWriter.DELTA);
        NodeList allAmendmentChildrenNodes = doc.getElementById(amendment.getGridId()).getChildNodes();

        List<Node> deltaNodes = intersection(allDeltaNodes, allAmendmentChildrenNodes);

        for (Node deltaNode : deltaNodes) {
            Element element = (Element) deltaNode;
            // Get Delta if Delta Exists
            String deltaGridId = element.getAttribute(ID);
            Delta<?> delta = deltaDao.getByGridId(deltaGridId);

            if (delta == null) {
                // Get Delta Node if Node Exists
                String nodeGridId = element.getAttribute(NODE_ID);
                Node node = doc.getElementById(nodeGridId);

                PlanTreeNode<?> planTreeNode;
                if(PLANNDED_CALENDAR.equals(node.getNodeName())) {
                    planTreeNode = plannedCalendarDao.getByGridId(nodeGridId);

                    delta = new PlannedCalendarDelta();
                    ((PlannedCalendarDelta)delta).setNode((PlannedCalendar) planTreeNode);
                    //Delta.createDeltaFor(planTreeNode, change);
                } else if (EPOCH.equals(node.getNodeName())) {
                    // Set Parameter for getAmendedNode
                    planTreeNode = new Epoch();
                    planTreeNode.setGridId(nodeGridId);

                    // getting amended Node for the revision
                    planTreeNode = amendmentService.getAmendedNode(planTreeNode, amendment);

                    delta = new EpochDelta();
                    ((EpochDelta)delta).setNode((Epoch) planTreeNode);
                } else if (StudyXMLWriter.STUDY_SEGMENT.equals(node.getNodeName())) {
                    planTreeNode = new StudySegment();
                    planTreeNode.setGridId(nodeGridId);

                    planTreeNode = amendmentService.getAmendedNode(planTreeNode, amendment);

                    delta = new StudySegmentDelta();
                    ((StudySegmentDelta) delta).setNode((StudySegment) planTreeNode);
                } else {
                    throw new StudyCalendarError("Cannot find PlanTreeNode for: %s", node.getNodeName());
                }

                if (planTreeNode == null) {
                    throw new StudyCalendarError("Cannot find Planned Calendar: %s", nodeGridId);
                }

                delta.setRevision(amendment);
                delta.setGridId(deltaGridId);
            }
            List<Change> changes = parseChanges(doc, deltaGridId);
            delta.addChanges(changes.toArray(new Change[]{}));

            deltas.add(delta);
        }

        return deltas;
    }

    protected List<Change> parseChanges(Document doc, String deltaGridId) {
        List<Change> changes = new ArrayList<Change>();

        NodeList allAddNodes = doc.getElementsByTagName(ADD);
        NodeList allDeltaChildrenNodes = doc.getElementById(deltaGridId).getChildNodes();

        List<Node> addNodes = intersection(allAddNodes, allDeltaChildrenNodes);

        for (Node addNode : addNodes) {
            Element element = (Element) addNode;
             // Get Add if Delta Exists
            String gridId = element.getAttribute(ID);
            Change change = changeDao.getByGridId(gridId);
            if (change == null) {
                if (StudyXMLWriter.ADD.equals(element.getNodeName())) {
                    PlanTreeNode<?> changeChild = parsePlanTreeNode(doc, gridId);
                    change = Add.create(changeChild, Integer.parseInt(element.getAttribute(INDEX)));
                } else {
                    throw new StudyCalendarError("Cannot find Change Node for: %s", element.getNodeName());
                }

                change.setGridId(gridId);
            }


            changes.add(change);
        }

        return changes;
    }

    protected PlanTreeNode<?> parsePlanTreeNode(Document doc, String changeGridId) {
        PlanTreeNode<?> childPlanTreeNode = null;

        NodeList allEpochNodes = doc.getElementsByTagName(EPOCH);
        NodeList allSegmentNodes = doc.getElementsByTagName(StudyXMLWriter.STUDY_SEGMENT);
        NodeList allPeriodNodes = doc.getElementsByTagName(StudyXMLWriter.PERIOD);

        NodeList allChangeChildrenNodes = doc.getElementById(changeGridId).getChildNodes();

        List<Node> childNodes =
                intersection( allChangeChildrenNodes,
                union ( new NodeListCollection(allPeriodNodes),
                        union(new NodeListCollection(allEpochNodes), new NodeListCollection(allSegmentNodes))));

        if (!childNodes.isEmpty()) {
            Element element = (Element) childNodes.get(0);
            // There should be one child Node
            String gridId = element.getAttribute(ID);

            if (StudyXMLWriter.EPOCH.equals(element.getNodeName())) {
                childPlanTreeNode = epochDao.getByGridId(gridId);
                if (childPlanTreeNode == null) {
                    childPlanTreeNode = new Epoch();
                    ((Epoch) childPlanTreeNode).setName(element.getAttribute(NAME));
                    childPlanTreeNode.setGridId(gridId);
                }
            } else if (StudyXMLWriter.STUDY_SEGMENT.equals(element.getNodeName())) {
                childPlanTreeNode = studySegmentDao.getByGridId(gridId);
                if (childPlanTreeNode == null) {
                    childPlanTreeNode = new StudySegment();
                    ((StudySegment) childPlanTreeNode).setName(element.getAttribute(NAME));
                    childPlanTreeNode.setGridId(gridId);
                }
            } else if  (StudyXMLWriter.PERIOD.equals(element.getNodeName())) {
                childPlanTreeNode = periodDao.getByGridId(gridId);
                if (childPlanTreeNode == null) {
                    childPlanTreeNode = new Period();
                    ((Period) childPlanTreeNode).setName(element.getAttribute(NAME));
                    childPlanTreeNode.setGridId(gridId);
                }
            } else {
                throw new StudyCalendarError("Cannot find Child Node for: %s", element.getNodeName());
            }
        }
        
        return childPlanTreeNode;
    }

    /* Class Helpers */
    private List<Node> intersection(NodeList list0, NodeList list1) {
        return intersection(new NodeListCollection(list0), new NodeListCollection(list1));
    }

    private List<Node> intersection(NodeList list0, Collection<Node> list1) {
        return intersection(new NodeListCollection(list0), list1);
    }

    private List<Node> intersection(Collection<Node> list0, Collection<Node> list1) {
        List<Node> resultList = new ArrayList<Node>();
        for (Node outter : list0) {
            for (Node inner : list1) {
                if (outter == inner) {
                    resultList.add(outter);
                }
            }
        }
        return resultList;
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

    public void setPlannedCalendarDao(PlannedCalendarDao plannedCalendarDao) {
        this.plannedCalendarDao = plannedCalendarDao;
    }

    public void setDeltaDao(DeltaDao deltaDao) {
        this.deltaDao = deltaDao;
    }

    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }

    public void setChangeDao(ChangeDao changeDao) {
        this.changeDao = changeDao;
    }

    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }
}
