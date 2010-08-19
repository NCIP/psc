package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.BlackoutDateDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER;

/**
 * @author Nataliya Shurupova
 */

@AccessControl(roles = {Role.STUDY_ADMIN, Role.SYSTEM_ADMINISTRATOR})
public class BlackoutDatesController extends PscSimpleFormController implements PscAuthorizedHandler {
    private SiteDao siteDao;
    private BlackoutDateDao blackoutDateDao;
    private static final Logger log = LoggerFactory.getLogger(BlackoutDatesController.class.getName());

    public BlackoutDatesController() {
        setCommandClass(BlackoutDatesCommand.class);
        setFormView("manageBlackoutDates");
        setBindOnNewForm(true);
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        Site site = parseSite(queryParameters);

        if (site == null) {
            throw new StudyCalendarSystemException("Site parameter is invalid");
        }

        return ResourceAuthorization.createCollection(site, PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);

    }

    private Site parseSite(Map<String, String[]> queryParameters) {
        Integer siteId = null;

        String[] siteArray = queryParameters.get("site");
        if (siteArray != null && siteArray[0] != null) {
            try {
                siteId = Integer.parseInt(siteArray[0]);
            } catch (NumberFormatException e) {
                log.debug("Site parameter is invalid " + e);
            }
        }

        return siteId != null ? siteDao.getById(siteId) : null;
    }

    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        return new BlackoutDatesCommand(siteDao, blackoutDateDao);
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

    @Required
    public void setBlackoutDateDao(BlackoutDateDao blackoutDateDao) {
        this.blackoutDateDao = blackoutDateDao;
    }
}



