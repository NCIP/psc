/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import gov.nih.nci.cabig.ctms.grid.ae.common.AdverseEventConsumerI;
import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.InvalidRegistration;
import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.RegistrationFailed;
import gov.nih.nci.cabig.ccts.ae.domain.AENotificationType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.security.SecurityManager;
import org.oasis.wsrf.properties.*;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 */

@Transactional(readOnly = false)
public class PSCAdverseEventConsumer implements AdverseEventConsumerI {

    private static final Log logger = LogFactory.getLog(PSCAdverseEventConsumer.class);

    private ScheduledCalendarService scheduledCalendarService;
    
    private PscUserDetailsService pscUserDetailsService;
    
    private SuiteRoleMembership suiteRoleMembership;
    
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    
    private AdverseEventGridServiceAuthorizationHelper gridServicesAuthorizationHelper;

    public boolean authorizedAdverseEventConsumer(){
    	String userName = getGridServicesAuthorizationHelper().getCurrentUsername();
    	if (userName != null){
    		PscUser loadedUser = pscUserDetailsService.loadUserByUsername(userName);
    		Map<SuiteRole, SuiteRoleMembership> memberships = loadedUser.getMemberships();
    		suiteRoleMembership = memberships.get(SuiteRole.AE_REPORTER);
    		if(suiteRoleMembership != null){
    			return true;
    		}
    	}
    	return false;
    }

    public boolean authorizedStudyIdentifier(String studyIdentifier ){
    	return suiteRoleMembership.getStudyIdentifiers().contains(studyIdentifier);
    }
    
    public boolean authorizedSiteIdentifier(String siteidentifier){
    	return suiteRoleMembership.getSiteIdentifiers().contains(siteidentifier);
    }

    public void register(final AENotificationType aeNotification) throws java.rmi.RemoteException, InvalidRegistration, RegistrationFailed {
    	try{
    		// Check for Role
    		// 1. If Assigned Role is AE_REPORTER, then process, otherwise Access Denied.
    		if(!authorizedAdverseEventConsumer()){
    			logger.error("Access Denied");
    			throw new RegistrationFailed();
    		}

    		// TODO: Change fault implementation to accept a reason message.
    		String gridId = aeNotification.getRegistrationGridId();
    		if (gridId == null) {
    			logger.error("No registrationGridId provided");
    			throw new InvalidRegistration();
    		}
    		// get the StudySubjectAssignment
    		StudySubjectAssignment studySubjectAssignment = studySubjectAssignmentDao.getByGridId(gridId);
    		StudySite studySite = studySubjectAssignment.getStudySite();
    		Study study = studySite.getStudy();
    		String studyAssignedIdentifier = study.getAssignedIdentifier();
    		// Authorization
    		if(!authorizedStudyIdentifier(studyAssignedIdentifier)){
    			String message = "Access Denied: AE_REPORTER is not authorized for this Study Identifier : " + studyAssignedIdentifier;
    			logger.error(message);
    			throw new RegistrationFailed();
    		}
    		
    		Site site = studySite.getSite();
    		
    		String siteAssignedIdentifier = site.getAssignedIdentifier();
    		// Authorization
    		if(!authorizedSiteIdentifier(siteAssignedIdentifier)){
    			String message = "Access Denied: AE_REPORTER is not authorized for this Site Identifier : " + siteAssignedIdentifier;
    			logger.error(message);
    			throw new RegistrationFailed();
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
    	}catch (Exception e) {
    		logger.error("Error while creating Adverse Event", e);
    		throw new RemoteException("Unable to create Adverse Event", e);
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
    
    public PscUserDetailsService getPscUserDetailsService() {
		return pscUserDetailsService;
	}
	@Required
	public void setPscUserDetailsService(PscUserDetailsService pscUserDetailsService) {
		this.pscUserDetailsService = pscUserDetailsService;
	}

	public StudySubjectAssignmentDao getStudySubjectAssignmentDao() {
		return studySubjectAssignmentDao;
	}
	@Required
	public void setStudySubjectAssignmentDao(
			StudySubjectAssignmentDao studySubjectAssignmentDao) {
		this.studySubjectAssignmentDao = studySubjectAssignmentDao;
	}
	
	public AdverseEventGridServiceAuthorizationHelper getGridServicesAuthorizationHelper() {
		if(gridServicesAuthorizationHelper==null){
			gridServicesAuthorizationHelper = new AdverseEventGridServiceAuthorizationHelper();
		}
		return gridServicesAuthorizationHelper;
	}
	public void setGridServicesAuthorizationHelper(
			AdverseEventGridServiceAuthorizationHelper gridServicesAuthorizationHelper) {
		this.gridServicesAuthorizationHelper = gridServicesAuthorizationHelper;
	}
}
