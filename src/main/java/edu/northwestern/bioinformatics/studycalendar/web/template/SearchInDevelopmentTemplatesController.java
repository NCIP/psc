package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.StudyListController;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Saurabh Agrawal
 */
public class SearchInDevelopmentTemplatesController extends PscAbstractCommandController<SearchInDevelopmentTemplateCommand> {
    private StudyDao studyDao;
    private TemplateService templateService;

    public SearchInDevelopmentTemplatesController() {
        setCommandClass(SearchInDevelopmentTemplateCommand.class);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
    }

    protected ModelAndView handle(SearchInDevelopmentTemplateCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("GET".equals(request.getMethod())) {
            Map<String, Object> model = new HashMap<String, Object>();
            String searchText = command.getSearchText() != null ? command.getSearchText() : EMPTY;

            List<StudyListController.DevelopmentTemplate> results = templateService.getInDevelopmentTemplates(searchText);
            model.put("inDevelopmentTemplates", results);

            return new ModelAndView("template/ajax/inDevelopmentTemplates", model);
        }

        else {
            getControllerTools().sendGetOnlyError(response);
            return null;
        }
    }


    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }


    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
