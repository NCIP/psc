/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

import java.util.Date;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "amendment_approvals")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_amendment_approvals_id")
    }
)
public class AmendmentApproval extends AbstractMutableDomainObject {
    private Amendment amendment;
    private StudySite studySite;
    private Date date;

    ////// FACTORIES

    public static AmendmentApproval create(Amendment amendment, Date approvalDate) {
        return create(null, amendment, approvalDate);
    }

    public static AmendmentApproval create(StudySite studySite, Amendment amendment, Date approvalDate) {
        AmendmentApproval approval = new AmendmentApproval();
        approval.setAmendment(amendment);
        approval.setStudySite(studySite);
        approval.setDate(approvalDate);
        return approval;
    }

    ////// BEAN PROPERTIES

    @ManyToOne
    public Amendment getAmendment() {
        return amendment;
    }

    public void setAmendment(Amendment amendment) {
        this.amendment = amendment;
    }

    @ManyToOne
    public StudySite getStudySite() {
        return studySite;
    }

    public void setStudySite(StudySite studySite) {
        this.studySite = studySite;
    }

    @Column(name = "approval_date")
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    ////// OBJECT METHODS

    public String toString() {
        return new StringBuilder().append(getClass().getSimpleName())
            .append("[date=").append(getDate())
            .append("; amendment=").append(getAmendment())
            .append(']').toString();
    }
}
