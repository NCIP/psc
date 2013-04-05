/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
public class DefaultCrumb implements Crumb {
    private CrumbSource parent;
    private String name;

    public DefaultCrumb() { }

    public DefaultCrumb(String name) {
        this.name = name;
    }

    public CrumbSource getParent() {
        return parent;
    }

    public void setParent(CrumbSource parent) {
        this.parent = parent;
    }

    public String getName(DomainContext context) {
        return name;
    }                                                       

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getParameters(DomainContext context) {
        return null;
    }

    /**
     * Creates a parameter map by combining the elements of the given array such that
     * the even elements are keys and the odd elements values.
     * @param elements
     * @return
     */
    protected Map<String, String> createParameters(String... elements) {
        if (elements.length % 2 != 0) throw new IllegalArgumentException("Must provide an even number of entries");
        Map<String, String> params = new HashMap<String, String>();
        for (int i = 0 ; i < elements.length ; i = i+2) {
            params.put(elements[i], elements[i+1]);
        }
        return params;
    }
}
