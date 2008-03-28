package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class  AmendmentXmlSerializer extends AbstractStudyCalendarXmlSerializer<Amendment> {
    public static final String DATE = "date";

    public static final String MANDATORY = "mandatory";
    private static final String PREVIOUS_AMENDMENT_KEY = "previous-amendment-key";

    private Study study;
    private AmendmentDao amendmentDao;

    private boolean isDevelopmentAmendment = false;

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public Element createElement(Amendment amendment) {
        Element element = null;
        if (!isDevelopmentAmendment) {
            element = XsdElement.AMENDMENT.create();
        } else {
            element = XsdElement.DEVELOPMENT_AMENDMENT.create();
        }
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

        Amendment amendment = amendmentDao.getByNaturalKey(new Amendment.Key(date, name).toString(), study);
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

            addDeltas(element, amendment);
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
            AbstractDeltaXmlSerializer serializer = getDeltaXmlSerializerFactory().createXmlSerializer(delta);
            Element eDelta = serializer.createElement(delta);
            element.add(eDelta);
        }
    }


    private void addDeltas(final Element element, Amendment amendment) {
        for (Object oDelta : element.elements()) {
            Element eDelta = (Element) oDelta;
            AbstractDeltaXmlSerializer serializer = getDeltaXmlSerializerFactory().createXmlSerializer(eDelta);
            Delta delta = serializer.readElement(eDelta);
            amendment.addDelta(delta);
        }
    }


    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    public DeltaXmlSerializerFactory getDeltaXmlSerializerFactory() {
        DeltaXmlSerializerFactory factory = (DeltaXmlSerializerFactory) getBeanFactory().getBean("deltaXmlSerializerFactory");
        factory.setStudy(study);
        return factory;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public boolean isDevelopmentAmendment() {
        return isDevelopmentAmendment;
    }

    public void setDevelopmentAmendment(final boolean developmentAmendment) {
        isDevelopmentAmendment = developmentAmendment;
    }
}
