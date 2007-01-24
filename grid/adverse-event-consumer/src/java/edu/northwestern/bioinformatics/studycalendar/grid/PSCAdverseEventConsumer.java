/**
 * 
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import java.util.Date;

import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import gov.nih.nci.cabig.ctms.grid.ae.beans.AENotificationType;
import gov.nih.nci.cabig.ctms.grid.ae.common.AdverseEventConsumer;
import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.InvalidRegistration;
import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.RegistrationFailed;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 * 
 */
public class PSCAdverseEventConsumer implements AdverseEventConsumer {

    private static final Log logger = LogFactory.getLog(PSCAdverseEventConsumer.class);

    public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

    private ApplicationContext ctx;

    public PSCAdverseEventConsumer() {
        this.ctx = new ClassPathXmlApplicationContext(new String[] {
                        "classpath:applicationContext.xml", "classpath:applicationContext-api.xml",
                        "classpath:applicationContext-command.xml", "classpath:applicationContext-dao.xml",
                        "classpath:applicationContext-security.xml",
                        "classpath:applicationContext-service.xml",
                        "classpath:applicationContext-spring.xml" });
    }

    public void register(AENotificationType aeNotification) throws InvalidRegistration,
                    RegistrationFailed {
        openSession();
        
        //TODO: Change fault implementation to accept a reason message.
        String gridId = aeNotification.getRegistrationGridId();
        if(gridId == null){
            logger.error("No registrationGridId provided");
            throw new InvalidRegistration();
        }
        String description = aeNotification.getDescription();
        if(description == null){
            logger.error("No description provided");
            throw new InvalidRegistration();
        }
        Date detectionDate = aeNotification.getDetectionDate();
        if(detectionDate == null){
            logger.error("No detectionDate provided");
            throw new InvalidRegistration();
        }
        
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        assignment.setBigId(gridId);
        
        AdverseEvent event = new AdverseEvent();
        event.setDescription(description);
        event.setDetectionDate(detectionDate);

        
        try{
            ScheduledCalendarService svc = (ScheduledCalendarService)ctx.getBean(SERVICE_BEAN_NAME);
            svc.registerSevereAdverseEvent(assignment, event);
        }catch(Exception ex){
            logger.error("Error registering adverse event: " + ex.getMessage(), ex);
            throw new RegistrationFailed();
        }
        
        closeSession();
    }
    
    private void closeSession() {
        SessionFactory fact = (SessionFactory) this.ctx.getBean("sessionFactory");
        Session session = SessionFactoryUtils.getSession(fact, true);
        TransactionSynchronizationManager.unbindResource(fact);
        SessionFactoryUtils.releaseSession(session, fact);
    }

    private void openSession() {
        SessionFactory fact = (SessionFactory) this.ctx.getBean("sessionFactory");
        Session session = SessionFactoryUtils.getSession(fact, true);
        TransactionSynchronizationManager.bindResource(fact, new SessionHolder(session));
    }

}
