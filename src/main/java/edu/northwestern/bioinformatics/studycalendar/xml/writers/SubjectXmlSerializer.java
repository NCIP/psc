package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SUBJECT;
import org.dom4j.Element;
import static org.springframework.util.StringUtils.capitalize;

import java.util.Date;
import java.util.List;

/**
 * @author John Dzak
 */
public class SubjectXmlSerializer extends AbstractStudyCalendarXmlSerializer<Subject> {
    private SubjectDao subjectDao;

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

        validateAttributes(personId, firstName, lastName, birthDate, gender);

        if (personId != null) {
            Subject subject = subjectDao.findSubjectByPersonId(personId);
            if (subject == null) {
                subject = createSubject(personId, firstName,  lastName, birthDate, gender);
            }
            return subject;
        } else {
            List<Subject> subjects = subjectDao.findSubjectByFirstNameLastNameAndDoB(firstName, lastName, birthDate);

            if (subjects.isEmpty()) {
                return createSubject(personId, firstName,  lastName, birthDate, gender);
            } else if (subjects.size() == 1) {
                return subjects.get(0);
            } else {
                throw new StudyCalendarValidationException(
                        "Multiple subjects found for %s, %s, and %s.",
                        lastName, firstName, SUBJECT_BIRTH_DATE.from(element));
            }
        }
    }

    private void validateAttributes(String personId, String firstName, String lastName, Date birthDate, String gender) {
        if (personId == null) {
            if (firstName == null) {
                throw new StudyCalendarValidationException(
                        "Subject first name is required if person id is empty");
            } else if (lastName == null) {
                throw new StudyCalendarValidationException(
                        "Subject last name is required if person id is empty");
            } else if (birthDate == null) {
                throw new StudyCalendarValidationException(
                        "Subject birth date is required if person id is empty");
            }
        }
        if (gender == null) {
            throw new StudyCalendarValidationException(
                    "Subject gender is required");
        }
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

    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }
}
