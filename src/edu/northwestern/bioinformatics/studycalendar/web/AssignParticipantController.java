package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Collections;

/**
 * @author Padmaja Vedula
 */
public class AssignParticipantController extends SimpleFormController {
    private ParticipantDao participantDao;
    private ParticipantService participantService;
    private StudyDao studyDao;
    private StudySiteDao studySiteDao;
    private ArmDao armDao;

    public AssignParticipantController() {
        setCommandClass(AssignParticipantCommand.class);
        setFormView("assignParticipant");
        setBindOnNewForm(true);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Date.class, ControllerTools.getDateEditor(true));
        ControllerTools.registerDomainObjectEditor(binder, "arm", armDao);
        ControllerTools.registerDomainObjectEditor(binder, "studySite", studySiteDao);
        ControllerTools.registerDomainObjectEditor(binder, "participant", participantDao);
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        Collection<Participant> participants = participantDao.getAll();
        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
        // TODO: for now, there's just a single default site for each study
        refdata.put("studySite", study.getStudySites().get(0));
        refdata.put("study", study);
        refdata.put("participants", participants);
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        ControllerTools.addHierarchyToModel(epoch, refdata);
        List<Arm> arms = epoch.getArms();
        if (arms.size() > 1) {
            refdata.put("arms", arms);
        } else {
            refdata.put("arms", Collections.emptyList());
        }
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AssignParticipantCommand command = (AssignParticipantCommand) oCommand;
        StudyParticipantAssignment assignment = command.assignParticipant();
        return new ModelAndView("redirectToSchedule", "assignment", assignment.getId().intValue());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AssignParticipantCommand command = new AssignParticipantCommand();
        command.setParticipantService(participantService);
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

    @Required
    public void setArmDao(ArmDao armDao) {
        this.armDao = armDao;
    }

    @Required
    public void setParticipantService(ParticipantService participantService) {
        this.participantService = participantService;
    }
}
