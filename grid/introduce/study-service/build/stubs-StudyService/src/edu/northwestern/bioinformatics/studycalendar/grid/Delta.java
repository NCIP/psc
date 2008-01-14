/**
 * Delta.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Apr 28, 2006 (12:42:00 EDT) WSDL2Java emitter.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

public class Delta  implements java.io.Serializable {
    private edu.northwestern.bioinformatics.studycalendar.grid.Add[] add;
    private edu.northwestern.bioinformatics.studycalendar.grid.Remove[] remove;
    private edu.northwestern.bioinformatics.studycalendar.grid.Reorder[] reorder;
    private edu.northwestern.bioinformatics.studycalendar.grid.PropertyChange[] propertyChange;
    private java.lang.String id;  // attribute
    private java.lang.String nodeId;  // attribute

    public Delta() {
    }

    public Delta(
           edu.northwestern.bioinformatics.studycalendar.grid.Add[] add,
           java.lang.String id,
           java.lang.String nodeId,
           edu.northwestern.bioinformatics.studycalendar.grid.PropertyChange[] propertyChange,
           edu.northwestern.bioinformatics.studycalendar.grid.Remove[] remove,
           edu.northwestern.bioinformatics.studycalendar.grid.Reorder[] reorder) {
           this.add = add;
           this.remove = remove;
           this.reorder = reorder;
           this.propertyChange = propertyChange;
           this.id = id;
           this.nodeId = nodeId;
    }


    /**
     * Gets the add value for this Delta.
     * 
     * @return add
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Add[] getAdd() {
        return add;
    }


    /**
     * Sets the add value for this Delta.
     * 
     * @param add
     */
    public void setAdd(edu.northwestern.bioinformatics.studycalendar.grid.Add[] add) {
        this.add = add;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.Add getAdd(int i) {
        return this.add[i];
    }

    public void setAdd(int i, edu.northwestern.bioinformatics.studycalendar.grid.Add _value) {
        this.add[i] = _value;
    }


    /**
     * Gets the remove value for this Delta.
     * 
     * @return remove
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Remove[] getRemove() {
        return remove;
    }


    /**
     * Sets the remove value for this Delta.
     * 
     * @param remove
     */
    public void setRemove(edu.northwestern.bioinformatics.studycalendar.grid.Remove[] remove) {
        this.remove = remove;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.Remove getRemove(int i) {
        return this.remove[i];
    }

    public void setRemove(int i, edu.northwestern.bioinformatics.studycalendar.grid.Remove _value) {
        this.remove[i] = _value;
    }


    /**
     * Gets the reorder value for this Delta.
     * 
     * @return reorder
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Reorder[] getReorder() {
        return reorder;
    }


    /**
     * Sets the reorder value for this Delta.
     * 
     * @param reorder
     */
    public void setReorder(edu.northwestern.bioinformatics.studycalendar.grid.Reorder[] reorder) {
        this.reorder = reorder;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.Reorder getReorder(int i) {
        return this.reorder[i];
    }

    public void setReorder(int i, edu.northwestern.bioinformatics.studycalendar.grid.Reorder _value) {
        this.reorder[i] = _value;
    }


    /**
     * Gets the propertyChange value for this Delta.
     * 
     * @return propertyChange
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.PropertyChange[] getPropertyChange() {
        return propertyChange;
    }


    /**
     * Sets the propertyChange value for this Delta.
     * 
     * @param propertyChange
     */
    public void setPropertyChange(edu.northwestern.bioinformatics.studycalendar.grid.PropertyChange[] propertyChange) {
        this.propertyChange = propertyChange;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.PropertyChange getPropertyChange(int i) {
        return this.propertyChange[i];
    }

    public void setPropertyChange(int i, edu.northwestern.bioinformatics.studycalendar.grid.PropertyChange _value) {
        this.propertyChange[i] = _value;
    }


    /**
     * Gets the id value for this Delta.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Delta.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the nodeId value for this Delta.
     * 
     * @return nodeId
     */
    public java.lang.String getNodeId() {
        return nodeId;
    }


    /**
     * Sets the nodeId value for this Delta.
     * 
     * @param nodeId
     */
    public void setNodeId(java.lang.String nodeId) {
        this.nodeId = nodeId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Delta)) return false;
        Delta other = (Delta) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.add==null && other.getAdd()==null) || 
             (this.add!=null &&
              java.util.Arrays.equals(this.add, other.getAdd()))) &&
            ((this.remove==null && other.getRemove()==null) || 
             (this.remove!=null &&
              java.util.Arrays.equals(this.remove, other.getRemove()))) &&
            ((this.reorder==null && other.getReorder()==null) || 
             (this.reorder!=null &&
              java.util.Arrays.equals(this.reorder, other.getReorder()))) &&
            ((this.propertyChange==null && other.getPropertyChange()==null) || 
             (this.propertyChange!=null &&
              java.util.Arrays.equals(this.propertyChange, other.getPropertyChange()))) &&
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.nodeId==null && other.getNodeId()==null) || 
             (this.nodeId!=null &&
              this.nodeId.equals(other.getNodeId())));
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
        if (getAdd() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAdd());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAdd(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRemove() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRemove());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRemove(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getReorder() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getReorder());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getReorder(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getPropertyChange() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPropertyChange());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPropertyChange(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getNodeId() != null) {
            _hashCode += getNodeId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Delta.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Delta"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("id");
        attrField.setXmlName(new javax.xml.namespace.QName("", "id"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("nodeId");
        attrField.setXmlName(new javax.xml.namespace.QName("", "node-id"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("add");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "add"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Add"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("remove");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "remove"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Remove"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reorder");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "reorder"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Reorder"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("propertyChange");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "property-change"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "PropertyChange"));
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
