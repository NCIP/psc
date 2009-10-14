package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.coppa.po.Organization;
import gov.nih.nci.coppa.po.ResearchOrganization;
import org.iso._21090.II;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class CoppaProviderHelper {
    public static Site pscSite(Organization organization) {
        Site site = new Site();
        site.setName(organization.getName().getPart().get(0).getValue());
        site.setAssignedIdentifier(organization.getIdentifier().getExtension());
        return site;
    }

    public static List<Site> pscSites(Organization... organizations) {
        List<Site> sites = new ArrayList<Site>();
        for (Organization org : organizations) {
            sites.add(pscSite(org));
        }
        return sites;
    }

    public static II[] getIds(ResearchOrganization... rs) {
        List<II> ids = new ArrayList<II>();
        for (ResearchOrganization r : rs) {
            II ii = getId(r);
            ids.add(ii);
        }
        return ids.toArray(new II[0]);
    }

    public static II[] getIds(Organization... os) {
        List<II> ids = new ArrayList<II>();
        for (Organization r : os) {
            II ii = getId(r);
            ids.add(ii);
        }
        return ids.toArray(new II[0]);
    }

    public static II[] getPlayerIds(ResearchOrganization[] researchOrgs) {
        List<II> iis = new ArrayList<II>();
        for(ResearchOrganization r : researchOrgs) {
            II ii = getPlayerId(r);
            iis.add(ii);
        }
        return iis.toArray(new II[0]);
    }

    public static II getId(ResearchOrganization r) {
        return r.getIdentifier().getItem().get(0);
    }

    public static II getId(Organization o) {
        return o.getIdentifier();
    }

    public static II getPlayerId(ResearchOrganization r) {
        return r.getPlayerIdentifier();
    }

    public static <T extends II> T[] tranformIds(Class<T> clazz, II[] iis) {
        List<T> ids = new ArrayList<T>();
        for (II ii : iis) {
            ids.add(tranformId(clazz, ii));
        }
        return (T[]) ids.toArray((T[]) Array.newInstance(clazz, 0));
    }

    public static <T extends II> T tranformId(Class<T> clazz, II ii) {
        try {
            T obj = clazz.newInstance();
            obj.setExtension(ii.getExtension());
            obj.setRoot(ii.getRoot());
            return obj;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}