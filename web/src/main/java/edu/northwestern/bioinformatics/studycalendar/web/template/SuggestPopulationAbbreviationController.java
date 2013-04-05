/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.service.PopulationService;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;

import java.util.Collection;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

/**
 * @author Rhett Sutphin
 */
public class SuggestPopulationAbbreviationController extends AbstractController implements PscAuthorizedHandler {
    private PopulationService populationService;
    private StudyDao studyDao;

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

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
