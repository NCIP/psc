package edu.northwestern.bioinformatics.studycalendar.web.template;

import static org.easymock.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.dataloaders.MultipartFileActivityLoader;
import edu.northwestern.bioinformatics.studycalendar.utils.validators.XmlValidator;
import edu.northwestern.bioinformatics.studycalendar.utils.validators.Schema;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;

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
