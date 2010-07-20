package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Aug 5, 2008
 * Time: 11:00:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddSourceController extends PscAbstractController implements PscAuthorizedHandler {
    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;


    public AddSourceController() {
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(BUSINESS_ADMINISTRATOR);
    }    
                                    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        String sourceName = ServletRequestUtils.getRequiredStringParameter(request, "source");
        if (sourceName == null || sourceName.trim().length()==0) {
            model.put("error", "The source name is invalid");
            model.put("displayCreateNewActivity", Boolean.FALSE);
        } else {
            Source sourceInDB = sourceDao.getByName(sourceName);
            if (sourceInDB == null) {
                Source source = new Source();
                source.setName(sourceName);
                sourceDao.save(source);
                model.put("source", source);
                model.put("displayCreateNewActivity", Boolean.TRUE);
                model.put("activityTypes", activityTypeDao.getAll());
                model.put("showtable", Boolean.TRUE);
            }
        }
        return new ModelAndView("template/ajax/activityTableUpdate", model);
     }


     //// CONFIGURATION
     @Required
     public void setSourceDao(SourceDao sourceDao) {
         this.sourceDao = sourceDao;
     }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
