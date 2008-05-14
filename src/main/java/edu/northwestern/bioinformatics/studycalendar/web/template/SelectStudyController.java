package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Saurabh Agrawal
 */
public class SelectStudyController implements Controller {
    private DeltaService deltaService;
    private StudyDao studyDao;
    private static final Logger log = LoggerFactory.getLogger(SelectStudyController.class.getName());

    @SuppressWarnings({"unchecked"})
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int id = ServletRequestUtils.getRequiredIntParameter(request, "study");

        Study study = studyDao.getById(id);

        Study theRevisedStudy = null;
        if (study.getDevelopmentAmendment() != null) {
            theRevisedStudy = deltaService.revise(study, study.getDevelopmentAmendment());
        } else {
            theRevisedStudy = study;
        }

        List<Epoch> epochs = theRevisedStudy.getPlannedCalendar().getEpochs();
        Map model = new HashMap();
        model.put("epochs", epochs);
        return new ModelAndView("template/ajax/displayEpochs", model);
    }


    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setStudyDao(final StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}

