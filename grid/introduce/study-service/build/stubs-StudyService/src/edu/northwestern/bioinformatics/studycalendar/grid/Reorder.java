/**
 * Reorder.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Apr 28, 2006 (12:42:00 EDT) WSDL2Java emitter.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

public class Reorder  implements java.io.Serializable {
    private java.lang.String id;  // attribute
    private java.lang.String childId;  // attribute
    private java.math.BigInteger oldIndex;  // attribute
    private java.math.BigInteger newIndex;  // attribute

    public Reorder() {
    }

    public Reorder(
           java.lang.String childId,
           java.lang.String id,
           java.math.BigInteger newIndex,
           java.math.BigInteger oldIndex) {
           this.id = id;
           this.childId = childId;
           this.oldIndex = oldIndex;
           this.newIndex = newIndex;
    }


    /**
     * Gets the id value for this Reorder.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Reorder.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the childId value for this Reorder.
     * 
     * @return childId
     */
    public java.lang.String getChildId() {
        return childId;
    }


    /**
     * Sets the childId value for this Reorder.
     * 
     * @param childId
     */
    public void setChildId(java.lang.String childId) {
        this.childId = childId;
    }


    /**
     * Gets the oldIndex value for this Reorder.
     * 
     * @return oldIndex
     */
    public java.math.BigInteger getOldIndex() {
        return oldIndex;
    }


    /**
     * Sets the oldIndex value for this Reorder.
     * 
     * @param oldIndex
     */
    public void setOldIndex(java.math.BigInteger oldIndex) {
        this.oldIndex = oldIndex;
    }


    /**
     * Gets the newIndex value for this Reorder.
     * 
     * @return newIndex
     */
    public java.math.BigInteger getNewIndex() {
        return newIndex;
    }


    /**
     * Sets the newIndex value for this Reorder.
     * 
     * @param newIndex
     */
    public void setNewIndex(java.math.BigInteger newIndex) {
        this.newIndex = newIndex;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Reorder)) return false;
        Reorder other = (Reorder) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.childId==null && other.getChildId()==null) || 
             (this.childId!=null &&
              this.childId.equals(other.getChildId()))) &&
            ((this.oldIndex==null && other.getOldIndex()==null) || 
             (this.oldIndex!=null &&
              this.oldIndex.equals(other.getOldIndex()))) &&
            ((this.newIndex==null && other.getNewIndex()==null) || 
             (this.newIndex!=null &&
              this.newIndex.equals(other.getNewIndex())));
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
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getChildId() != null) {
            _hashCode += getChildId().hashCode();
        }
        if (getOldIndex() != null) {
            _hashCode += getOldIndex().hashCode();
        }
        if (getNewIndex() != null) {
            _hashCode += getNewIndex().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Reorder.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Reorder"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("id");
        attrField.setXmlName(new javax.xml.namespace.QName("", "id"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("childId");
        attrField.setXmlName(new javax.xml.namespace.QName("", "child-id"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("oldIndex");
        attrField.setXmlName(new javax.xml.namespace.QName("", "old-index"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("newIndex");
        attrField.setXmlName(new javax.xml.namespace.QName("", "new-index"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
        typeDesc.addFieldDesc(attrField);
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
