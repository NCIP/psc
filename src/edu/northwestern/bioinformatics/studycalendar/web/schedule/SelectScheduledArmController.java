package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledArmDao;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class SelectScheduledArmController implements Controller {
    private ScheduledArmDao scheduledArmDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int armId = ServletRequestUtils.getRequiredIntParameter(request, "arm");
        Map<String, Object> model = new ModelMap("arm", scheduledArmDao.getById(armId));

        return new ModelAndView("schedule/ajax/selectScheduledArm", model);
    }

    @Required
    public void setScheduledArmDao(ScheduledArmDao scheduledArmDao) {
        this.scheduledArmDao = scheduledArmDao;
    }
}
