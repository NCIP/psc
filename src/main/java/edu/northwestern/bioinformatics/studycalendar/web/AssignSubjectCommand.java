package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SITE_COORDINATOR;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.nwu.bioinformatics.commons.spring.Validatable;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.apache.commons.lang.StringUtils;


/**
 * @author Padmaja Vedula
 */
public class AssignSubjectCommand implements Validatable {
    private Subject subject;
    private StudySegment studySegment;
    private Date startDate;

    private Site site;
    private Study study;
    private User subjectCoordinator;
    private Set<Population> populations;

    private SubjectService subjectService;
    private SubjectDao subjectDao;

    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String gender;
    private String personId;

    private static final Logger log = LoggerFactory.getLogger(AssignSubjectCommand.class.getName());

    public AssignSubjectCommand() {
        populations = new HashSet<Population>();
    }


    public void validate(Errors errors){
        if (getPersonId()!= null && getPersonId().length() > 0) {
            Subject subject = subjectService.findSubjectByPersonId(getPersonId());
            if (subject != null) {
                StudySubjectAssignment assignment = subjectDao.getAssignment(subject, getStudy(), getSite());
                if (assignment != null) {
                    errors.rejectValue("personId", "error.person.id.already.exists");
                }
            }
        } else {
            List <Subject> subjects = subjectService.findSubjectByFirstNameLastNameAndDateOfBirth(getFirstName(), getLastName(), getDateOfBirth());
            if (subjects != null && !subjects.isEmpty()) {
                errors.rejectValue("lastName", "error.person.last.name.already.exists");
            }
        }
    }



    public StudySubjectAssignment assignSubject() {
		Subject subject = createSubject();
        StudySubjectAssignment assignment = subjectService.assignSubject(
            subject, getStudySite(), getEffectiveStudySegment(), getStartDate(), getSubjectCoordinator());
        subjectService.updatePopulations(assignment, getPopulations());
        return assignment;
    }


	public Subject createSubject() {
		Subject subject = new Subject();
		subject.setFirstName(getFirstName());
		subject.setLastName(getLastName());
		subject.setDateOfBirth(getDateOfBirth());
		subject.setGender(getGender());
		subject.setPersonId(getPersonId());
        subjectDao.save(subject);
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

    ////// CONFIGURATION

    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    ////// BOUND PROPERTIES

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
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


   //////////////////////////////////////////
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
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

}
