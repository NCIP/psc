/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;

import java.util.Date;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class Registration {
    private StudySegment firstStudySegment;
    private Date date;
    private Subject subject;
    private PscUser studySubjectCalendarManager;
    private String desiredStudySubjectAssignmentId;
    private String studySubjectId;
    private Set<Population> populations;

    public StudySegment getFirstStudySegment() {
        return firstStudySegment;
    }

    public void setFirstStudySegment(StudySegment firstStudySegment) {
        this.firstStudySegment = firstStudySegment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public PscUser getStudySubjectCalendarManager() {
        return studySubjectCalendarManager;
    }

    public void setStudySubjectCalendarManager(PscUser studySubjectCalendarManager) {
        this.studySubjectCalendarManager = studySubjectCalendarManager;
    }

    public String getDesiredStudySubjectAssignmentId() {
        return desiredStudySubjectAssignmentId;
    }

    public void setDesiredStudySubjectAssignmentId(String desiredStudySubjectAssignmentId) {
        this.desiredStudySubjectAssignmentId = desiredStudySubjectAssignmentId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Registration)) return false;

        Registration that = (Registration) o;

        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (desiredStudySubjectAssignmentId != null ? !desiredStudySubjectAssignmentId.equals(that.desiredStudySubjectAssignmentId) : that.desiredStudySubjectAssignmentId != null)
            return false;
        if (firstStudySegment != null ? !firstStudySegment.equals(that.firstStudySegment) : that.firstStudySegment != null)
            return false;
        if (populations != null ? !populations.equals(that.populations) : that.populations != null)
            return false;
        if (studySubjectCalendarManager != null ? !studySubjectCalendarManager.equals(that.studySubjectCalendarManager) : that.studySubjectCalendarManager != null)
            return false;
        if (studySubjectId != null ? !studySubjectId.equals(that.studySubjectId) : that.studySubjectId != null)
            return false;
        if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = firstStudySegment != null ? firstStudySegment.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (studySubjectCalendarManager != null ? studySubjectCalendarManager.hashCode() : 0);
        result = 31 * result + (desiredStudySubjectAssignmentId != null ? desiredStudySubjectAssignmentId.hashCode() : 0);
        result = 31 * result + (studySubjectId != null ? studySubjectId.hashCode() : 0);
        result = 31 * result + (populations != null ? populations.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[subject=").append(subject).
            append(" on segment=").append(firstStudySegment).
            append(" as studySubjectId=").append(studySubjectId).
            append(" by manager=").append(studySubjectCalendarManager).
            append(']').toString();
    }

    public static class Builder {
        private Registration target;

        public Builder() {
            this.target = new Registration();
        }

        public Builder date(Date date) {
            this.target.setDate(date);
            return this;
        }

        public Builder subject(Subject subject) {
            this.target.setSubject(subject);
            return this;
        }

        public Builder firstStudySegment(StudySegment segment) {
            this.target.setFirstStudySegment(segment);
            return this;
        }

        public Builder manager(PscUser manager) {
            this.target.setStudySubjectCalendarManager(manager);
            return this;
        }

        public Builder desiredAssignmentId(String id) {
            this.target.setDesiredStudySubjectAssignmentId(id);
            return this;
        }

        public Builder studySubjectId(String id) {
            this.target.setStudySubjectId(id);
            return this;
        }

        public Builder populations(Set<Population> populations) {
            this.target.setPopulations(populations);
            return this;
        }

        public Registration toRegistration() {
            return target;
        }
    }
}
