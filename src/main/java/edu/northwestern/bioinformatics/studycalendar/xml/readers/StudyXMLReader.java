package edu.northwestern.bioinformatics.studycalendar.xml.readers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ASSIGNED_IDENTIFIER;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.ID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class StudyXMLReader  {
    private StudyDao studyDao;

    public StudyXMLReader(StudyDao studyDao) {
        this.studyDao = studyDao;
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

        study.setAssignedIdentifier(element.getAttribute(ASSIGNED_IDENTIFIER));

        return study;
    }

    protected PlannedCalendar parsePlannedCalendar(Document dom) {
        return null;
    }


}
