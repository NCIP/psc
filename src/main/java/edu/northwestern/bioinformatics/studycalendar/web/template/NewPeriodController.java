package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.PlanTreeNodeService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class NewPeriodController extends AbstractPeriodController<NewPeriodCommand> {
    private AmendmentService amendmentService;
    private StudySegmentDao studySegmentDao;

    private PlanTreeNodeService planTreeNodeService;
    private PeriodDao periodDao;
    private DeltaService deltaService;

    public NewPeriodController() {
        super(NewPeriodCommand.class);
        setCrumb(new Crumb());
        setSessionForm(true);
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new NewPeriodCommand(amendmentService);
    }


    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "studySegment", studySegmentDao);
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map<String, Object> refdata = super.referenceData(request, command, errors);
        refdata.put("verb", "add");
        Study study = templateService.findStudy(((PeriodCommand) command).getStudySegment());
        if (study.getDevelopmentAmendment() != null) {
            Study revisedStudy = deltaService.revise(study, study.getDevelopmentAmendment());
            refdata.put("selectedStudy", revisedStudy.getNaturalKey());

        }
        refdata.put("studyId", study.getId());


        getControllerTools().addHierarchyToModel(((PeriodCommand) command).getStudySegment(), refdata);
        return refdata;
    }

    @Override
    protected ModelAndView onSubmit(final HttpServletRequest request, final HttpServletResponse httpServletResponse, final Object oCommand, final BindException e) throws Exception {

        if (isCopyRequest(request)) {


            if (request.getParameter("selectedPeriod") != null && !StringUtils.isEmpty(request.getParameter("selectedPeriod"))) {
                NewPeriodCommand command = (NewPeriodCommand) oCommand;


                String isDevelopmentTemplateSelected = ServletRequestUtils.getRequiredStringParameter(request, "isDevelopmentTemplateSelected");
                int selectedPeriod = ServletRequestUtils.getIntParameter(request, "selectedPeriod");

                Period copiedPeriod = null;

                Period period = periodDao.getById(selectedPeriod);

                if (isDevelopmentTemplateSelected.equalsIgnoreCase("false")) {
                    copiedPeriod = planTreeNodeService.copy(period, false);
                } else if (isDevelopmentTemplateSelected.equalsIgnoreCase("true")) {
                    //user has selected the releaesd template so dont revise the study
                    copiedPeriod = planTreeNodeService.copy(period, true);
                }
                command.setPeriod(copiedPeriod);
                command.apply();
                Study study = studyService.saveStudyFor(command.getStudySegment());
                 
                return new ModelAndView(new RedirectView("editPeriod"), "period", copiedPeriod.getId());


            } else {
                logger.debug("User must select atleast one period. ");
                return super.showForm(request, httpServletResponse, e);
            }


        } else {
            return super.onSubmit(request, httpServletResponse, oCommand, e);
        }

    }


    private boolean isCopyRequest(final HttpServletRequest request) {
        String copyPeriod = request.getParameter("copyPeriod");
        return copyPeriod != null && copyPeriod.equalsIgnoreCase("Copy");

    }


    @Required
    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }


    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setPlanTreeNodeService(final PlanTreeNodeService planTreeNodeService) {
        this.planTreeNodeService = planTreeNodeService;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Add period");
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            return Collections.singletonMap("studySegment", context.getStudySegment().getId().toString());
        }
    }

    @Required
    public void setPeriodDao(final PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

    @Required
    public void setDeltaService(final DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
