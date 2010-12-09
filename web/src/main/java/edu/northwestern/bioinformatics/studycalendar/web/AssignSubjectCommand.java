package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.Registration;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySiteRelationship;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedCommand;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isEmpty;


/**
 * @author Padmaja Vedula
 */
public class AssignSubjectCommand implements Validatable, PscAuthorizedCommand {
    private StudySegment studySegment;
    private String startDate;

    private Site site;
    private Study study;
    private PscUser studySubjectCalendarManager;
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

    public Collection<ResourceAuthorization> authorizations(Errors bindErrors) {
        return ResourceAuthorization.createCollection(site, study, PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    public void validate(Errors errors){
        Subject subject;
        if (getRadioButton() == null) {
           errors.rejectValue("personId", "error.subject.please.select.a.subject");
        }
        else {
            if (useExistingSubject()){
                if (getIdentifier() == null || getIdentifier().trim().length()==0){
                    errors.rejectValue("personId", "error.subject.please.select.a.subject");
                } else if (getStartDate() == null || getStartDate().length() != 10 || convertStringToDate(getStartDate()) == null) {
                    errors.rejectValue("startDate", "error.subject.assignment.please.enter.a.start.date");
                } else {
                    subject = subjectDao.findSubjectByGridOrPersonId(getIdentifier());
                }
            } else if (creatingNewSubject()){
                if (isEmpty(getPersonId()) && (isEmpty(getFirstName()) || isEmpty(getLastName()) || isEmpty(getDateOfBirth()))) {
                    errors.rejectValue("personId", "error.subject.assignment.please.enter.person.id.and.or.first.last.birthdate");
                } else if (isEmpty(getDateOfBirth()) || getDateOfBirth() == null || convertStringToDate(getDateOfBirth()) == null) {
                        errors.rejectValue("dateOfBirth", "error.subject.assignment.please.enter.date.of.birth");
                } else if (isEmpty(getStartDate()) || getStartDate() == null || convertStringToDate(getStartDate()) == null) {
                        errors.rejectValue("startDate", "error.subject.assignment.please.enter.a.start.date");
                }
                if (!errors.hasErrors()) {
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

    private boolean creatingNewSubject() {
        return getRadioButton().equals("new");
    }

    private boolean useExistingSubject() {
        return getRadioButton().equals("existing");
    }

    public StudySubjectAssignment assignSubject() {
        Subject subject = createAndSaveNewOrExtractExistingSubject();
        StudySubjectAssignment assignment = subjectService.assignSubject(
            getStudySite(),
            new Registration.Builder().
                subject(subject).
                firstStudySegment(getEffectiveStudySegment()).
                date(convertStringToDate(getStartDate())).
                studySubjectId(getStudySubjectId()).
                populations(getPopulations()).
                manager(getStudySubjectCalendarManager()).
                toRegistration());
        subjectService.updatePopulations(assignment, getPopulations());
        return assignment;
    }

    private Subject createSubject() {
        Subject subject = new Subject();
        subject.setFirstName(getFirstName());
        subject.setLastName(getLastName());
        subject.setDateOfBirth(convertStringToDate(getDateOfBirth()));
        subject.setGender(Gender.getByCode(getGender()));
        subject.setPersonId(getPersonId());
        return subject;
    }

    private Subject createAndSaveNewOrExtractExistingSubject() {
        Subject subject;
        if (creatingNewSubject()) {
            if (canCreateSubjects()) {
               subject = createSubject();
               subjectDao.save(subject);
            } else {
                 throw new StudyCalendarSystemException(
                "%s has insufficient privilege to create new subject.", getStudySubjectCalendarManager());
            }
        } else {
            subject = subjectDao.findSubjectByGridOrPersonId(getIdentifier());
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

    public StudySite getStudySite() {
        return StudySite.findStudySite(getStudy(), getSite());
    }

    private Date convertStringToDate(String dateString) {
        Date convertedDate;
        try {
            convertedDate = FormatTools.getLocal().getDateFormat().parse(dateString);
        } catch (ParseException e) {
            convertedDate = null;

        }
        return convertedDate;
    }

    private boolean canCreateSubjects() {
        UserStudySiteRelationship ussr =
                new UserStudySiteRelationship(getStudySubjectCalendarManager(), getStudySite());
        return ussr.getCanCreateSubjects();
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

    public PscUser getStudySubjectCalendarManager() {
        return studySubjectCalendarManager;
    }

    public void setStudySubjectCalendarManager(PscUser studySubjectCalendarManager) {
        this.studySubjectCalendarManager = studySubjectCalendarManager;
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
