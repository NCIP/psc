/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.CoppaAccessor;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import gov.nih.nci.coppa.services.pa.StudySite;
import gov.nih.nci.coppa.po.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import java.util.Dictionary;

/**
 * @author Rhett Sutphin
 */
public class IntegrationHubCoppaAccessor implements CoppaAccessor, ManagedService {
    public static final String SERVICE_PID =
        "edu.northwestern.bioinformatics.studycalendar.coppa-ihub-strategy";

    public static final String REQUEST_PROCESSOR_URI_KEY = "ihub.request-processor.endpoint";

    private ServiceRegistration registration;
    private String endpoint;
    private IntegrationHubExecutor executor = new IntegrationHubExecutor();

    public void register(BundleContext bundleContext) {
        registration = bundleContext.registerService(new String[] {
            ManagedService.class.getName(), CoppaAccessor.class.getName()
        }, this, new MapBuilder<String, Object>().
            put(Constants.SERVICE_PID, SERVICE_PID).toDictionary());
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public void updated(Dictionary dictionary) throws ConfigurationException {
        if (dictionary != null) {
            registration.setProperties(dictionary);
            this.endpoint = (String) dictionary.get(REQUEST_PROCESSOR_URI_KEY);
        }
    }

    ////// PA INTERACTIONS

    public StudyProtocol getStudyProtocol(gov.nih.nci.coppa.services.pa.Id id) {
        return (StudyProtocol) executor.execute(endpoint, new IntegrationHubCoppaTask(
            PaOperation.GET_STUDY_PROTOCOL,
            id
        ));
    }

    public StudyProtocol[] searchStudyProtocols(StudyProtocol criteria, LimitOffset limit) {
        return (StudyProtocol[]) executor.execute(endpoint, new IntegrationHubCoppaTask(
            PaOperation.STUDY_PROTOCOL_SEARCH,
            criteria, limit
        ));
    }

    public StudySite[] searchStudySitesByStudyProtocolId(gov.nih.nci.coppa.services.pa.Id id) {
        return (StudySite[]) executor.execute(endpoint, new IntegrationHubCoppaTask(
            PaOperation.GET_STUDY_SITES_BY_PROTOCOL, id
        ));
    }

    public StudySite[] searchStudySitesByStudySite(StudySite s, LimitOffset l) {
       return (StudySite[]) executor.execute(endpoint, new IntegrationHubCoppaTask(
            PaOperation.SEARCH_STUDY_SITES_BY_STUDY_SITE, s, l
       ));
    }

    public ResearchOrganization[] getResearchOrganizationsByPlayerIds(Id[] ids) {
        return (ResearchOrganization[]) executor.execute(endpoint, new IntegrationHubCoppaTask(
            PoOperation.GET_RESEARCH_ORGANIZATIONS_BY_PLAYER_IDS, ids
        ));
    }

    ////// PO INTERACTIONS

    public Organization[] searchOrganizations(Organization criteria) {
        return (Organization[]) executor.execute(endpoint, new IntegrationHubCoppaTask(
            PoOperation.ORGANIZATION_SEARCH, criteria
        ));
    }

    public Organization getOrganization(gov.nih.nci.coppa.po.Id id) {
        return (Organization) executor.execute(endpoint, new IntegrationHubCoppaTask(
            PoOperation.GET_ORGANIZATION, id
        ));
    }

    public ResearchOrganization[] getResearchOrganizations(gov.nih.nci.coppa.po.Id[] ids) {
        return (ResearchOrganization[]) executor.execute(endpoint, new IntegrationHubCoppaTask(
            PoOperation.GET_RESEARCH_ORGANIZATIONS, (Object[]) ids
        ));
    }

    public HealthCareFacility[] getHealthCareFacilities(Id[] ids) {
        return (HealthCareFacility[]) executor.execute(endpoint, new IntegrationHubCoppaTask(
            PoOperation.GET_HEALTH_CARE_FACILITIES, (Object[]) ids
        ));
    }

    public HealthCareFacility[] getHealthCareFacilitiesByPlayerIds(Id[] ids) {
        return (HealthCareFacility[]) executor.execute(endpoint, new IntegrationHubCoppaTask(
            PoOperation.GET_HEALTH_CARE_FACILITIES_BY_PLAYER_IDS, (Object[]) ids
        ));
    }

    ////// CONFIGURATION

    public void setExecutor(IntegrationHubExecutor executor) {
        this.executor = executor;
    }
}
