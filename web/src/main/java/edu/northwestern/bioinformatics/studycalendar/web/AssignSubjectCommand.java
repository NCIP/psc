package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import static org.apache.commons.lang.StringUtils.isEmpty;
import org.springframework.validation.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.text.ParseException;


/**
 * @author Padmaja Vedula
 */
public class AssignSubjectCommand implements Validatable {
    private StudySegment studySegment;
    private String startDate;

    private Site site;
    private Study study;
    private User subjectCoordinator;
    private Set<Population> populations;

    private SubjectService subjectService;
    private SubjectDao subjectDao;

    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String personId;
    private String studySubjectId;
    private String identifier;
    private String radioButton;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public AssignSubjectCommand() {
        populations = new HashSet<Population>();
    }


    public void validate(Errors errors){
        Subject subject = null;
        if (getRadioButton() == null) {
           errors.rejectValue("personId", "error.subject.please.select.a.subject");
        }
        else {
            if (getRadioButton().equals("existing")){
                if (getIdentifier() == null || getIdentifier().trim().length()==0){
                    errors.rejectValue("personId", "error.subject.please.select.a.subject");
                } else if (getStartDate() == null || getStartDate().length() != 10 || convertStringToDate(getStartDate()) == null) {
                    errors.rejectValue("startDate", "error.subject.assignment.please.enter.a.start.date");
                } else {
                    subject = subjectDao.findSubjectByGridOrPersonId(getIdentifier());
                }
            } else if (getRadioButton().equals("new")){
                if (isEmpty(getPersonId()) && (isEmpty(getFirstName()) || isEmpty(getLastName()) || getDateOfBirth() == null)) {
                    errors.rejectValue("personId", "error.subject.assignment.please.enter.person.id.and.or.first.last.birthdate");
                } else if (getDateOfBirth() == null || getDateOfBirth().length() != 10 || convertStringToDate(getDateOfBirth()) == null) {
                    errors.rejectValue("dateOfBirth", "error.subject.assignment.please.enter.date.of.birth");
                } else if (getStartDate() == null || getStartDate().length() != 10 || convertStringToDate(getStartDate()) == null) {
                    errors.rejectValue("startDate", "error.subject.assignment.please.enter.a.start.date");
                } else {
                    subject = createSubject();
                    List<Subject> results = subjectService.findSubjects(subject);
                    if (results.size() > 1) {
                        if (subject.getPersonId() != null) {
                            errors.rejectValue("personId", "error.person.id.already.exists");
                        } else {
                            errors.rejectValue("lastName", "error.person.last.name.already.exists");
                        }
                    }
                }
            }
        }
    }


    public StudySubjectAssignment assignSubject() {
		Subject subject = createAndSaveNewOrExtractExistingSubject();
        StudySubjectAssignment assignment = subjectService.assignSubject(
            subject, getStudySite(), getEffectiveStudySegment(), convertStringToDate(getStartDate()), getStudySubjectId(), getSubjectCoordinator(), getPopulations());
        subjectService.updatePopulations(assignment, getPopulations());
        return assignment;
    }

    public Subject createSubject() {
        Subject subject = new Subject();
		subject.setFirstName(getFirstName());
		subject.setLastName(getLastName());
		subject.setDateOfBirth(convertStringToDate(getDateOfBirth()));
		subject.setGender(Gender.getByCode(getGender()));
		subject.setPersonId(getPersonId());
        return subject;
    }

    public Subject createAndSaveNewOrExtractExistingSubject() {
		Subject subject = null;
        if (getRadioButton().equals("existing")){
            subject = subjectDao.findSubjectByGridOrPersonId(getIdentifier());
        } else {
            subject = createSubject();
            subjectDao.save(subject);
        }
        return subject;
    }

    private StudySegment getEffectiveStudySegment() {
        StudySegment effectiveStudySegment = getStudySegment();
        if (effectiveStudySegment == null) {
            effectiveStudySegment = getStudySite().getStudy().getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        }
        return effectiveStudySegment;
    }

    private StudySite getStudySite() {
        return StudySite.findStudySite(getStudy(), getSite());
    }

    public Date convertStringToDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date convertedDate = null;

        try {
            convertedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            convertedDate = null;

        }
        return convertedDate;
    }

    ////// CONFIGURATION

    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    ////// BOUND PROPERTIES

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public StudySegment getStudySegment() {
        return studySegment;
    }

    public void setStudySegment(StudySegment studySegment) {
        this.studySegment = studySegment;
    }

    public User getSubjectCoordinator() {
        return subjectCoordinator;
    }

    public void setSubjectCoordinator(User subjectCoordinator) {
        this.subjectCoordinator = subjectCoordinator;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public Set<Population> getPopulations() {
        return populations;
    }

    public void setPopulations(Set<Population> populations) {
        this.populations = populations;
    }

    public String getStudySubjectId() {
        return studySubjectId;
    }

    public void setStudySubjectId(String studySubjectId) {
        this.studySubjectId = studySubjectId;
    }

   //////////////////////////////////////////
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getRadioButton() {
        return radioButton;
    }

    public void setRadioButton(String radioButton) {
        this.radioButton = radioButton;
    }
}
