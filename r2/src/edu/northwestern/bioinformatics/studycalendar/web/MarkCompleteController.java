package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.view.RedirectView;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;

/**
 * @author Jaron Sampson
 */
 
public class MarkCompleteController extends SimpleFormController {
    private StudyDao studyDao;

    public MarkCompleteController() {
        setCommandClass(MarkCompleteCommand.class);
        setFormView("markComplete");
        setSuccessView("studyList");
    }
    
    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("study", studyDao.getById(ServletRequestUtils.getIntParameter(request, "id")));
        return data;
    }

    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
        MarkCompleteCommand command = (MarkCompleteCommand) oCommand;
        Study study = studyDao.getById(command.getStudyId());
        if("true".equals(command.getCompleted())) {
        	study.getPlannedSchedule().setComplete(true);
            studyDao.save(study);
        }
        // TODO: transaction

        Map<String, Object> model = errors.getModel();
        model.put("study", study);        
        return new ModelAndView(new RedirectView(getSuccessView()));
    }

    
    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
