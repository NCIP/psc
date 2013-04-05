/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.po.HealthCareFacility;
import gov.nih.nci.coppa.po.Organization;
import gov.nih.nci.coppa.po.ResearchOrganization;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import gov.nih.nci.coppa.services.pa.StudySite;
import org.osgi.framework.BundleContext;

/**
 * Strategy-style interface for low-level COPPA access.  It is possible for any of these
 * methods to return null -- some for business reasons and some only in case of an error.
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

    StudySite[] searchStudySitesByStudySite(StudySite s, LimitOffset l);

    ResearchOrganization[] getResearchOrganizationsByPlayerIds(gov.nih.nci.coppa.po.Id[] ids );

    Organization[] searchOrganizations(Organization criteria);

    Organization getOrganization(gov.nih.nci.coppa.po.Id id);

    ResearchOrganization[] getResearchOrganizations(gov.nih.nci.coppa.po.Id[] ids );

    HealthCareFacility[] getHealthCareFacilities(gov.nih.nci.coppa.po.Id[] ids);

    HealthCareFacility[] getHealthCareFacilitiesByPlayerIds(gov.nih.nci.coppa.po.Id[] ids);
}
