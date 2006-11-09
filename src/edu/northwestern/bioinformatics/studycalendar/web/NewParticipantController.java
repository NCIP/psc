package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomDateEditor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

/**
 * @author Padmaja Vedula
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class NewParticipantController extends PscSimpleFormController {
    private ParticipantDao participantDao;

    public NewParticipantController() {
        setCommandClass(NewParticipantCommand.class);
        setFormView("createParticipant");
        setSuccessView("assignParticipant");
    }

    protected void initBinder(HttpServletRequest request,
        ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Date.class, ControllerTools.getDateEditor(true));
        super.initBinder(request, binder);
    }
    
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        //can be probably loaded from a properties file ??
        Map<String, String> genders = new HashMap<String, String>();
        genders.put("Female", "Female");
        genders.put("Male", "Male");
        refdata.put("genders", genders);
        refdata.put("action", "New");
        refdata.put("studyId", ServletRequestUtils.getIntParameter(httpServletRequest, "id"));
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        NewParticipantCommand participantCommand = (NewParticipantCommand) oCommand;
        Participant participant = participantCommand.createParticipant();
        participantDao.save(participant);

        Map<String, Object> model = errors.getModel();
        model.put("participant", participant);
        return new ModelAndView(new RedirectView(getSuccessView()), "id", ServletRequestUtils.getIntParameter(request, "id"));
        //return new ModelAndView(new RedirectView(getSuccessView()), "newParticipant", participant);
    }

    ////// CONFIGURATION

    @Required
    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }
}
