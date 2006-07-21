package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
public class NewStudyController extends SimpleFormController {
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
}
