/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

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
    private NowFactory nowFactory;
    private AmendmentService amendmentService;

    public ApproveAmendmentsCommand(
        StudySite studySite, AmendmentService amendmentService, NowFactory nowFactory
    ) {
        this.studySite = studySite;
        this.amendmentService = amendmentService;
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
        List<AmendmentApproval> toApprove = new ArrayList<AmendmentApproval>();
        for (Approval approval : getApprovals()) {
            if (approval.isJustApproved()) {
                toApprove.add(approval.toDomainApproval());
                for (AmendmentApproval amendmentApproval : toApprove) {
                    amendmentApproval.setDate(approval.getDate());
                }
                amendmentService.approve(studySite,
                    toApprove.toArray(new AmendmentApproval[toApprove.size()]));
                toApprove.clear();
            } else if (!approval.isAlreadyApproved()) {
                toApprove.add(approval.toDomainApproval());
            }
        }
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

        /////// LOGIC

        public AmendmentApproval toDomainApproval() {
            return AmendmentApproval.create(getAmendment(), getDate());
        }

        /////// BEAN PROPERTIES

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
