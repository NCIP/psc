package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializerPostProcessor;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializerPreProcessor;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Transactional
public class ImportTemplateService {
    private StudyXmlSerializer studyXmlSerializer;
    private StudyXmlSerializerPreProcessor studyPreProcessor;
    private StudyXmlSerializerPostProcessor studyPostProcessor;
    private StudyDao studyDao;

    /**
     * This method will be called when importing from within the Import Template Page
     *
     * @param stream
     */
    public void readAndSaveTemplate(InputStream stream) {
        // TODO: it should be possible to do this without re-reading the stream
        // (or relying on reset, which is not always available)
        byte[] contents;
        try {
            contents = IOUtils.toByteArray(stream);
        } catch (IOException e) {
            throw new StudyCalendarSystemException("Reading imported stream failed", e);
        }

        String id = studyXmlSerializer.readAssignedIdentifier(new ByteArrayInputStream(contents));
        Study study = studyDao.getByAssignedIdentifier(id);
        readAndSaveTemplate(study, new ByteArrayInputStream(contents));
    }

    public Study readAndSaveTemplate(Study existingStudy, InputStream stream) {

        Document document = studyXmlSerializer.deserializeDocument(stream);
        Element element = document.getRootElement();

        studyXmlSerializer.validate(element);

        if (existingStudy != null) {
            studyPreProcessor.process(existingStudy);
        } else {
            existingStudy = new Study();
        }

        Study study = studyXmlSerializer.readElement(element, existingStudy);

        studyPostProcessor.process(study);

        return study;
    }

    ////// Bean Setters
    @Required
    public void setStudyXmlSerializer(StudyXmlSerializer studyXmlSerializer) {
        this.studyXmlSerializer = studyXmlSerializer;
    }

    @Required
    public void setStudyXmlSerializerPreProcessor(StudyXmlSerializerPreProcessor studyPreProcessor) {
        this.studyPreProcessor = studyPreProcessor;
    }

    @Required
    public void setStudyXmlSerializerPostProcessor(StudyXmlSerializerPostProcessor studyPostProcessor) {
        this.studyPostProcessor = studyPostProcessor;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
