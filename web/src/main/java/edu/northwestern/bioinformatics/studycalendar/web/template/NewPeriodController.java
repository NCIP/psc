/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateDevelopmentService;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
public class NewPeriodController extends AbstractPeriodController<PeriodCommand> {
    private TemplateDevelopmentService templateDevelopmentService;
    private AmendmentService amendmentService;
    private DeltaService deltaService;

    private PeriodDao periodDao;
    private StudySegmentDao studySegmentDao;

    public NewPeriodController() {
        super(PeriodCommand.class);
        setCrumb(new Crumb());
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        if (request.getParameter("selectedPeriod") == null) {
            return new NewPeriodCommand(amendmentService, templateService);
        } else {
            return new CopyPeriodCommand(templateService, templateDevelopmentService);
        }
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "studySegment", studySegmentDao);
        getControllerTools().registerDomainObjectEditor(binder, "selectedPeriod", periodDao);
    }

    @Override
    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map<String, Object> refdata = super.referenceData(request, command, errors);
        refdata.put("verb", "Create new");
        Study study = templateService.findStudy(((PeriodCommand) command).getStudySegment());
        if (study.getDevelopmentAmendment() != null) {
            Study revisedStudy = deltaService.revise(study, study.getDevelopmentAmendment());
            refdata.put("selectedStudy", revisedStudy.getNaturalKey());

        }
        refdata.put("studyId", study.getId());

        getControllerTools().addHierarchyToModel(((PeriodCommand) command).getStudySegment(), refdata);
        return refdata;
    }

    @Required
    public void setDeltaService(final DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
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
    public void setTemplateDevelopmentService(TemplateDevelopmentService templateDevelopmentService) {
        this.templateDevelopmentService = templateDevelopmentService;
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Add period");
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            return Collections.singletonMap("studySegment", context.getStudySegment().getId().toString());
        }
    }
}
