package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import static edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.po.HealthCareFacility;
import gov.nih.nci.coppa.po.Organization;
import gov.nih.nci.coppa.po.ResearchOrganization;
import gov.nih.nci.coppa.services.pa.Id;
import static org.apache.commons.collections.CollectionUtils.collect;
import org.apache.commons.collections.Transformer;
import static org.apache.commons.lang.ArrayUtils.addAll;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import org.iso._21090.II;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import static java.util.Collections.EMPTY_LIST;

public class CoppaStudySiteProvider implements edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider, RefreshableProvider {
    private BundleContext bundleContext;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public CoppaStudySiteProvider(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public List<List<StudySite>> getAssociatedSites(List<Study> studies) {
        List<List<StudySite>> results = new ArrayList<List<StudySite>>(studies.size());

        for (Study study : studies) {
            String ext = study.getSecondaryIdentifierValue(CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE);
            if (ext != null) {
                Id id = new Id();
                id.setExtension(ext);
                gov.nih.nci.coppa.services.pa.StudySite[] studySites = getCoppaAccessor(bundleContext).searchStudySitesByStudyProtocolId(id);

                if (studySites == null || studySites.length == 0) {
                    results.add(EMPTY_LIST);
                } else {
                    Map<String, II[]> roles = buildOrganizationRolesMap(studySites);

                    gov.nih.nci.coppa.po.Id[] researchOrgIds = tranformIds(gov.nih.nci.coppa.po.Id.class, roles.get("RO"));
                    ResearchOrganization[] researchOrgs = getCoppaAccessor(bundleContext).getResearchOrganizations(researchOrgIds);

                    gov.nih.nci.coppa.po.Id[] hcFacilityIds = tranformIds(gov.nih.nci.coppa.po.Id.class, roles.get("HCF"));
                    HealthCareFacility[] hcFacilities= getCoppaAccessor(bundleContext).getHealthCareFacilities(hcFacilityIds);

                    II[] orgIIsForResearchOrgs = getPlayerIds(researchOrgs);
                    II[] orgIIsForhcFacilities = getPlayerIds(hcFacilities);

                    II[] orgIIs = (II[]) addAll(orgIIsForResearchOrgs , orgIIsForhcFacilities);

                    gov.nih.nci.coppa.po.Id[] orgIDs = tranformIds(gov.nih.nci.coppa.po.Id.class, orgIIs);
                    Organization[] organizations = getOrganizationsById(orgIDs);

                    List<Site> sites = pscSites(organizations);

                    results.add(buildStudySites(sites));
                }
            } else {
                results.add(EMPTY_LIST);
            }
        }

        return results;
    }

    private Map<String, II[]> buildOrganizationRolesMap(gov.nih.nci.coppa.services.pa.StudySite[] studySites) {
        List<II> hcFacilities = new ArrayList<II>();
        List<II> researchOrgs = new ArrayList<II>();

        for (gov.nih.nci.coppa.services.pa.StudySite s : studySites) {
            if (s.getHealthcareFacility() != null) {
                hcFacilities.add(s.getHealthcareFacility());
            } else if (s.getResearchOrganization() != null) {
                researchOrgs.add(s.getResearchOrganization());
            }
        }

        Map<String, II[]> roles = new HashMap<String, II[]>();
        roles.put("HCF", hcFacilities.toArray(new II[0]));
        roles.put("RO", researchOrgs.toArray(new II[0]));
        return roles;
    }

    private Organization[] getOrganizationsById(gov.nih.nci.coppa.po.Id[]  ids) {
        List<Organization> orgs = new ArrayList<Organization>();
        for (gov.nih.nci.coppa.po.Id id : ids) {
            Organization org = getCoppaAccessor(bundleContext).getOrganization(id);
            if (org != null) {
                orgs.add(org);
            }
        }
        return orgs.toArray(new Organization[0]);
    }

    private List<StudySite> buildStudySites(List<Site> sites) {
        List<StudySite> studySites = new ArrayList<StudySite>();
        for (Site site : sites) {
            StudySite ss = new StudySite();
            ss.setSite(site);
            studySites.add(ss);
        }
        return studySites;
    }

    public List<List<StudySite>> getAssociatedStudies(List<Site> sites) {
        if (sites == null) {
            return EMPTY_LIST;
        }

        List<List<StudySite>> results = new ArrayList<List<StudySite>>(sites.size());

        for (Site s : sites) {

            List<StudySite> provided = null;

            if (isNotBlank(s.getAssignedIdentifier())) {
                provided = new ArrayList<StudySite>();

                Set<String> associatedStudyIds = new HashSet<String>();

                HealthCareFacility hcf = getHealthCareFacilityByPlayerId(s.getAssignedIdentifier());
                if (hcf != null) {
                    II hcfII = detectHealthCareFacilityIdentifier(hcf);

                    if (hcfII!= null && hcfII.getExtension() != null) {
                        gov.nih.nci.coppa.services.pa.StudySite param = new gov.nih.nci.coppa.services.pa.StudySite();
                        param.setResearchOrganization(hcfII);
                        gov.nih.nci.coppa.services.pa.StudySite[] raw = searchStudySitesByStudySite(param);

                        associatedStudyIds.addAll(collectStudyProtocolIdentifiers(raw));
                    }
                }

                ResearchOrganization ro = getResearchOrganizationByPlayerId(s.getAssignedIdentifier());
                if (ro != null) {
                    II roII = detectResearchOrganizationIdentifier(ro);

                    if (roII != null && roII.getExtension() != null) {
                        gov.nih.nci.coppa.services.pa.StudySite param = new gov.nih.nci.coppa.services.pa.StudySite();
                        param.setResearchOrganization(roII);
                        gov.nih.nci.coppa.services.pa.StudySite[] raw = searchStudySitesByStudySite(param);

                        associatedStudyIds.addAll(collectStudyProtocolIdentifiers(raw));
                    }
                }


                for (String protocolId : associatedStudyIds) {
                    StudySecondaryIdentifier ssid = new StudySecondaryIdentifier();
                    ssid.setType(CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE);
                    ssid.setValue(protocolId);

                    Study study = new Study();
                    study.addSecondaryIdentifier(ssid);

                    StudySite transformed = new StudySite(study, null);
                    provided.add(transformed);
                }
            }
            results.add(provided);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private Set<String> collectStudyProtocolIdentifiers(gov.nih.nci.coppa.services.pa.StudySite[] raw) {
        if (raw == null || raw.length <= 0){
            return Collections.EMPTY_SET;
        }

        return new HashSet(collect(Arrays.asList(raw), new Transformer() {
            public Object transform(Object o) {
                gov.nih.nci.coppa.services.pa.StudySite s = (gov.nih.nci.coppa.services.pa.StudySite) o;
                if (s != null && s.getStudyProtocolIdentifier() != null) {
                    return s.getStudyProtocolIdentifier().getExtension();
                }
                return null;
            }
        }));
    }

    private HealthCareFacility getHealthCareFacilityByPlayerId(String playerId) {
        gov.nih.nci.coppa.po.Id orgId = new gov.nih.nci.coppa.po.Id();
        orgId.setExtension(playerId);
        orgId.setRoot("2.16.840.1.113883.3.26.4.2");

        HealthCareFacility[] hcfs = getCoppaAccessor(bundleContext).getHealthCareFacilitiesByPlayerIds(new gov.nih.nci.coppa.po.Id[]{orgId});

        if (hcfs == null || hcfs[0] == null) {
            return null;
        }

        return hcfs[0];
    }

    private ResearchOrganization getResearchOrganizationByPlayerId(String playerId) {
        gov.nih.nci.coppa.po.Id orgId = new gov.nih.nci.coppa.po.Id();
        orgId.setExtension(playerId);
        orgId.setRoot("2.16.840.1.113883.3.26.4.2");

        ResearchOrganization[] ros = getCoppaAccessor(bundleContext).getResearchOrganizationsByPlayerIds(new gov.nih.nci.coppa.po.Id[]{orgId});

        if (ros == null || ros[0] == null) {
            return null;
        }

        return ros[0];
    }

    private gov.nih.nci.coppa.services.pa.StudySite[] searchStudySitesByStudySite(gov.nih.nci.coppa.services.pa.StudySite param) {
        LimitOffset l = new LimitOffset();
        l.setLimit(250);

        return getCoppaAccessor(bundleContext).searchStudySitesByStudySite(param, l);
    }

    private II detectResearchOrganizationIdentifier(ResearchOrganization ro) {
        if (ro != null) {
            for (II ii : ro.getIdentifier().getItem()) {
                if (ii.getIdentifierName().equalsIgnoreCase("NCI Research Organization identifier")) {
                    return ii;
                }
            }
        }
        return null;
    }

    private II detectHealthCareFacilityIdentifier(HealthCareFacility hcf) {
        if (hcf != null) {
            for (II ii : hcf.getIdentifier().getItem()) {
                if (ii.getIdentifierName().equalsIgnoreCase("NCI Health Care Facility identifier")) {
                    return ii;
                }
            }
        }
        return null;
    }

    public String providerToken() {
        return CoppaProviderConstants.PROVIDER_TOKEN;
    }

    public Integer getRefreshInterval() {
        return 15;
    }
}
