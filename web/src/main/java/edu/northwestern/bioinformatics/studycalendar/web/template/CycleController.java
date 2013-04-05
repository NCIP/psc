/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
* @author Jalpa Patel
 * Date: Aug 26, 2008
 */
public class CycleController extends PscAbstractCommandController<CycleCommand> implements PscAuthorizedHandler {
    private StudySegmentDao studySegmentDao;
    private TemplateService templateService;
    private AmendmentService amendmentService;

    public CycleController() {
        setCommandClass(CycleCommand.class);
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }    

    protected Object getCommand(HttpServletRequest httpServletRequest) throws Exception {
        return new CycleCommand(templateService,amendmentService);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "studySegment", studySegmentDao);
    }

    protected ModelAndView handle(CycleCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!errors.hasErrors()) {
             command.apply();
        }
        Study study;
        study = templateService.findStudy(command.getStudySegment());
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("study",study.getId());
        model.put("amendment",study.getDevelopmentAmendment().getId());
        model.put("studySegment",command.getStudySegment().getId());

        return new ModelAndView("redirectToCalendarTemplate", model);
       
    }


    ////// CONFIGURATION

    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    @Required 
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }
}
