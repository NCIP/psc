package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;

import java.util.Date;


/**
 * @author Padmaja Vedula
 */
public class AssignSubjectCommand {
    private Subject subject;
    private Arm arm;
    private Date startDate;
    private Site site;
    private Study study;
    private User subjectCoordinator;

    private SubjectService subjectService;

    public StudySubjectAssignment assignSubject() {
        return subjectService.assignSubject(
            getSubject(), getStudySite(), getEffectiveArm(), getStartDate(), getSubjectCoordinator());
    }

    private Arm getEffectiveArm() {
        Arm effectiveArm = getArm();
        if (effectiveArm == null) {
            effectiveArm = getStudySite().getStudy().getPlannedCalendar().getEpochs().get(0).getArms().get(0);
        }
        return effectiveArm;
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

    public Arm getArm() {
        return arm;
    }

    public void setArm(Arm arm) {
        this.arm = arm;
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
}
