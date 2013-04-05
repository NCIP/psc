/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class DeletePeriodController extends PscSimpleFormController implements PscAuthorizedHandler {
    private PeriodDao periodDao;
    private AmendmentService amendmentService;
    private DeltaService deltaService;
    private StudySegmentDao studySegmentDao;
    private TemplateService templateService;

    public DeletePeriodController() {
        setCommandClass(DeletePeriodCommand.class);
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        int periodId = ServletRequestUtils.getRequiredIntParameter(request, "period");
        int studySegmentId = ServletRequestUtils.getRequiredIntParameter(request, "studySegment");

        Period period = periodDao.getById(periodId);
        StudySegment studySegment = studySegmentDao.getById(studySegmentId);
        return new DeletePeriodCommand(period, studySegment, amendmentService, templateService);
    }

    @Override
    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        DeletePeriodCommand command = (DeletePeriodCommand) oCommand;
        Map<String, Object> model = new HashMap<String, Object>();

        command.apply();

        Study study = templateService.findStudy(command.getStudySegment());
        StudySegment studySegment = command.getStudySegment();
        studySegment = deltaService.revise(studySegment);
        model.put("developmentRevision", study.getDevelopmentAmendment());
        model.put("canEdit", true);  // okay since you can't execute this code unless you can edit

        model.put("studySegment", new StudySegmentTemplate(studySegment));
        return new ModelAndView("template/ajax/selectStudySegment", model);
    }

    ////// CONFIGURATION

    @Required
    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}

