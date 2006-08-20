package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;

/**
 * @author Padmaja Vedula
 */
public class NewParticipantController extends SimpleFormController {
    private ParticipantDao participantDao;

    public NewParticipantController() {
        setCommandClass(NewParticipantCommand.class);
        setFormView("createParticipant");
        setSuccessView("createParticipant");
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        refdata.put("action", "New");
        return refdata;
    }

    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
    	NewParticipantCommand participantCommand = (NewParticipantCommand) oCommand;
    	Participant participant = participantCommand.createParticipant();
        participantDao.save(participant);

        Map<String, Object> model = errors.getModel();
        model.put("participant", participant);
        return new ModelAndView(getSuccessView(), model);
    }

    ////// CONFIGURATION

    @Required
    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }
}
