package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AmendmentXmlSerializer extends AbstractStudyCalendarXmlSerializer<Amendment> {
    public static final String DATE = "date";
    public static final String AMENDMENT = "amendment";
    public static final String MANDATORY = "mandatory";
    private static final String PREVIOUS_AMENDMENT_KEY = "previous-amendment-key";

    private Study study;
    private AmendmentDao amendmentDao;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public AmendmentXmlSerializer(Study study) {
        this.study = study;
    }

    public Element createElement(Amendment amendment) {
        Element element = element(AMENDMENT);
        element.addAttribute(ID, amendment.getGridId());
        element.addAttribute(NAME, amendment.getName());
        element.addAttribute(MANDATORY, Boolean.toString(amendment.isMandatory()));

        element.addAttribute(DATE, formatter.format(amendment.getDate()));

        if (amendment.getPreviousAmendment() != null) {
            element.addAttribute(PREVIOUS_AMENDMENT_KEY, amendment.getNaturalKey());
        }

        return element;
    }

    public Amendment readElement(Element element) {
        String name = element.attributeValue(NAME);
        Date date;
        try {
            date = formatter.parse(element.attributeValue(DATE));
        } catch (ParseException e) {
            throw new StudyCalendarValidationException("Could not parse date \"%s\", should be in format YYYY-MM-DD", element.attributeValue(DATE));
        }

        Amendment amendment = amendmentDao.getByNaturalKey(new Amendment.Key(date, name).toString());
        if (amendment == null) {
            amendment = new Amendment();
            amendment.setName(name);
            amendment.setDate(date);
            amendment.setMandatory(Boolean.parseBoolean(element.attributeValue(MANDATORY)));
            // TODO: find previous amendment from study and set on amendment
        }
        return amendment;
    }

    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }
}
