package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nataliya Shurupova
 */

@AccessControl(roles = {Role.STUDY_ADMIN, Role.SYSTEM_ADMINISTRATOR})
public class BlackoutDatesController extends PscSimpleFormController {
    private SiteDao siteDao;
    private static final Logger log = LoggerFactory.getLogger(BlackoutDatesController.class.getName());

    public BlackoutDatesController() {
        setCommandClass(BlackoutDatesCommand.class);
        setFormView("manageBlackoutDates");
        setBindOnNewForm(true);
    }

    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        return new BlackoutDatesCommand(siteDao);
    }

    protected ModelAndView onSubmit(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object oCommand, BindException errors) throws Exception {

        BlackoutDatesCommand command = (BlackoutDatesCommand) oCommand;
        command.execute();
        return new ModelAndView(getFormView(), errors.getModel());
    }

    protected void initBinder(HttpServletRequest httpServletRequest,
                              ServletRequestDataBinder servletRequestDataBinder) throws Exception {
        super.initBinder(httpServletRequest, servletRequestDataBinder);
        getControllerTools().registerDomainObjectEditor(servletRequestDataBinder, "site", siteDao);
    }

    ////// CONFIGURATION
    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}



