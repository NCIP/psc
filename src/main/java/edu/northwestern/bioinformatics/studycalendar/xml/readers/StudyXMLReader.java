package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.PLANNDED_CALENDAR;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ASSIGNED_IDENTIFIER;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ID;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class StudyXMLReader  {
    private StudyDao studyDao;
    private PlannedCalendarDao plannedCalendarDao;

    public StudyXMLReader(StudyDao studyDao, PlannedCalendarDao plannedCalendarDao) {
        this.studyDao = studyDao;
        this.plannedCalendarDao = plannedCalendarDao;
    }

    public Study read(InputStream dataFile) throws Exception {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        //Using factory get an instance of document builder
        DocumentBuilder db = dbf.newDocumentBuilder();

        //parse using builder to get DOM representation of the XML file
        Document dom = db.parse(dataFile);

        return parseStudy(dom);
    }

    protected Study parseStudy(Document doc) throws SAXException {
        Element element = doc.getDocumentElement();
        String gridId = element.getAttribute(ID);
        Study study = studyDao.getByGridId(gridId);
        if (study == null) {
            study = new Study();
            study.setGridId(gridId);
        }
        //TODO: Use amendments to figure out which study is newer and use that assigned identifier
        study.setAssignedIdentifier(element.getAttribute(ASSIGNED_IDENTIFIER));
                                Node e = element.getFirstChild();
        return study;
    }

    protected PlannedCalendar parsePlannedCalendar(Document doc) {
        NodeList nodes = doc.getElementsByTagName(PLANNDED_CALENDAR);

        Element element = ((Element)nodes.item(0));
        String gridId = element.getAttribute(ID);
        PlannedCalendar calendar = plannedCalendarDao.getByGridId(gridId);
        if (calendar == null) {
            calendar = new PlannedCalendar();
            calendar.setGridId(gridId);
        }

        return calendar;
    }


}
