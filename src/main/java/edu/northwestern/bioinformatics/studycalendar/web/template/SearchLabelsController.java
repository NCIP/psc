package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: May 19, 2008
 * Time: 2:38:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchLabelsController extends PscAbstractCommandController<SearchLabelsCommand> {
    private LabelDao labelDao;

    public SearchLabelsController() {
        setCommandClass(SearchLabelsCommand.class);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(ActivityType.class, new ControlledVocabularyEditor(ActivityType.class));
    }

    protected ModelAndView handle(SearchLabelsCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("GET".equals(request.getMethod())) {
            Map<String, Object> model =  new HashMap<String, Object>();

            String searchText = command.getSearchText() != null ? command.getSearchText() : EMPTY;
            List<Label> results = labelDao.getLablesBySearchText(searchText);
            model.put("labels", results);
            
            return new ModelAndView("template/ajax/repetitions", model);
        } else {
            getControllerTools().sendGetOnlyError(response);
            return null;
        }
    }

    public LabelDao getLabelDao() {
        return labelDao;
    }

    public void setLabelDao(LabelDao labelDao) {
        this.labelDao = labelDao;
    }
}