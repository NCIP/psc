package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Date.class, null, new CustomDateEditor(simpleDateFormat, false));
        super.initBinder(request, binder);
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        Collection<Participant> participants = participantDao.getAll();
        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
        // TODO: for now, there's just a single default site for each study
        refdata.put("studySite", study.getStudySites().get(0));
        refdata.put("study", study);
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
