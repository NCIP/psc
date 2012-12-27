/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.felixcm;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.cabig.ctms.domain.CodedEnum;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Proxy;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * A representation of a single KV pair for an OSGi configuration.
 *
 * @author Rhett Sutphin
 * @see edu.northwestern.bioinformatics.studycalendar.osgi.felixcm.internal.PscFelixPersistenceManager
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
@Entity
@Proxy(lazy = false)
@Table(name = "osgi_cm_properties")
@GenericGenerator(name = "id-generator", strategy = "native",
    parameters = {
        @Parameter(name = "sequence", value = "seq_osgi_cm_properties_id")
    }
)
public class OsgiConfigurationProperty {
    private Integer id, version;
    private String servicePid;
    private String name;
    private CollectionType collectionType;
    private Type type;
    private List<String> rawValues;

    public OsgiConfigurationProperty() {
        rawValues = new ArrayList<String>();
    }

    ////// LOGIC

    @Transient
    public Object getValue() {
        switch (getCollectionType()) {
            case SINGLE: return getSingleValue();
            case VECTOR: return getVectorValue();
            case ARRAY:  return getArrayValue();
        }
        throw new UnsupportedOperationException("Unsupported collection type: " + getCollectionType());
    }

    @Transient
    private Object getSingleValue() {
        return getType().convert(getRawValues().get(0));
    }

    @Transient
    private Vector<?> getVectorValue() {
        Vector<Object> result = new Vector<Object>();
        for (String rawValue : getRawValues()) {
            result.add(getType().convert(rawValue));
        }
        return result;
    }

    @Transient
    private Object getArrayValue() {
        Object result = getType().createArray(getRawValues().size());
        for (int i = 0; i < getRawValues().size(); i++) {
            String rawValue = getRawValues().get(i);
            Array.set(result, i, getType().convert(rawValue));
        }
        return result;
    }

    public void setValue(Object value) {
        if (value.getClass().isArray()) {
            setArrayValue(value);
        } else if (value instanceof List) {
            setVectorValue((List) value);
        } else {
            setSingleValue(value.toString(), Type.fromJavaClass(value.getClass()));
        }
    }

    private void setArrayValue(Object value) {
        setCollectionType(CollectionType.ARRAY);
        setType(Type.fromJavaClass(value.getClass().getComponentType()));
        getRawValues().clear();
        int len = Array.getLength(value);
        for (int i = 0; i < len; i++) {
            getRawValues().add(Array.get(value, i).toString());
        }
    }

    private void setVectorValue(List list) {
        setCollectionType(CollectionType.VECTOR);
        if (list.isEmpty()) {
            setType(Type.STRING); // default type
        } else {
            setType(Type.fromJavaClass(list.get(0).getClass()));
            getRawValues().clear();
            for (Object o : list) {
                getRawValues().add(o.toString());
            }
        }
    }

    private void setSingleValue(String serializedValue, Type type) {
        setCollectionType(CollectionType.SINGLE);
        setType(type);
        getRawValues().clear();
        getRawValues().add(serializedValue);
    }

    ////// BOUND PROPERTIES

    @Id @GeneratedValue(generator = "id-generator")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getServicePid() {
        return servicePid;
    }

    public void setServicePid(String servicePid) {
        this.servicePid = servicePid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "collection_kind")
    @Enumerated(EnumType.STRING)
    public CollectionType getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(CollectionType collectionType) {
        this.collectionType = collectionType;
    }

