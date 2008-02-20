package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SUBJECT;
import org.dom4j.Element;
import static org.springframework.util.StringUtils.capitalize;

import java.util.Date;

/**
 * @author John Dzak
 */
public class SubjectXmlSerializer extends AbstractStudyCalendarXmlSerializer<Subject> {
    private SubjectService subjectService;

    public Element createElement(Subject subject) {
        Element elt = SUBJECT.create();
        SUBJECT_FIRST_NM.addTo(elt, subject.getFirstName());
        SUBJECT_LAST_NM.addTo(elt, subject.getLastName());
        SUBJECT_PERSON_ID.addTo(elt, subject.getPersonId());
        SUBJECT_BIRTH_DATE.addTo(elt, subject.getDateOfBirth());
        SUBJECT_GENDER.addTo(elt, subject.getGender());
        return elt;
    }

    public Subject readElement(Element element) {
        String personId  = SUBJECT_PERSON_ID.from(element);
        String firstName = SUBJECT_FIRST_NM.from(element);
        String lastName  = SUBJECT_LAST_NM.from(element);
        Date birthDate   = SUBJECT_BIRTH_DATE.fromDate(element);
        String gender    = capitalize(SUBJECT_GENDER.from(element));

        Subject searchCriteria = createSubject(personId, firstName,  lastName, birthDate, gender);
        Subject searchResult = subjectService.findSubject(searchCriteria);

        return (searchResult != null) ? searchResult : searchCriteria;

    }



    private Subject createSubject(String personId, String firstName, String lastName, Date birthDate, String gender) {
        Subject subject = new Subject();
        subject.setPersonId(personId);
        subject.setFirstName(firstName);
        subject.setLastName(lastName);
        subject.setDateOfBirth(birthDate);
        subject.setGender(gender);
        return subject;
    }

    ////// Bean Setters
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }
}
