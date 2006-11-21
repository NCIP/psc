package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import org.apache.log4j.Logger;

/**
 * @author Yufang Wang
 * @author Jaron Sampson
 */

@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class ReportBuilderController extends PscSimpleFormController {
	private SiteDao siteDao;
	private static final Logger log = Logger.getLogger(ReportBuilderController.class.getName());

    public ReportBuilderController() {
        setCommandClass(ReportBuilderCommand.class);
        setFormView("reporting/reportBuilder");
        setSuccessView("reporting/reportBuilder");
    }
    
    
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        log.debug("referenceData"); 
        Map<String, Object> refdata = new HashMap<String, Object>();
        List<Study> studies = new ArrayList<Study>();
        List<Site> sites = new ArrayList<Site>();
        List<Participant> participants = new ArrayList<Participant>();
        sites = siteDao.getAll();
        
    	refdata.put("studies", studies);        
    	refdata.put("sites", sites);        
    	refdata.put("participants", participants);        
        
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
    	  	
        return new ModelAndView("reporting/reportBuilder", model);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        //log.debug("formBackingObject");
    	ReportBuilderCommand command = new ReportBuilderCommand();
        return command;
    }


    ////// CONFIGURATION    
    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
