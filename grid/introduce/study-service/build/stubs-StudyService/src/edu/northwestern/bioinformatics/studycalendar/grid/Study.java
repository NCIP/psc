/**
 * Study.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Apr 28, 2006 (12:42:00 EDT) WSDL2Java emitter.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

public class Study  implements java.io.Serializable {
    private edu.northwestern.bioinformatics.studycalendar.grid.PlannedCalendar plannedCalendar;
    private edu.northwestern.bioinformatics.studycalendar.grid.Amendment[] amendment;
    private java.lang.String assignedIdentifier;  // attribute
    private java.lang.String id;  // attribute

    public Study() {
    }

    public Study(
           edu.northwestern.bioinformatics.studycalendar.grid.Amendment[] amendment,
           java.lang.String assignedIdentifier,
           java.lang.String id,
           edu.northwestern.bioinformatics.studycalendar.grid.PlannedCalendar plannedCalendar) {
           this.plannedCalendar = plannedCalendar;
           this.amendment = amendment;
           this.assignedIdentifier = assignedIdentifier;
           this.id = id;
    }


    /**
     * Gets the plannedCalendar value for this Study.
     * 
     * @return plannedCalendar
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.PlannedCalendar getPlannedCalendar() {
        return plannedCalendar;
    }


    /**
     * Sets the plannedCalendar value for this Study.
     * 
     * @param plannedCalendar
     */
    public void setPlannedCalendar(edu.northwestern.bioinformatics.studycalendar.grid.PlannedCalendar plannedCalendar) {
        this.plannedCalendar = plannedCalendar;
    }


    /**
     * Gets the amendment value for this Study.
     * 
     * @return amendment
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Amendment[] getAmendment() {
        return amendment;
    }


    /**
     * Sets the amendment value for this Study.
     * 
     * @param amendment
     */
    public void setAmendment(edu.northwestern.bioinformatics.studycalendar.grid.Amendment[] amendment) {
        this.amendment = amendment;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.Amendment getAmendment(int i) {
        return this.amendment[i];
    }

    public void setAmendment(int i, edu.northwestern.bioinformatics.studycalendar.grid.Amendment _value) {
        this.amendment[i] = _value;
    }


    /**
     * Gets the assignedIdentifier value for this Study.
     * 
     * @return assignedIdentifier
     */
    public java.lang.String getAssignedIdentifier() {
        return assignedIdentifier;
    }


    /**
     * Sets the assignedIdentifier value for this Study.
     * 
     * @param assignedIdentifier
     */
    public void setAssignedIdentifier(java.lang.String assignedIdentifier) {
        this.assignedIdentifier = assignedIdentifier;
    }


    /**
     * Gets the id value for this Study.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Study.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Study)) return false;
        Study other = (Study) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.plannedCalendar==null && other.getPlannedCalendar()==null) || 
             (this.plannedCalendar!=null &&
              this.plannedCalendar.equals(other.getPlannedCalendar()))) &&
            ((this.amendment==null && other.getAmendment()==null) || 
             (this.amendment!=null &&
              java.util.Arrays.equals(this.amendment, other.getAmendment()))) &&
            ((this.assignedIdentifier==null && other.getAssignedIdentifier()==null) || 
             (this.assignedIdentifier!=null &&
              this.assignedIdentifier.equals(other.getAssignedIdentifier()))) &&
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
        if (getPlannedCalendar() != null) {
            _hashCode += getPlannedCalendar().hashCode();
        }
        if (getAmendment() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAmendment());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAmendment(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getAssignedIdentifier() != null) {
            _hashCode += getAssignedIdentifier().hashCode();
        }
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Study.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Study"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("assignedIdentifier");
        attrField.setXmlName(new javax.xml.namespace.QName("", "assigned-identifier"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("id");
        attrField.setXmlName(new javax.xml.namespace.QName("", "id"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("plannedCalendar");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "planned-calendar"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "PlannedCalendar"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("amendment");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "amendment"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Amendment"));
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
