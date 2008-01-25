package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        element.addAttribute(NAME, amendment.getName());
        element.addAttribute(DATE, formatter.format(amendment.getDate()));
        element.addAttribute(MANDATORY, Boolean.toString(amendment.isMandatory()));

        if (amendment.getPreviousAmendment() != null) {
            element.addAttribute(PREVIOUS_AMENDMENT_KEY, amendment.getPreviousAmendment().getNaturalKey());
        }

        addDeltas(amendment, element);

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

            String previousAmendmentKey = element.attributeValue(PREVIOUS_AMENDMENT_KEY);
            if (previousAmendmentKey != null) {
                Amendment previousAmendment = findAmendment(study.getAmendmentsList(), previousAmendmentKey);
                amendment.setPreviousAmendment(previousAmendment);
            }

            //TODO: Add Deltas
        }
        return amendment;
    }

    protected Amendment findAmendment(final List<Amendment> amendments, final String amendmentKey) {
        for (Amendment amendment : amendments) {
            if (amendmentKey.equals(amendment.getNaturalKey())) {
                return amendment;
            }
        }
        return null;
    }

    private void addDeltas(final Amendment amendment, Element element) {
        for (Delta delta : amendment.getDeltas()) {
            AbstractDeltaXmlSerializer serializer = findDeltaXmlSerializer(amendment);
            Element eDelta = serializer.createElement(delta);
            element.add(eDelta);
        }
    }
    private AbstractDeltaXmlSerializer findDeltaXmlSerializer(final Amendment amendment) {
        for (Delta delta : amendment.getDeltas()) {
            if (delta instanceof PlannedCalendarDelta) {
                return new PlannedCalendarDeltaXmlSerializer(study);
            } else if (delta instanceof EpochDelta) {
                return new EpochDeltaXmlSerializer(study);
            } else if (delta instanceof StudySegmentDelta) {
                return new StudySegmentDeltaXmlSerializer(study);
            } else if (delta instanceof PeriodDelta) {
                return new PeriodDeltaXmlSerializer(study);
            } else if (delta instanceof PlannedActivityDelta) {
                return new PlannedActivityDeltaXmlSerializer(study);
            }
        }
        throw new StudyCalendarError("Could not find delta type");
    }

    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }
}
