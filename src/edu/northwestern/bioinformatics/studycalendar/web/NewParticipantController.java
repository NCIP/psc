package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomDateEditor;

import javax.servlet.http.HttpServletRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;

/**
 * @author Padmaja Vedula
 */
public class NewParticipantController extends SimpleFormController {
    private ParticipantDao participantDao;
    private String pattern = "MM/dd/yyyy";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    public NewParticipantController() {
        setCommandClass(NewParticipantCommand.class);
        setFormView("createParticipant");
        setSuccessView("assignParticipant");
    }

    protected void initBinder(HttpServletRequest request,
		ServletRequestDataBinder binder) throws Exception {
		binder.registerCustomEditor(Date.class, null, new CustomDateEditor(
		simpleDateFormat, false));
		super.initBinder(request, binder);
    }
    
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        //can be probably loaded from a properties file ??
        Map<String, String> genders = new HashMap<String, String>();
        genders.put("Female", "Female");
        genders.put("male", "male");
        refdata.put("genders", genders);
        refdata.put("action", "New");
        return refdata;
    }

    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
    	NewParticipantCommand participantCommand = (NewParticipantCommand) oCommand;
    	Participant participant = participantCommand.createParticipant();
    	System.out.println("new participant name : " + participant.getFirstName());
        participantDao.save(participant);

        Map<String, Object> model = errors.getModel();
        model.put("participant", participant);
        return new ModelAndView(new RedirectView(getSuccessView()), model);
        //return new ModelAndView(new RedirectView(getSuccessView()), "newParticipant", participant);
    }

    ////// CONFIGURATION

    @Required
    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }
}
