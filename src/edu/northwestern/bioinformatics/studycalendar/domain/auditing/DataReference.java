package edu.northwestern.bioinformatics.studycalendar.domain.auditing;

import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public class DataReference {
    private String className;
    private Integer id;

    public DataReference() { }

    public DataReference(Class<?> clazz, Integer id) {
        this(clazz.getName(), id);
    }

    public DataReference(String className, Integer id) {
        this.className = className;
        this.id = id;
    }

    public static DataReference create(DomainObject target) {
        return new DataReference(target.getClass().getName(), target.getId());
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
