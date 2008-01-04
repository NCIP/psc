package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import static edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode.cast;
import static edu.nwu.bioinformatics.commons.CollectionUtils.firstElement;
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
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
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
    private TemplateService templateService;

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

        NodeList nodes = doc.getElementsByTagName(AMENDMENT);
        for (int i=0; i < nodes.getLength(); i++) {

            Element element = ((Element)nodes.item(i));

            String gridId = element.getAttribute(ID);
            Amendment amendment = amendmentDao.getByGridId(gridId);
            if (amendment == null) {
                amendment = new Amendment();
                amendment.setGridId(gridId);
                amendment.setName(element.getAttribute(NAME));
                amendment.setMandatory(valueOf(element.getAttribute(MANDATORY)));
                amendment.setDate(formatter.parse(element.getAttribute(DATE)));

                String prevAmendmentGridId = element.getAttribute(PREVIOUS_AMENDMENT_ID);
                if (prevAmendmentGridId != null) {
                    for (Amendment searchAmendment : amendments) {
                        if (searchAmendment.getGridId().equals(prevAmendmentGridId)) {
                            amendment.setPreviousAmendment(searchAmendment);
                        }
                    }
                }
            }
            amendments.add(amendment);

            List<Delta<?>> deltas = parseDeltas(doc, amendment, study);
            amendment.setDeltas(deltas);
        }

        if (amendments.isEmpty()) {
            return null;
        }

        return (amendments.size() == 0) ? amendments.get(0) : amendments.get(amendments.size() - 1);
    }


    // TODO: Use Delta.createDeltaFor method
    protected List<Delta<?>> parseDeltas(Document doc, Amendment amendment, Study study) {
        List<Delta<?>> deltas = new ArrayList<Delta<?>>();

        NodeList allDeltaNodes = doc.getElementsByTagName(DELTA);
        NodeList allAmendmentChildrenNodes = doc.getElementById(amendment.getGridId()).getChildNodes();

        Collection<Node> deltaNodes = CollectionUtils.intersection(new NodeListCollection(allDeltaNodes), new NodeListCollection(allAmendmentChildrenNodes));

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
                    planTreeNode = templateService.findEquivalentChild(study, planTreeNode);

                    delta = new EpochDelta();
                    ((EpochDelta)delta).setNode((Epoch) planTreeNode);
                } else if (STUDY_SEGMENT.equals(node.getNodeName())) {
                    planTreeNode = new StudySegment();
                    planTreeNode.setGridId(nodeGridId);

                    planTreeNode = templateService.findEquivalentChild(study, planTreeNode);

                    delta = new StudySegmentDelta();
                    ((StudySegmentDelta) delta).setNode((StudySegment) planTreeNode);
                } else {
                    throw new StudyCalendarError("Cannot find PlanTreeNode for: %s", node.getNodeName());
                }

                if (planTreeNode == null) {
                    throw new StudyCalendarError("Cannot find PlannedNode: %s", nodeGridId);
                }

                delta.setRevision(amendment);
                delta.setGridId(deltaGridId);
            }
            List<Change> changes = parseChanges(doc, deltaGridId, study);
            delta.addChanges(changes.toArray(new Change[]{}));

            deltas.add(delta);
        }

        return deltas;
    }

    protected List<Change> parseChanges(Document doc, String deltaGridId, Study study) {
        List<Change> changes = new ArrayList<Change>();

        NodeList allAddNodes = doc.getElementsByTagName(ADD);
        NodeList allDeltaChildrenNodes = doc.getElementById(deltaGridId).getChildNodes();

        Collection<Node> addNodes = CollectionUtils.intersection(new NodeListCollection(allAddNodes), new NodeListCollection(allDeltaChildrenNodes));

        for (Node addNode : addNodes) {
            Element element = (Element) addNode;
             // Get Add if Delta Exists
            String gridId = element.getAttribute(ID);
            Change change = changeDao.getByGridId(gridId);
            if (change == null) {
                if (ADD.equals(element.getNodeName())) {
                    PlanTreeNode<?> changeChild = parsePlanTreeNode(doc, gridId, study);
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

    protected PlanTreeNode<?> parsePlanTreeNode(Document doc, String changeGridId, Study study) {
        return firstElement(parsePlanTreeNodes(doc, changeGridId, study));
    }

    protected List<PlanTreeNode<?>> parsePlanTreeNodes(Document doc, String parentGridId, Study study) {
        List<PlanTreeNode<?>> planTreeNodes = new ArrayList<PlanTreeNode<?>>();

        NodeList allEpochNodes = doc.getElementsByTagName(EPOCH);
        NodeList allSegmentNodes = doc.getElementsByTagName(STUDY_SEGMENT);
        NodeList allPeriodNodes = doc.getElementsByTagName(PERIOD);

        NodeList allChangeChildrenNodes = doc.getElementById(parentGridId).getChildNodes();

        Collection<Node> childNodes =
                CollectionUtils.intersection( new NodeListCollection(allChangeChildrenNodes),
                    union ( new NodeListCollection(allPeriodNodes),
                        union( new NodeListCollection(allEpochNodes), new NodeListCollection(allSegmentNodes))));


        for (Node childNode : childNodes) {
            Element element = (Element) childNode;
            String childGridId = element.getAttribute(ID);

            PlanTreeNode<?> parameter;
            if (EPOCH.equals(element.getNodeName())) {
                parameter = new Epoch();
            } else if (STUDY_SEGMENT.equals(element.getNodeName())) {
                parameter = new StudySegment();
            } else if  (PERIOD.equals(element.getNodeName())) {
                parameter = new Period();
            } else {
                throw new StudyCalendarError("Cannot find Child Node for: %s", element.getNodeName());
            }

            if (parameter instanceof Named) {
                ((Named) parameter).setName(element.getAttribute(NAME));
            }

            parameter.setGridId(childGridId);
            PlanTreeNode<?> planTreeNode = templateService.findEquivalentChild(study, parameter);

            if (planTreeNode == null) {
                planTreeNode = parameter;
            }

            List<PlanTreeNode<?>> childrenTreeNodes = parsePlanTreeNodes(doc, childGridId, study);
            if (!childrenTreeNodes.isEmpty()) {
                for (PlanTreeNode node : childrenTreeNodes) {
                    cast(planTreeNode).addChild(node);
                }
            }

           planTreeNodes.add(planTreeNode);

        }
        return planTreeNodes;
    }

    /* Class Helpers */
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
    public void setChangeDao(ChangeDao changeDao) {
        this.changeDao = changeDao;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
