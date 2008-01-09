package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.xml.readers.StudyXMLReader;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class ImportTemplateXmlController extends PscSimpleFormController {
     private StudyXMLReader studyXMLReader;
    private StudyService studyService;
    private DeltaService deltaService;

    public ImportTemplateXmlController() {
        setCommandClass(ImportTemplateXmlCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("template/importTemplate");
        setSuccessView("redirectToStudyList");
    }

    protected ModelAndView onSubmit(Object o, BindException errors) throws Exception {
        ImportTemplateXmlCommand command = (ImportTemplateXmlCommand) o;
        command.apply();

        Map<String, Object> model = errors.getModel();

        return new ModelAndView(getSuccessView(), model);
    }

    protected ImportTemplateXmlCommand formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        ImportTemplateXmlCommand command = new ImportTemplateXmlCommand();
        command.setStudyXMLReader(studyXMLReader);
        command.setStudyService(studyService);
        command.setDeltaService(deltaService);
        return command;
    }

    //// Field setters
    @Required
    public void setStudyXMLReader(StudyXMLReader studyXMLReader) {
        this.studyXMLReader = studyXMLReader;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
