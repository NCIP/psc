package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Rhett Sutphin
*/
public class AmendmentView {
    private Study study;
    private Amendment amendment;
    private RevisionChanges revisionChanges;
    private Map<StudySite, ApprovalView> approvals;

    public AmendmentView(User user, Study study, Amendment amendment, DaoFinder daoFinder) {
        this.study = study;
        this.amendment = amendment;
        this.revisionChanges = new RevisionChanges(daoFinder, amendment, study);
        initApprovals(user);
    }

    private void initApprovals(User user) {
        approvals = new TreeMap<StudySite, ApprovalView>(NamedComparator.INSTANCE);
        if (amendment.equals(study.getDevelopmentAmendment())) return;
        Set<StudySite> readableStudySites = findUnapprovableStudySites(user);
        Set<StudySite> approvableStudySites = new HashSet<StudySite>();
        findUnapprovableStudySites(user);

        if (user.hasRole(SITE_COORDINATOR)) {
            UserRole siteCoord = user.getUserRole(SITE_COORDINATOR);
            for (StudySite studySite : study.getStudySites()) {
                if (siteCoord.getSites().contains(studySite.getSite())) {
                    approvableStudySites.add(studySite);
                }
            }
        }
        readableStudySites.addAll(approvableStudySites);

        for (StudySite studySite : readableStudySites) {
            AmendmentApproval approval = studySite.getAmendmentApproval(amendment);
            Date approvalDate = approval == null ? null : approval.getDate();
            approvals.put(studySite,
                new ApprovalView(approvalDate, approvableStudySites.contains(studySite)));
        }
    }

    private Set<StudySite> findUnapprovableStudySites(User user) {
        Set<StudySite> readableStudySites = new HashSet<StudySite>();
        if (user.hasRole(STUDY_ADMIN)) {
            readableStudySites.addAll(study.getStudySites());
        } else if (user.hasRole(SUBJECT_COORDINATOR)) {
            UserRole siteCoord = user.getUserRole(SUBJECT_COORDINATOR);
            for (StudySite studySite : study.getStudySites()) {
                if (siteCoord.getStudySites().contains(studySite)) {
                    readableStudySites.add(studySite);
                }
            }
        }
        return readableStudySites;
    }

    public Amendment getAmendment() {
        return amendment;
    }

    public RevisionChanges getChanges() {
        return revisionChanges;
    }

    public Study getStudy() {
        return study;
    }

    public Map<StudySite, ApprovalView> getApprovals() {
        return approvals;
    }

    ////// INNER CLASSES

    public static class ApprovalView {
        private Date date;
        private boolean approvable;

        public ApprovalView(Date date, boolean approveable) {
            this.date = date;
            this.approvable = approveable;
        }

        public Date getDate() {
            return date;
        }

        public boolean isApprovable() {
            return approvable;
        }
    }
}
