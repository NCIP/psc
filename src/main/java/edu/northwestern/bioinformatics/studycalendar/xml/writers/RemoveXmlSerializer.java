package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;

public class RemoveXmlSerializer extends AbstractChildrenChangeXmlSerializer {
    private static final String REMOVE = "remove";

    public RemoveXmlSerializer(Study study) {
        super(study);
    }

    protected Change changeInstance() {
        return new Remove();
    }

    protected String elementName() {
        return REMOVE;
    }
}
