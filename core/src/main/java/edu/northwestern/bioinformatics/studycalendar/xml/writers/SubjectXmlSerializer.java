package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.SubjectProperty;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.*;

/**
 * @author John Dzak
 */
public class SubjectXmlSerializer extends AbstractStudyCalendarXmlSerializer<Subject> {
    @Override
    public Element createElement(Subject subject) {
        Element elt = SUBJECT.create();
        SUBJECT_FIRST_NAME.addTo(elt, subject.getFirstName());
        SUBJECT_LAST_NAME.addTo(elt, subject.getLastName());
        SUBJECT_PERSON_ID.addTo(elt, subject.getPersonId());
        SUBJECT_BIRTH_DATE.addTo(elt, subject.getDateOfBirth());
        SUBJECT_GENDER.addTo(elt, subject.getGender().getCode());
        for (SubjectProperty property : subject.getProperties()) {
            Element child = SUBJECT_PROPERTY.create();
            PROPERTY_NAME.addTo(child, property.getName());
            PROPERTY_VALUE.addTo(child, property.getValue());
            elt.add(child);
        }
        return elt;
    }

    @Override
    @SuppressWarnings( { "unchecked" })
    public Subject readElement(Element element) {
        Subject subject = new Subject();
        subject.setPersonId(SUBJECT_PERSON_ID.from(element));
        subject.setFirstName(SUBJECT_FIRST_NAME.from(element));
        subject.setLastName(SUBJECT_LAST_NAME.from(element));
        subject.setDateOfBirth(SUBJECT_BIRTH_DATE.fromDate(element));
        subject.setGender(Gender.getByCode(SUBJECT_GENDER.from(element)));

        for (Element child : ((List<Element>) element.elements(SUBJECT_PROPERTY.xmlName()))) {
            subject.getProperties().add(
                new SubjectProperty(PROPERTY_NAME.from(child), PROPERTY_VALUE.from(child)));
        }

        return subject;
    }
}
