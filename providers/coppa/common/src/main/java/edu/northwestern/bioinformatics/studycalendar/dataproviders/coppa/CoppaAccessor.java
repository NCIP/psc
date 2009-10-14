package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import gov.nih.nci.coppa.services.pa.StudySite;
import org.osgi.framework.BundleContext;

/**
 * Strategy-style interface for low-level COPPA access.
 *
 * @author Rhett Sutphin
 */
public interface CoppaAccessor {
    /**
     * Tell this accessor to register itself as a service with the given context.
     */
    void register(BundleContext bundleContext);

    ////// PA methods

    StudyProtocol getStudyProtocol(Id id);

    StudyProtocol[] searchStudyProtocols(StudyProtocol criteria, LimitOffset limit);

    StudySite[] searchStudySitesByStudyProtocolId(Id id);
}
