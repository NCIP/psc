package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SUBJECT_MANAGER;

/**
 * @author Padmaja Vedula
 */
// TODO: this class needs to be cleaned up
public class NewSubjectController extends PscSimpleFormController implements PscAuthorizedHandler {
    private SubjectDao subjectDao;

    public NewSubjectController() {
        setCommandClass(NewSubjectCommand.class);
        setFormView("createSubject");
        setSuccessView("assignSubject");
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(SUBJECT_MANAGER);
    }    

    protected void initBinder(HttpServletRequest request,
        ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(true));
        super.initBinder(request, binder);
    }
    
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        //can be probably loaded from a properties file ??
        Map<String, String> genders = Gender.getGenderMap();


        refdata.put("genders", genders);
        refdata.put("action", "New");
        refdata.put("studyId", ServletRequestUtils.getIntParameter(httpServletRequest, "id"));
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        NewSubjectCommand subjectCommand = (NewSubjectCommand) oCommand;
        Subject subject = subjectCommand.createSubject();
        subjectDao.save(subject);

        // TODO: this is bad -- the redirect view should be full-path context-relative
        return new ModelAndView(new RedirectView(getSuccessView()), "study", ServletRequestUtils.getIntParameter(request, "id"));
        //return new ModelAndView(new RedirectView(getSuccessView()), "newSubject", subject);
    }

    ////// CONFIGURATION

    @Required
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }
}
