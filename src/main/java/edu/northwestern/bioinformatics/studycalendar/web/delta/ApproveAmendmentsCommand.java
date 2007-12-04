package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import gov.nih.nci.cabig.ctms.lang.NowFactory;

/**
 * @author Rhett Sutphin
 */
public class ApproveAmendmentsCommand {
    private StudySite studySite;
    private List<Approval> approvals;
    private StudySiteDao studySiteDao;
    private NowFactory nowFactory;

    public ApproveAmendmentsCommand(
        StudySite studySite, StudySiteDao studySiteDao, NowFactory nowFactory
    ) {
        this.studySite = studySite;
        this.studySiteDao = studySiteDao;
        this.nowFactory = nowFactory;
        approvals = new ArrayList<Approval>();
        buildSubcommands();
    }

    ////// LOGIC

    private void buildSubcommands() {
        Amendment current = studySite.getStudy().getAmendment();
        while (current != null) {
            Approval approval = new Approval(current);
            AmendmentApproval currentApproval = studySite.getAmendmentApproval(current);
            if (currentApproval == null) {
                approval.setDate(nowFactory.getNow());
                approval.setAlreadyApproved(false);
            } else {
                approval.setDate(currentApproval.getDate());
                approval.setAlreadyApproved(true);
            }
            approvals.add(0, approval);
            current = current.getPreviousAmendment();
        }
    }

    public void apply() {
        for (Approval approval : approvals) {
            if (approval.isJustApproved()) {
                studySite.approveAmendment(approval.getAmendment(), approval.getDate());
            }
        }
        studySiteDao.save(studySite);
    }

    public StudySite getStudySite() {
        return studySite;
    }

    ////// BOUND PROPERTIES

    public List<Approval> getApprovals() {
        return approvals;
    }

    ////// INNER CLASSES

    public static class Approval {
        private boolean alreadyApproved;
        private boolean justApproved;
        private Date date;
        private Amendment amendment;

        public Approval(Amendment amendment) {
            this.amendment = amendment;
        }

        public boolean isAlreadyApproved() {
            return alreadyApproved;
        }

        public void setAlreadyApproved(boolean alreadyApproved) {
            this.alreadyApproved = alreadyApproved;
        }

        public boolean isJustApproved() {
            return justApproved;
        }

        public void setJustApproved(boolean justApproved) {
            this.justApproved = justApproved;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Amendment getAmendment() {
            return amendment;
        }

        public void setAmendment(Amendment amendment) {
            this.amendment = amendment;
        }
    }
}
