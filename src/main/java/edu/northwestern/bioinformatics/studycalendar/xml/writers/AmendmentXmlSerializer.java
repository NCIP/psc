package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AmendmentXmlSerializer extends AbstractStudyCalendarXmlSerializer<Amendment> {
    public static final String AMENDMENT = "amendment";
    public static final String MANDATORY = "mandatory";
    public static final String DATE = "date";

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private AmendmentDao amendmentDao;
    private static final String PREVIOUS_AMENDMENT_KEY = "previous-amendment-key";

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
        }
        return amendment;
    }

    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }
}
