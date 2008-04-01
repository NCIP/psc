/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import gov.nih.nci.cabig.ctms.grid.ae.beans.AENotificationType;
import gov.nih.nci.cabig.ctms.grid.ae.common.AdverseEventConsumer;
import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.InvalidRegistration;
import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.RegistrationFailed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 */

@Transactional(readOnly = true)
public class PSCAdverseEventConsumer implements AdverseEventConsumer {

    private static final Log logger = LogFactory.getLog(PSCAdverseEventConsumer.class);

    private ScheduledCalendarService scheduledCalendarService;


    public PSCAdverseEventConsumer() {
//		ctx = new ClassPathXmlApplicationContext(new String[] {
//				// "classpath:applicationContext.xml",
//				"classpath:applicationContext-api.xml", "classpath:applicationContext-command.xml",
//				"classpath:applicationContext-dao.xml", "classpath:applicationContext-db.xml",
//				"classpath:applicationContext-security.xml", "classpath:applicationContext-service.xml",
//				"classpath:applicationContext-spring.xml" });

    }

    public void register(final AENotificationType aeNotification) throws InvalidRegistration, RegistrationFailed {

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
}
