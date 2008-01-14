/**
 * StudySegment.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Apr 28, 2006 (12:42:00 EDT) WSDL2Java emitter.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

public class StudySegment  implements java.io.Serializable {
    private edu.northwestern.bioinformatics.studycalendar.grid.Period[] period;
    private java.lang.String name;  // attribute
    private java.lang.String id;  // attribute

    public StudySegment() {
    }

    public StudySegment(
           java.lang.String id,
           java.lang.String name,
           edu.northwestern.bioinformatics.studycalendar.grid.Period[] period) {
           this.period = period;
           this.name = name;
           this.id = id;
    }


    /**
     * Gets the period value for this StudySegment.
     * 
     * @return period
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Period[] getPeriod() {
        return period;
    }


    /**
     * Sets the period value for this StudySegment.
     * 
     * @param period
     */
    public void setPeriod(edu.northwestern.bioinformatics.studycalendar.grid.Period[] period) {
        this.period = period;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.Period getPeriod(int i) {
        return this.period[i];
    }

    public void setPeriod(int i, edu.northwestern.bioinformatics.studycalendar.grid.Period _value) {
        this.period[i] = _value;
    }


    /**
     * Gets the name value for this StudySegment.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this StudySegment.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the id value for this StudySegment.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this StudySegment.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof StudySegment)) return false;
        StudySegment other = (StudySegment) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.period==null && other.getPeriod()==null) || 
             (this.period!=null &&
              java.util.Arrays.equals(this.period, other.getPeriod()))) &&
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
        if (getPeriod() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPeriod());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPeriod(), i);
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
        new org.apache.axis.description.TypeDesc(StudySegment.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "StudySegment"));
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
        elemField.setFieldName("period");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "period"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Period"));
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
