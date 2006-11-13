package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import gov.nih.nci.security.AuthenticationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.apache.log4j.Logger;

/**
 * @author Yufang Wang
 * @author Jaron Sampson
 */

@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class ReportBuilderController extends PscSimpleFormController {
	private StudyDao studyDao;
	private SiteDao siteDao;
	private ParticipantDao participantDao;
	private static final Logger log = Logger.getLogger(ReportBuilderController.class.getName());

    public ReportBuilderController() {
        setCommandClass(ReportBuilderCommand.class);
        setFormView("reportBuilder");
        setSuccessView("reportBuilder");
    }
    
    
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        log.debug("referenceData"); 
        Map<String, Object> refdata = new HashMap<String, Object>();
        List<Study> studies = new ArrayList<Study>();
        List<Site> sites = new ArrayList<Site>();
    	List<Participant> participants = new ArrayList<Participant>();
        studies = studyDao.getAll();
        sites = siteDao.getAll();
        participants = participantDao.getAll();
        
    	refdata.put("studies", studies);        
    	refdata.put("sites", sites);        
    	refdata.put("participants", participants);        
        refdata.put("action", "Report");
        
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
    	ReportBuilderCommand reportCommand = (ReportBuilderCommand) oCommand;
    	
        Map<String, Object> model = new HashMap<String, Object>();
        Date startDate = reportCommand.getStartDate();
        Date endDate = reportCommand.getEndDate();
        List<Study> studies = new ArrayList<Study>();
        List<Site> sites = new ArrayList<Site>();
        List<Participant> participants = new ArrayList<Participant>();
        studies = reportCommand.getStudies();
        sites = reportCommand.getSites();
        participants = reportCommand.getParticipants();
        
        model.put("studies", studies);
        model.put("sites", sites);
        model.put("participants", participants);
    	  	
        return new ModelAndView("reportBuilder", model);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        //log.debug("formBackingObject");
    	ReportBuilderCommand command = new ReportBuilderCommand();
        return command;
    }


    ////// CONFIGURATION
    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
    
    @Required
    public void setParticipantDao(ParticipantDao pDao) {
        this.participantDao = pDao;
    }
}
