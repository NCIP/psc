package edu.northwestern.bioinformatics.studycalendar.web.template;

import static org.easymock.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.ImportActivitiesService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindException;
import org.springframework.mock.web.MockMultipartFile;
import org.apache.commons.lang.StringUtils;

public class ImportActivitiesCommandTest extends StudyCalendarTestCase {
    private ImportActivitiesService service;
    private ImportActivitiesCommand command;
    private MultipartFile file;

    protected void setUp() throws Exception {
        super.setUp();

        service = registerMockFor(ImportActivitiesService.class);
        file = registerMockFor(MockMultipartFile.class);

        command = new ImportActivitiesCommand();
        command.setImportActivitiesService(service);
        command.setActivitiesFile(file);
    }

    public void testApply() throws Exception {
        expect(file.getInputStream()).andReturn(null);
        expect(file.getContentType()).andReturn("text/xml");
        service.loadAndSave(null);
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
        assertEquals("Wrong error code", "error.activities.file.not.specified", errors.getGlobalError().getCode());
    }
}
