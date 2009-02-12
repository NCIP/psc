package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.service.ImportTemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyImportException;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ImportTemplateXmlController extends PscSimpleFormController {
    private ImportTemplateService importTemplateService;

    public ImportTemplateXmlController() {
        setCommandClass(ImportTemplateXmlCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("template/importTemplate");
        setSuccessView("redirectToStudyList");
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws Exception {

        ImportTemplateXmlCommand command = (ImportTemplateXmlCommand) o;
        try {
            command.apply();
        } catch (StudyImportException e) {
            errors.reject("error.problem.importing.file", new String[]{e.getMessage()}, e.getMessage());

        }
        if (errors.hasErrors()) {
            return showForm(request, response, errors);
        }
        Map<String, Object> model = errors.getModel();

        return new ModelAndView(getSuccessView(), model);
    }

    protected ImportTemplateXmlCommand formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        ImportTemplateXmlCommand command = new ImportTemplateXmlCommand();
        command.setImportTemplateService(importTemplateService);
        return command;
    }

    //// Field setters
    @Required
    public void setImportTemplateService(ImportTemplateService importTemplateService) {
        this.importTemplateService = importTemplateService;
    }
}
