package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

import java.text.SimpleDateFormat;

public class AmendmentXmlSerializer extends AbstractStudyCalendarXmlSerializer<Amendment> {
    public static final String AMENDMENT = "amendment";
    private static final String MANDATORY = "mandatory";
    public static final String DATE = "date";
    private AmendmentDao amendmentDao;
    private static final String PREVIOUS_AMENDMENT_ID = "previous-amendment-id";

    public Element createElement(Amendment amendment) {
        Element element = element(AMENDMENT);
        element.addAttribute(ID, amendment.getGridId());
        element.addAttribute(NAME, amendment.getName());
        element.addAttribute(MANDATORY, Boolean.toString(amendment.isMandatory()));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        element.addAttribute(DATE, formatter.format(amendment.getDate()));

        if (amendment.getPreviousAmendment() != null) {
            element.addAttribute(PREVIOUS_AMENDMENT_ID, amendment.getPreviousAmendment().getGridId());
        }

        return element;
    }

    public Amendment readElement(Element element) {
        throw new UnsupportedOperationException();
    }

    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }
}
