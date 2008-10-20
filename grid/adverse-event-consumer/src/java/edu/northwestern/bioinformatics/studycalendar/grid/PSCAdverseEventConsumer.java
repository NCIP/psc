/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import gov.nih.nci.cabig.ctms.grid.ae.common.AdverseEventConsumerI;
import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.InvalidRegistration;
import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.RegistrationFailed;
import gov.nih.nci.cabig.ccts.ae.domain.AENotificationType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.wsrf.properties.*;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.Date;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 */

@Transactional(readOnly = false)
public class PSCAdverseEventConsumer implements AdverseEventConsumerI {

    private static final Log logger = LogFactory.getLog(PSCAdverseEventConsumer.class);

    private ScheduledCalendarService scheduledCalendarService;


    public void register(final AENotificationType aeNotification) throws java.rmi.RemoteException, InvalidRegistration, RegistrationFailed {

        // TODO: Change fault implementation to accept a reason message.
        String gridId = aeNotification.getRegistrationGridId();
        if (gridId == null) {
            logger.error("No registrationGridId provided");
            throw new InvalidRegistration();
        }
        String description = aeNotification.getDescription();
        if (description == null) {
            logger.error("No description provided");
            throw new InvalidRegistration();
        }
        Date detectionDate = aeNotification.getDetectionDate();
        if (detectionDate == null) {
            logger.error("No detectionDate provided");
            throw new InvalidRegistration();
        }

        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setGridId(gridId);

        AdverseEvent event = new AdverseEvent();
        event.setDescription(description);
        event.setDetectionDate(detectionDate);

        try {
            scheduledCalendarService.registerSevereAdverseEvent(assignment, event);
        }
        catch (Exception ex) {
            logger.error("Error registering adverse event: " + ex.getMessage(), ex);
            throw new RegistrationFailed();
        }
    }

    @Required
    public void setScheduledCalendarService(final ScheduledCalendarService scheduledCalendarService) {
        this.scheduledCalendarService = scheduledCalendarService;
    }

    public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(final GetMultipleResourceProperties_Element getMultipleResourceProperties_element) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public GetResourcePropertyResponse getResourceProperty(final QName qName) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public QueryResourcePropertiesResponse queryResourceProperties(final QueryResourceProperties_Element queryResourceProperties_element) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
