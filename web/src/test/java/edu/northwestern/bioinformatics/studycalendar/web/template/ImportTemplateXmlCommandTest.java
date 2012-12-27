/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.importer.TemplateImportService;
import org.apache.commons.lang.StringUtils;
import static org.easymock.EasyMock.expect;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public class ImportTemplateXmlCommandTest extends StudyCalendarTestCase {

    private ImportTemplateXmlCommand command;
    private MultipartFile file;
    private InputStream stream;
    private TemplateImportService templateImportService;

    protected void setUp() throws Exception {
        super.setUp();

        file = registerMockFor(MockMultipartFile.class);
        stream = registerMockFor(InputStream.class);
        templateImportService =  registerMockFor(TemplateImportService.class);

        command = new ImportTemplateXmlCommand();
        command.setStudyXml(file);
        command.setTemplateImportService(templateImportService);
    }

    public void testApply() throws Exception {
        expect(file.getInputStream()).andReturn(stream);
        templateImportService.readAndSaveTemplate(stream);
        replayMocks();

        command.apply();
        verifyMocks();
    }

    public void testValidate() throws Exception {
        expect(file.isEmpty()).andReturn(true);
        replayMocks();

        BindException errors = new BindException(file, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();

        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.template.xml.not.specified", errors.getGlobalError().getCode());
    }
}
