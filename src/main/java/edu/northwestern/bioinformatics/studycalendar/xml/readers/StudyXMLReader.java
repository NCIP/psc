package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.NODE_ID;

import static java.lang.Boolean.valueOf;

import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.PLANNDED_CALENDAR;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ASSIGNED_IDENTIFIER;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ID;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.text.SimpleDateFormat;

import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;

public class StudyXMLReader  {
    private StudyDao studyDao;
    private DeltaDao deltaDao;
    private AmendmentDao amendmentDao;
    private PlannedCalendarDao plannedCalendarDao;
    private ChangeDao changeDao;


    private EpochDao epochDao;
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
                amendment.setName(element.getAttribute(StudyXMLWriter.NAME));
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

        List<Element> deltaNodes = union(allDeltaNodes, allAmendmentChildrenNodes);

        for (Element element : deltaNodes) {
            // Get Delta if Delta Exists
            String deltaGridId = element.getAttribute(ID);
            Delta<?> delta = deltaDao.getByGridId(deltaGridId);

            if (delta == null) {
                // Get Delta Node if Node Exists
                String nodeGridId = element.getAttribute(NODE_ID);
                Node node = doc.getElementById(nodeGridId);

                PlanTreeNode<?> planTreeNode;
                if(PLANNDED_CALENDAR.equals(node.getNodeName())) {
                    planTreeNode  = plannedCalendarDao.getByGridId(nodeGridId);

                    delta = new PlannedCalendarDelta();
                    ((PlannedCalendarDelta)delta).setNode((PlannedCalendar) planTreeNode);
                    //Delta.createDeltaFor(planTreeNode, change);
                } else {
                    throw new StudyCalendarError("Cannot find PlanTreeNode for: %s", node.getNodeName());
                }

                if (planTreeNode == null) {
                    throw new StudyCalendarError("Cannot find Planned Calendar: %s", nodeGridId);
                }

                delta.setRevision(amendment);
                delta.setGridId(deltaGridId);
            }
            List<Change> changes = parseChanges(doc, delta);
            delta.addChanges(changes.toArray(new Change[]{}));

            deltas.add(delta);
        }

        return deltas;
    }

    protected List<Change> parseChanges(Document doc, Delta delta) {
        List<Change> changes = new ArrayList<Change>();

        NodeList allAddNodes = doc.getElementsByTagName(StudyXMLWriter.ADD);
        NodeList allDeltaChildrenNodes = doc.getElementById(delta.getGridId()).getChildNodes();

        List<Element> addNodes = union(allAddNodes, allDeltaChildrenNodes);

        for (Element element : addNodes) {
             // Get Add if Delta Exists
            String addGridId = element.getAttribute(ID);
            Change change = changeDao.getByGridId(addGridId);
            if (change == null) {
                if (StudyXMLWriter.ADD.equals(element.getNodeName())) {
                    change = new Add();
                    ((Add) change).setIndex(new Integer(element.getAttribute(StudyXMLWriter.INDEX)));
                } else {
                    throw new StudyCalendarError("Cannot find Change Node for: %s", element.getNodeName());
                }

                change.setGridId(addGridId);
            }
            changes.add(change);
        }

        return changes;
    }

    /* Class Helpers */

    private List<Element> union(NodeList list0, NodeList list1) {
        List resultList = new ArrayList();
        for (int i = 0; i < list0.getLength(); i++) {
            for (int j = 0; j < list1.getLength(); j++) {
                if (list0.item(i) == list1.item(j)) {
                    resultList.add(list0.item(i));
                }
            }
        }
        return resultList;
    }


    /* Dao Setters */
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
}