    @Column(name = "kind")
    @org.hibernate.annotations.Type(
        type = "edu.northwestern.bioinformatics.studycalendar.domain.tools.hibernate.StringCodedEnumType",
        parameters = {
            @Parameter(
                name = "enumClass",
                value = "edu.northwestern.bioinformatics.studycalendar.osgi.felixcm.OsgiConfigurationProperty$Type"
            )
        })
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "value")
    @JoinTable(name = "osgi_cm_property_values", joinColumns = @JoinColumn(name = "property_id", nullable = false))
    @IndexColumn(name = "list_index", nullable = false)
    @Cascade({CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<String> getRawValues() {
        return rawValues;
    }

    public void setRawValues(List<String> rawValues) {
        this.rawValues = rawValues;
    }

    public static OsgiConfigurationProperty create(String pid, String name, Object value) {
        OsgiConfigurationProperty property = new OsgiConfigurationProperty();
        property.setServicePid(pid);
        property.setName(name);
        property.setValue(value);
        return property;
    }

    ////// ENUMERATIONS

    public enum CollectionType {
        SINGLE,
        ARRAY,
        VECTOR
    }

    public enum Type implements CodedEnum<String> {
        STRING(String.class) {
            @Override public Object convert(String serialized) { return serialized; }
        },
        INTEGER(Integer.class),
        LONG(Long.class),
        FLOAT(Float.class),
        DOUBLE(Double.class),
        BYTE(Byte.class),
        SHORT(Short.class),
        CHARACTER(Character.class),
        BOOLEAN(Boolean.class),

        PRIMITIVE_LONG(Long.TYPE, Long.class),
        PRIMITIVE_INT(Integer.TYPE, Integer.class),
        PRIMITIVE_SHORT(Short.TYPE, Short.class),
        PRIMITIVE_CHAR(Character.TYPE, Character.class),
        PRIMITIVE_BYTE(Byte.TYPE, Byte.class),
        PRIMITIVE_DOUBLE(Double.TYPE, Double.class),
        PRIMITIVE_FLOAT(Float.TYPE, Float.class),
        PRIMITIVE_BOOLEAN(Boolean.TYPE, Boolean.class);

        private Class<?> javaClass, boxClass;

        private Type(Class<?> javaClass) {
            this(javaClass, javaClass);
        }

        private Type(Class<?> javaClass, Class<?> boxClass) {
            this.javaClass = javaClass;
            this.boxClass = boxClass;
        }

        public Class<?> javaClass() {
            return javaClass;
        }

        public static Type fromJavaClass(Class<?> aClass) {
            for (Type type : values()) {
                if (type.javaClass().equals(aClass)) {
                    return type;
                }
            }
            return null;
        }

        public String typeName() {
            if (javaClass().isPrimitive()) {
                return name().replace("PRIMITIVE_", "").toLowerCase();
            } else {
                StringBuilder sb = new StringBuilder(name().toLowerCase());
                sb.setCharAt(0, name().charAt(0));
                return sb.toString();
            }
        }

        public static Type fromTypeName(String typeName) {
            try {
                if (Character.isLowerCase(typeName.charAt(0))) {
                    return valueOf("PRIMITIVE_" + typeName.toUpperCase());
                } else {
                    return valueOf(typeName.toUpperCase());
                }
            } catch (IllegalArgumentException iae) {
                return null;
            }
        }

        public Object createArray(int length) {
            return Array.newInstance(javaClass(), length);
        }

        public Object convert(String serialized) {
            if (boxClass.equals(Character.class)) {
                return serialized.charAt(0);
            }

            try {
                Constructor converter = boxClass.getConstructor(String.class);
                return converter.newInstance(serialized);
            } catch (NoSuchMethodException e) {
                throw new StudyCalendarError("Missing required constructor on " + boxClass.getName());
            } catch (InvocationTargetException e) {
                throw new StudyCalendarSystemException(
                    "Converting '%s' using type %s failed", e, serialized, this.name());
            } catch (IllegalAccessException e) {
                throw new StudyCalendarSystemException(
                    "Converting '%s' using type %s failed", e, serialized, this.name());
            } catch (InstantiationException e) {
                throw new StudyCalendarSystemException(
                    "Converting '%s' using type %s failed", e, serialized, this.name());
            }
        }

        public static Type getByCode(String code) {
            return fromTypeName(code);
        }

        public String getCode() {
            return typeName();
        }

        public String getDisplayName() {
            return typeName();
        }
    }
}
