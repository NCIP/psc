package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;

import java.util.ArrayList;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;

public class AssignSubjectToSubjectCoordinatorByUserCommand {
    private SubjectDao subjectDao;
    private List<Subject> subjects = new ArrayList<Subject>();
    private Study study;
    private Site site;
    private String selected;
    private final String UNASSIGNED = "unassigned";
    private Integer managerCsmUserId;

    public void assignSubjectsToSubjectCoordinator() {
        StudySite studySite = findStudySite(study, site);
        for (Subject subject : subjects) {
            List<StudySubjectAssignment> assignments = subject.getAssignments();
            for (StudySubjectAssignment assignment : assignments) {
                if (studySite.equals(assignment.getStudySite())) {
                    assignment.setManagerCsmUserId(managerCsmUserId);
                    subjectDao.save(subject);
                }
            }
        }
    }

    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public void setManagerCsmUserId(Integer managerCsmUserId) {
        this.managerCsmUserId = managerCsmUserId;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public Study getStudy() {
        return study;
    }

    public Site getSite() {
        return site;
    }

    public String getSelected() {
        return selected;
    }

    public boolean isUnassigned(String subjectCoordinatorIdString){
        return subjectCoordinatorIdString.equals(UNASSIGNED);
    }
}
