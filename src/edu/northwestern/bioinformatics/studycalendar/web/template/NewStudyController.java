package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;

/**
 * @author Rhett Sutphin
 */
public class NewStudyController extends SimpleFormController {
    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySiteDao studySiteDao;

    public NewStudyController() {
        setCommandClass(NewStudyCommand.class);
        setFormView("editStudy");
        setSuccessView("redirectToCalendarTemplate");
        setBindOnNewForm(true);
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        refdata.put("action", "New");
        return refdata;
    }

    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
        NewStudyCommand command = (NewStudyCommand) oCommand;
        Study study = command.createStudy();
        // TODO: transaction
        studyDao.save(study);

        //// XXX: TEMPORARY until there's an interface for setting up sites & assigning studies to them
        StudySite ss = new StudySite();
        ss.setStudy(study);
        ss.setSite(siteDao.getDefaultSite());
        studySiteDao.save(ss);

        return new ModelAndView(getSuccessView(), "id", study.getId());
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }
}
