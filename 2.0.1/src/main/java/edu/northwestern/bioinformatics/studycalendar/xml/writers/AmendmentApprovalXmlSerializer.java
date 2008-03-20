package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

import java.util.Date;

/**
 * @author Saurabh Agrawal
 */
public class AmendmentApprovalXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<AmendmentApproval> {

    protected XsdElement rootElement() {
        return XsdElement.AMENDMENT_APPROVAL;
    }

    protected XsdElement collectionRootElement() {
        return XsdElement.AMENDMENT_APPROVALS;
    }

    private AmendmentDao amendmentDao;



    @Override
    public Element createElement(AmendmentApproval amendmentApproval, boolean inCollection) {
        if (amendmentApproval == null) {
            throw new StudyCalendarValidationException("amendmentApproval can not be null");
        }
        final Element amendmentApprovalelement = rootElement().create();

        XsdAttribute.AMENDMENT_APPROVAL_DATE.addTo(amendmentApprovalelement, amendmentApproval.getDate());
        XsdAttribute.AMENDMENT_APPROVAL_AMENDMENT.addTo(amendmentApprovalelement, amendmentApproval.getAmendment().getNaturalKey());

        if (inCollection) {
            return amendmentApprovalelement;
        } else {
            Element root = collectionRootElement().create();
            root.add(amendmentApprovalelement);
            return root;
        }

    }

    @Override
    public AmendmentApproval readElement(Element element) {

        if (element == null) {
            throw new StudyCalendarValidationException("element can not be null");
        }

        final String amendmentIdentifier = XsdAttribute.AMENDMENT_APPROVAL_AMENDMENT.from(element);

        if (amendmentIdentifier!= null) {

            Date date = XsdAttribute.AMENDMENT_APPROVAL_DATE.fromDate(element);
            AmendmentApproval amendmentApproval = new AmendmentApproval();
            amendmentApproval.setAmendment(amendmentDao.getByNaturalKey(amendmentIdentifier));
            amendmentApproval.setDate(date);

            return amendmentApproval;

        } else {
            throw new StudyCalendarValidationException("amendment element can not be null");

        }
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }
}
