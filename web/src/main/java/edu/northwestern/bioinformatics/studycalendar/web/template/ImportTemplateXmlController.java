/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.service.importer.TemplateDifferenceException;
import edu.northwestern.bioinformatics.studycalendar.service.importer.TemplateImportService;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.DATA_IMPORTER;

public class ImportTemplateXmlController extends PscSimpleFormController implements PscAuthorizedHandler {
    private TemplateImportService templateImportService;

    public ImportTemplateXmlController() {
        setCommandClass(ImportTemplateXmlCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("template/importTemplate");
        setSuccessView("redirectToStudyList");
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(DATA_IMPORTER);
    }
    
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws Exception {

        ImportTemplateXmlCommand command = (ImportTemplateXmlCommand) o;
        try {
            command.apply();
        } catch (TemplateDifferenceException e) {
            errors.reject("error.problem.importing.file",
                new String[] { e.getDifferences().toTreeHtml() }, e.getMessage());
        } catch (StudyCalendarValidationException e) {
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
        command.setTemplateImportService(templateImportService);
        return command;
    }

    //// Field setters
    @Required
    public void setTemplateImportService(TemplateImportService templateImportService) {
        this.templateImportService = templateImportService;
    }
}
