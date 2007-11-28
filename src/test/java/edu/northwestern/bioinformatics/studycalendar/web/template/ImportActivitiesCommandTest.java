package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.dataloaders.MultipartFileActivityLoader;
import org.springframework.web.multipart.MultipartFile;

public class ImportActivitiesCommandTest extends StudyCalendarTestCase {
    private MultipartFileActivityLoader loader;
    private ImportActivitiesCommand command;
    private MultipartFile file;

    protected void setUp() throws Exception {
        super.setUp();

        loader = registerMockFor(MultipartFileActivityLoader.class);
        file   = null;

        command = new ImportActivitiesCommand();
        command.setActivityLoader(loader);
    }

    public void testApply() throws Exception {
        loader.loadData(file);
        replayMocks();

        command.apply();
        verifyMocks();
    }
}
