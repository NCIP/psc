package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.service.PopulationService;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;

/**
 * @author Rhett Sutphin
 */
public class SuggestPopulationAbbreviationController extends AbstractController {
    private PopulationService populationService;
    private StudyDao studyDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Integer studyId = ServletRequestUtils.getRequiredIntParameter(request, "study");
        String name = ServletRequestUtils.getRequiredStringParameter(request, "populationName");
        return new ModelAndView(
            "template/ajax/suggestPopulationAbbreviation",
            "suggestion",
            populationService.suggestAbbreviation(studyDao.getById(studyId), name)
        );
    }

    @Required
    public void setPopulationService(PopulationService populationService) {
        this.populationService = populationService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
