/**
 * RetrieveStudyByAssignedIdentifierRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Apr 28, 2006 (12:42:00 EDT) WSDL2Java emitter.
 */

package edu.northwestern.bioinformatics.studycalendar.grid.stubs;

public class RetrieveStudyByAssignedIdentifierRequest  implements java.io.Serializable {
    private java.lang.String assignedIdentifier;

    public RetrieveStudyByAssignedIdentifierRequest() {
    }

    public RetrieveStudyByAssignedIdentifierRequest(
           java.lang.String assignedIdentifier) {
           this.assignedIdentifier = assignedIdentifier;
    }


    /**
     * Gets the assignedIdentifier value for this RetrieveStudyByAssignedIdentifierRequest.
     * 
     * @return assignedIdentifier
     */
    public java.lang.String getAssignedIdentifier() {
        return assignedIdentifier;
    }


    /**
     * Sets the assignedIdentifier value for this RetrieveStudyByAssignedIdentifierRequest.
     * 
     * @param assignedIdentifier
     */
    public void setAssignedIdentifier(java.lang.String assignedIdentifier) {
        this.assignedIdentifier = assignedIdentifier;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RetrieveStudyByAssignedIdentifierRequest)) return false;
        RetrieveStudyByAssignedIdentifierRequest other = (RetrieveStudyByAssignedIdentifierRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.assignedIdentifier==null && other.getAssignedIdentifier()==null) || 
             (this.assignedIdentifier!=null &&
              this.assignedIdentifier.equals(other.getAssignedIdentifier())));
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
        if (getAssignedIdentifier() != null) {
            _hashCode += getAssignedIdentifier().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RetrieveStudyByAssignedIdentifierRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://grid.studycalendar.bioinformatics.northwestern.edu/StudyService", ">RetrieveStudyByAssignedIdentifierRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("assignedIdentifier");
        elemField.setXmlName(new javax.xml.namespace.QName("http://grid.studycalendar.bioinformatics.northwestern.edu/StudyService", "assignedIdentifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
