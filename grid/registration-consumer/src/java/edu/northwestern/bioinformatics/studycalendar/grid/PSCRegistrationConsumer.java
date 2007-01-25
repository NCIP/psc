/**
 * 
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.common.RegistrationConsumer;
import gov.nih.nci.cabig.ctms.grid.IdentifierType;
import gov.nih.nci.cabig.ctms.grid.ParticipantType;
import gov.nih.nci.cabig.ctms.grid.RegistrationType;
import gov.nih.nci.cabig.ctms.stubs.types.InvalidRegistration;
import gov.nih.nci.cabig.ctms.stubs.types.RegistrationFailed;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.rmi.RemoteException;
import java.util.Date;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 * 
 */
public class PSCRegistrationConsumer implements RegistrationConsumer {

    private static final Log logger = LogFactory.getLog(PSCRegistrationConsumer.class);

    public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

    private static final String MRN_IDENTIFIER_TYPE = "MRN";

    private ApplicationContext ctx;

    public PSCRegistrationConsumer() {
        this.ctx = new ClassPathXmlApplicationContext(new String[] {
                "classpath:applicationContext.xml", "classpath:applicationContext-api.xml",
                "classpath:applicationContext-command.xml", "classpath:applicationContext-dao.xml",
                "classpath:applicationContext-security.xml",
                "classpath:applicationContext-service.xml",
                "classpath:applicationContext-spring.xml" });
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nih.nci.cabig.ctms.common.RegistrationConsumer#createRegistration(gov.nih.nci.cabig.ctms.grid.RegistrationType)
     */
    public void register(RegistrationType registration) throws RemoteException,
                    InvalidRegistration, RegistrationFailed {

        Study study = new Study();
        Participant participant = new Participant();
        Site site = new Site();
        ScheduledCalendarService svc = (ScheduledCalendarService) this.ctx
                        .getBean(SERVICE_BEAN_NAME);

        site.setBigId(registration.getHealthCareSiteGridId());
        study.setBigId(registration.getStudyGridId());

        ParticipantType partBean = registration.getParticipant();
        participant.setBigId(partBean.getParticipantGridId());

        participant.setGender(partBean.getAdministrativeGenderCode());
        participant.setDateOfBirth(partBean.getBirthDate());
        participant.setFirstName(partBean.getFirstName());
        participant.setLastName(partBean.getLastName());

        String mrn = findIdentifierValue(partBean.getIdentifier(), MRN_IDENTIFIER_TYPE);
        participant.setPersonId(mrn);

        ScheduledCalendar scheduledCalendar = svc.assignParticipant(study, participant, site, null,
                        new Date());
        logger.debug("Created assignment " + scheduledCalendar.getId());
    }

    private String findIdentifierValue(IdentifierType[] idents, String desiredType) throws InvalidRegistration {
        String value = null;
        if (idents != null) {
            for (IdentifierType ident : idents) {
                if (ident.getType().equals(desiredType)) {
                    value = ident.getValue();
                    break;
                }
            }
        }
        if (value == null) {
            throw new InvalidRegistration();
        }
        return value;
    }

    public ServiceSecurityMetadata getServiceSecurityMetadata() throws RemoteException {
        throw new UnsupportedOperationException("Not implemented");
    }

}
