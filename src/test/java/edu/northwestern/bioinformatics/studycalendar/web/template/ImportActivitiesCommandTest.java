package edu.northwestern.bioinformatics.studycalendar.web.template;

import static org.easymock.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.readers.MultipartFileActivityLoader;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindException;
import org.springframework.mock.web.MockMultipartFile;
import org.apache.commons.lang.StringUtils;

public class ImportActivitiesCommandTest extends StudyCalendarTestCase {
    private MultipartFileActivityLoader loader;
    private ImportActivitiesCommand command;
    private MultipartFile file;

    protected void setUp() throws Exception {
        super.setUp();

        loader = registerMockFor(MultipartFileActivityLoader.class);
        file = registerMockFor(MockMultipartFile.class);

        command = new ImportActivitiesCommand();
        command.setActivityLoader(loader);
        command.setActivitiesFile(file);
    }

    public void testApply() throws Exception {
        loader.loadData(file);
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
