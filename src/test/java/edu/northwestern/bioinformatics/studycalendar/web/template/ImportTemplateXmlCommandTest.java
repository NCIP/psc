package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.readers.StudyXMLReader;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import org.apache.commons.lang.StringUtils;
import static org.easymock.EasyMock.expect;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;

public class ImportTemplateXmlCommandTest extends StudyCalendarTestCase {

    private StudyXMLReader reader;
    private StudyService studyService;
    private ImportTemplateXmlCommand command;
    private MultipartFile file;

    protected void setUp() throws Exception {
        super.setUp();

        studyService = registerMockFor(StudyService.class);
        reader = registerMockFor(StudyXMLReader.class);
        file = registerMockFor(MockMultipartFile.class);

        command = new ImportTemplateXmlCommand();
        command.setStudyXMLReader(reader);
        command.setStudyService(studyService);
        command.setStudyXml(file);
    }

    public void testApply() throws Exception {
        expect(file.getInputStream()).andReturn(null);
        expect(reader.read(null)).andReturn(null);
        studyService.save(null);
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
