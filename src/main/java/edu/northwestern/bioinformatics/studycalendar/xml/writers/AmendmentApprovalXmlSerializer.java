package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
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
    private Study study;

    private AmendmentDao amendmentDao;

    @Override
    protected XsdElement rootElement() {
        return XsdElement.AMENDMENT_APPROVAL;
    }

    @Override
    protected XsdElement collectionRootElement() {
        return XsdElement.AMENDMENT_APPROVALS;
    }

    @Override
    public Element createElement(AmendmentApproval amendmentApproval, boolean inCollection) {
        if (amendmentApproval == null) {
            throw new StudyCalendarValidationException("amendmentApproval can not be null");
        }
        final Element amendmentApprovalelement = rootElement().create();

        XsdAttribute.AMENDMENT_APPROVAL_DATE.addTo(amendmentApprovalelement, amendmentApproval.getDate());
        XsdAttribute.AMENDMENT_APPROVAL_AMENDMENT.addTo(amendmentApprovalelement, amendmentApproval.getAmendment().getNaturalKey());

        return amendmentApprovalelement;
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
            amendmentApproval.setAmendment(amendmentDao.getByNaturalKey(amendmentIdentifier, study));
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

    public void setStudy(Study study) {
        this.study = study;
    }
}
