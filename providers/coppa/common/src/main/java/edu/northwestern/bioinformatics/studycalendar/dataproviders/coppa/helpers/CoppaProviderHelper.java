/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.CoppaAccessor;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.coppa.po.Organization;
import gov.nih.nci.coppa.po.ResearchOrganization;
import org.iso._21090.II;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CoppaProviderHelper {
    public static final String ACCESSOR_SERVICE = CoppaAccessor.class.getName();
    
    // #1172 : Added for COPPA 3.2
    public static final String STUDY_PROTOCOL_ROOT = "2.16.840.1.113883.3.26.4.3";

    public static CoppaAccessor getCoppaAccessor(BundleContext bundleContext) {
        ServiceReference sr = bundleContext.getServiceReference(ACCESSOR_SERVICE);
        if (sr == null) {
            throw new StudyCalendarSystemException("Cannot provide studies; no %s available", ACCESSOR_SERVICE);
        }
        return (CoppaAccessor) bundleContext.getService(sr);
    }

    public static Site pscSite(Organization organization) {
        Site site = new Site();
        site.setName(organization.getName().getPart().get(0).getValue());
        site.setAssignedIdentifier(organization.getIdentifier().getExtension());
        return site;
    }

    public static List<Site> pscSites(Organization... organizations) {
        List<Site> sites = new ArrayList<Site>();
        if (organizations != null) {
            for (Organization org : organizations) {
                sites.add(pscSite(org));
            }
        }
        return sites;
    }

    public static II[] getIds(ResearchOrganization... rs) {
        List<II> ids = new ArrayList<II>();
        if (rs != null) {
            for (ResearchOrganization r : rs) {
                II ii = getId(r);
                ids.add(ii);
            }
        }
        return ids.toArray(new II[0]);
    }

    public static II[] getIds(Organization... os) {
        List<II> ids = new ArrayList<II>();
        if (os != null) {
            for (Organization r : os) {
                II ii = getId(r);
                ids.add(ii);
            }
        }
        return ids.toArray(new II[0]);
    }

    public static II[] getPlayerIds(Object[] roles) {
        List<II> iis = new ArrayList<II>();
        if (roles != null) {
            for(Object r : roles) {
                II ii = getPlayerId(r);
                iis.add(ii);
            }
        }        
        return iis.toArray(new II[0]);
    }

    public static II getId(ResearchOrganization r) {
        return r.getIdentifier().getItem().get(0);
    }

    public static II getId(Organization o) {
        return o.getIdentifier();
    }

    public static II getPlayerId(Object r) {
        try {
            Method m = r.getClass().getMethod("getPlayerIdentifier");
            return (II) m.invoke(r);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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