package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;

/**
 * @author Rhett Sutphin
 */
public class NewStudyController extends SimpleFormController {
    private StudyDao studyDao;

    public NewStudyController() {
        setCommandClass(NewStudyCommand.class);
        setFormView("editStudy");
        setSuccessView("viewStudy");
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        refdata.put("action", "New");
        return refdata;
    }

    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
        NewStudyCommand command = (NewStudyCommand) oCommand;
        Study study = command.createStudy();
        // TODO: transaction
        studyDao.save(study);

        Map<String, Object> model = errors.getModel();
        model.put("study", study);
        return new ModelAndView(getSuccessView(), model);
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
