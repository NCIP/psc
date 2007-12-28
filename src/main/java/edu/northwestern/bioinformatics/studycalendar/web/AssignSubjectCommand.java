package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Padmaja Vedula
 */
public class AssignSubjectCommand {
    private Subject subject;
    private StudySegment studySegment;
    private Date startDate;
    private Site site;
    private Study study;
    private User subjectCoordinator;
    private Set<Population> populations;

    private SubjectService subjectService;

    public AssignSubjectCommand() {
        populations = new HashSet<Population>();
    }

    public StudySubjectAssignment assignSubject() {
        StudySubjectAssignment assignment = subjectService.assignSubject(
            getSubject(), getStudySite(), getEffectiveStudySegment(), getStartDate(), getSubjectCoordinator());
        subjectService.updatePopulations(assignment, getPopulations());
        return assignment;
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
}
