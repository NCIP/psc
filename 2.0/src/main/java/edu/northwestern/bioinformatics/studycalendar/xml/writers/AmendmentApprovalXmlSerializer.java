package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

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

    private StudySiteXmlSerializer studySiteXmlSerializer;

    private AmendmentXmlSerializer amendmentSerializer;

    @Override
    public Element createElement(AmendmentApproval amendmentApproval, boolean inCollection) {

        if (amendmentApproval == null) {
            throw new StudyCalendarValidationException("amendmentApproval can not be null");

        }

        final Element amendmentApprovalelement = rootElement().create();

        final Element studySiteElement = studySiteXmlSerializer.createElement(amendmentApproval.getStudySite());
        amendmentApprovalelement.add(studySiteElement);

        final Element amendmentElement = amendmentSerializer.createElement(amendmentApproval.getAmendment());
        amendmentApprovalelement.add(amendmentElement);

        XsdAttribute.AMENDMENT_APPROVAL_DATE.addTo(amendmentApprovalelement, amendmentApproval.getDate());

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

        final Element studySiteElement = element.element(XsdElement.STUDY_SITE_LINK.xmlName());
        final Element amendmentElement = element.element(XsdElement.AMENDMENT.xmlName());
        StudySite studySite;
        if (studySiteElement != null && amendmentElement != null) {
            studySite = studySiteXmlSerializer.readElement(studySiteElement);
            Amendment amendment = amendmentSerializer.readElement(amendmentElement);
            Date date = XsdAttribute.AMENDMENT_APPROVAL_DATE.fromDate(element);

            AmendmentApproval amendmentApproval = new AmendmentApproval();
            amendmentApproval.setStudySite(studySite);
            amendmentApproval.setAmendment(amendment);
            amendmentApproval.setDate(date);

            return amendmentApproval;

        } else {
            throw new StudyCalendarValidationException("study site element  or amendment element can not be null");

        }


    }


    public void setAmendmentSerializer(final AmendmentXmlSerializer amendmentSerializer) {
        this.amendmentSerializer = amendmentSerializer;
    }

    public void setStudySiteXmlSerializer(final StudySiteXmlSerializer studySiteXmlSerializer) {
        this.studySiteXmlSerializer = studySiteXmlSerializer;
    }
}
