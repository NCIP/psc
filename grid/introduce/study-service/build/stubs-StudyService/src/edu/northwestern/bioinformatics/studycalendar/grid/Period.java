/**
 * Period.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Apr 28, 2006 (12:42:00 EDT) WSDL2Java emitter.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

public class Period  implements java.io.Serializable {
    private edu.northwestern.bioinformatics.studycalendar.grid.PlannedActivity[] plannedActivity;
    private java.lang.String name;  // attribute
    private java.lang.String id;  // attribute

    public Period() {
    }

    public Period(
           java.lang.String id,
           java.lang.String name,
           edu.northwestern.bioinformatics.studycalendar.grid.PlannedActivity[] plannedActivity) {
           this.plannedActivity = plannedActivity;
           this.name = name;
           this.id = id;
    }


    /**
     * Gets the plannedActivity value for this Period.
     * 
     * @return plannedActivity
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.PlannedActivity[] getPlannedActivity() {
        return plannedActivity;
    }


    /**
     * Sets the plannedActivity value for this Period.
     * 
     * @param plannedActivity
     */
    public void setPlannedActivity(edu.northwestern.bioinformatics.studycalendar.grid.PlannedActivity[] plannedActivity) {
        this.plannedActivity = plannedActivity;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.PlannedActivity getPlannedActivity(int i) {
        return this.plannedActivity[i];
    }

    public void setPlannedActivity(int i, edu.northwestern.bioinformatics.studycalendar.grid.PlannedActivity _value) {
        this.plannedActivity[i] = _value;
    }


    /**
     * Gets the name value for this Period.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this Period.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the id value for this Period.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Period.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Period)) return false;
        Period other = (Period) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.plannedActivity==null && other.getPlannedActivity()==null) || 
             (this.plannedActivity!=null &&
              java.util.Arrays.equals(this.plannedActivity, other.getPlannedActivity()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getPlannedActivity() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPlannedActivity());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPlannedActivity(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Period.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Period"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("name");
        attrField.setXmlName(new javax.xml.namespace.QName("", "name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("id");
        attrField.setXmlName(new javax.xml.namespace.QName("", "id"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("plannedActivity");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "planned-activity"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "PlannedActivity"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
