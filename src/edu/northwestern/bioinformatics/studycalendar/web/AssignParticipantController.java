package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;

/**
 * @author Padmaja Vedula
 */
public class AssignParticipantController extends SimpleFormController {
    private ParticipantDao participantDao;
	private StudyDao studyDao;
	private StudySiteDao studySiteDao;
    private String pattern = "MM/dd/yyyy";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);


    public AssignParticipantController() {
        setCommandClass(AssignParticipantCommand.class);
        setFormView("assignParticipant");
        setSuccessView("calendarTemplate");
    }

    protected void initBinder(HttpServletRequest request,
		ServletRequestDataBinder binder) throws Exception {
		binder.registerCustomEditor(Date.class, null, new CustomDateEditor(
		simpleDateFormat, false));
		super.initBinder(request, binder);
    }
    
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        Collection<Participant> participants = participantDao.getAll();
        StudySite studySite = studySiteDao.getById(1);
        refdata.put("studySite", studySite);
        refdata.put("study", studyDao.getById(ServletRequestUtils.getIntParameter(httpServletRequest, "id")));
        refdata.put("participants", participants);
        refdata.put("action", "Assign");
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
    	AssignParticipantCommand assignCommand = (AssignParticipantCommand) oCommand;
    	Participant participant = assignCommand.assignParticipant();
    	participantDao.save(participant);
       
        return new ModelAndView(new RedirectView(getSuccessView()), "id", ServletRequestUtils.getIntParameter(request, "id"));
    }
    
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
    	AssignParticipantCommand command = new AssignParticipantCommand();
    	command.setParticipantDao(participantDao);
    	command.setStudyDao(studyDao);
    	command.setStudySiteDao(studySiteDao);
    	return command;
    }

    ////// CONFIGURATION

    @Required
    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }
    
    @Required
    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }
    
    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
}
