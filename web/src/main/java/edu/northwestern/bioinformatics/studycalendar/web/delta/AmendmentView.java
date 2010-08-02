package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySiteRelationship;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
*/
public class AmendmentView {
    private UserTemplateRelationship rel;
    private Amendment amendment;
    private RevisionChanges revisionChanges;
    private Map<StudySite, ApprovalView> approvals;

    public AmendmentView(PscUser user, Study study, Amendment amendment, DaoFinder daoFinder) {
        this.rel = new UserTemplateRelationship(user, study);
        this.amendment = amendment;
        this.revisionChanges = new RevisionChanges(daoFinder, amendment, study);
        initApprovals();
    }

    private void initApprovals() {
        approvals = new TreeMap<StudySite, ApprovalView>(NamedComparator.INSTANCE);
        if (amendment.equals(rel.getStudy().getDevelopmentAmendment())) return;

        Collection<UserStudySiteRelationship> ssRels = rel.getVisibleStudySites();

        for (UserStudySiteRelationship ussr : ssRels) {
            AmendmentApproval approval = ussr.getStudySite().getAmendmentApproval(amendment);
            Date approvalDate = approval == null ? null : approval.getDate();
            approvals.put(ussr.getStudySite(),
                new ApprovalView(approvalDate, ussr.getCanApproveAmendments()));
        }
    }

    public Amendment getAmendment() {
        return amendment;
    }

    public RevisionChanges getChanges() {
        return revisionChanges;
    }

    public Study getStudy() {
        return rel.getStudy();
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
