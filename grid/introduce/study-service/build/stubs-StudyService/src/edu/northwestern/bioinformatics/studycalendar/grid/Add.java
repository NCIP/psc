/**
 * Add.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Apr 28, 2006 (12:42:00 EDT) WSDL2Java emitter.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

public class Add  implements java.io.Serializable {
    private edu.northwestern.bioinformatics.studycalendar.grid.Epoch[] epoch;
    private edu.northwestern.bioinformatics.studycalendar.grid.StudySegment[] studySegment;
    private edu.northwestern.bioinformatics.studycalendar.grid.Period[] period;
    private edu.northwestern.bioinformatics.studycalendar.grid.PlannedActivity[] plannedActivity;
    private java.math.BigInteger index;  // attribute
    private java.lang.String id;  // attribute

    public Add() {
    }

    public Add(
           edu.northwestern.bioinformatics.studycalendar.grid.Epoch[] epoch,
           java.lang.String id,
           java.math.BigInteger index,
           edu.northwestern.bioinformatics.studycalendar.grid.Period[] period,
           edu.northwestern.bioinformatics.studycalendar.grid.PlannedActivity[] plannedActivity,
           edu.northwestern.bioinformatics.studycalendar.grid.StudySegment[] studySegment) {
           this.epoch = epoch;
           this.studySegment = studySegment;
           this.period = period;
           this.plannedActivity = plannedActivity;
           this.index = index;
           this.id = id;
    }


    /**
     * Gets the epoch value for this Add.
     * 
     * @return epoch
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Epoch[] getEpoch() {
        return epoch;
    }


    /**
     * Sets the epoch value for this Add.
     * 
     * @param epoch
     */
    public void setEpoch(edu.northwestern.bioinformatics.studycalendar.grid.Epoch[] epoch) {
        this.epoch = epoch;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.Epoch getEpoch(int i) {
        return this.epoch[i];
    }

    public void setEpoch(int i, edu.northwestern.bioinformatics.studycalendar.grid.Epoch _value) {
        this.epoch[i] = _value;
    }


    /**
     * Gets the studySegment value for this Add.
     * 
     * @return studySegment
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.StudySegment[] getStudySegment() {
        return studySegment;
    }


    /**
     * Sets the studySegment value for this Add.
     * 
     * @param studySegment
     */
    public void setStudySegment(edu.northwestern.bioinformatics.studycalendar.grid.StudySegment[] studySegment) {
        this.studySegment = studySegment;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.StudySegment getStudySegment(int i) {
        return this.studySegment[i];
    }

    public void setStudySegment(int i, edu.northwestern.bioinformatics.studycalendar.grid.StudySegment _value) {
        this.studySegment[i] = _value;
    }


    /**
     * Gets the period value for this Add.
     * 
     * @return period
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Period[] getPeriod() {
        return period;
    }


    /**
     * Sets the period value for this Add.
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
     * Gets the plannedActivity value for this Add.
     * 
     * @return plannedActivity
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.PlannedActivity[] getPlannedActivity() {
        return plannedActivity;
    }


    /**
     * Sets the plannedActivity value for this Add.
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
     * Gets the index value for this Add.
     * 
     * @return index
     */
    public java.math.BigInteger getIndex() {
        return index;
    }


    /**
     * Sets the index value for this Add.
     * 
     * @param index
     */
    public void setIndex(java.math.BigInteger index) {
        this.index = index;
    }


    /**
     * Gets the id value for this Add.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Add.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Add)) return false;
        Add other = (Add) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.epoch==null && other.getEpoch()==null) || 
             (this.epoch!=null &&
              java.util.Arrays.equals(this.epoch, other.getEpoch()))) &&
            ((this.studySegment==null && other.getStudySegment()==null) || 
             (this.studySegment!=null &&
              java.util.Arrays.equals(this.studySegment, other.getStudySegment()))) &&
            ((this.period==null && other.getPeriod()==null) || 
             (this.period!=null &&
              java.util.Arrays.equals(this.period, other.getPeriod()))) &&
            ((this.plannedActivity==null && other.getPlannedActivity()==null) || 
             (this.plannedActivity!=null &&
              java.util.Arrays.equals(this.plannedActivity, other.getPlannedActivity()))) &&
            ((this.index==null && other.getIndex()==null) || 
             (this.index!=null &&
              this.index.equals(other.getIndex()))) &&
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
        if (getEpoch() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getEpoch());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getEpoch(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getStudySegment() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getStudySegment());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getStudySegment(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
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
        if (getIndex() != null) {
            _hashCode += getIndex().hashCode();
        }
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Add.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Add"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("index");
        attrField.setXmlName(new javax.xml.namespace.QName("", "index"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("id");
        attrField.setXmlName(new javax.xml.namespace.QName("", "id"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("epoch");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "epoch"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Epoch"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("studySegment");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "study-segment"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "StudySegment"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("period");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "period"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Period"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
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
