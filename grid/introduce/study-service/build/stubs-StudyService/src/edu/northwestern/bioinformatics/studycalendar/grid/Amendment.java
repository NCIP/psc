/**
 * Amendment.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Apr 28, 2006 (12:42:00 EDT) WSDL2Java emitter.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

public class Amendment  implements java.io.Serializable {
    private edu.northwestern.bioinformatics.studycalendar.grid.Delta[] plannedCalendarDelta;
    private edu.northwestern.bioinformatics.studycalendar.grid.Delta[] epochDelta;
    private edu.northwestern.bioinformatics.studycalendar.grid.Delta[] studySegmentDelta;
    private edu.northwestern.bioinformatics.studycalendar.grid.Delta[] periodDelta;
    private edu.northwestern.bioinformatics.studycalendar.grid.Delta[] plannedActivityDelta;
    private java.lang.String name;  // attribute
    private java.util.Date date;  // attribute
    private boolean mandatory;  // attribute
    private java.lang.String id;  // attribute
    private java.lang.String previousAmendmentId;  // attribute

    public Amendment() {
    }

    public Amendment(
           java.util.Date date,
           edu.northwestern.bioinformatics.studycalendar.grid.Delta[] epochDelta,
           java.lang.String id,
           boolean mandatory,
           java.lang.String name,
           edu.northwestern.bioinformatics.studycalendar.grid.Delta[] periodDelta,
           edu.northwestern.bioinformatics.studycalendar.grid.Delta[] plannedActivityDelta,
           edu.northwestern.bioinformatics.studycalendar.grid.Delta[] plannedCalendarDelta,
           java.lang.String previousAmendmentId,
           edu.northwestern.bioinformatics.studycalendar.grid.Delta[] studySegmentDelta) {
           this.plannedCalendarDelta = plannedCalendarDelta;
           this.epochDelta = epochDelta;
           this.studySegmentDelta = studySegmentDelta;
           this.periodDelta = periodDelta;
           this.plannedActivityDelta = plannedActivityDelta;
           this.name = name;
           this.date = date;
           this.mandatory = mandatory;
           this.id = id;
           this.previousAmendmentId = previousAmendmentId;
    }


    /**
     * Gets the plannedCalendarDelta value for this Amendment.
     * 
     * @return plannedCalendarDelta
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Delta[] getPlannedCalendarDelta() {
        return plannedCalendarDelta;
    }


    /**
     * Sets the plannedCalendarDelta value for this Amendment.
     * 
     * @param plannedCalendarDelta
     */
    public void setPlannedCalendarDelta(edu.northwestern.bioinformatics.studycalendar.grid.Delta[] plannedCalendarDelta) {
        this.plannedCalendarDelta = plannedCalendarDelta;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.Delta getPlannedCalendarDelta(int i) {
        return this.plannedCalendarDelta[i];
    }

    public void setPlannedCalendarDelta(int i, edu.northwestern.bioinformatics.studycalendar.grid.Delta _value) {
        this.plannedCalendarDelta[i] = _value;
    }


    /**
     * Gets the epochDelta value for this Amendment.
     * 
     * @return epochDelta
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Delta[] getEpochDelta() {
        return epochDelta;
    }


    /**
     * Sets the epochDelta value for this Amendment.
     * 
     * @param epochDelta
     */
    public void setEpochDelta(edu.northwestern.bioinformatics.studycalendar.grid.Delta[] epochDelta) {
        this.epochDelta = epochDelta;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.Delta getEpochDelta(int i) {
        return this.epochDelta[i];
    }

    public void setEpochDelta(int i, edu.northwestern.bioinformatics.studycalendar.grid.Delta _value) {
        this.epochDelta[i] = _value;
    }


    /**
     * Gets the studySegmentDelta value for this Amendment.
     * 
     * @return studySegmentDelta
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Delta[] getStudySegmentDelta() {
        return studySegmentDelta;
    }


    /**
     * Sets the studySegmentDelta value for this Amendment.
     * 
     * @param studySegmentDelta
     */
    public void setStudySegmentDelta(edu.northwestern.bioinformatics.studycalendar.grid.Delta[] studySegmentDelta) {
        this.studySegmentDelta = studySegmentDelta;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.Delta getStudySegmentDelta(int i) {
        return this.studySegmentDelta[i];
    }

    public void setStudySegmentDelta(int i, edu.northwestern.bioinformatics.studycalendar.grid.Delta _value) {
        this.studySegmentDelta[i] = _value;
    }


    /**
     * Gets the periodDelta value for this Amendment.
     * 
     * @return periodDelta
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Delta[] getPeriodDelta() {
        return periodDelta;
    }


    /**
     * Sets the periodDelta value for this Amendment.
     * 
     * @param periodDelta
     */
    public void setPeriodDelta(edu.northwestern.bioinformatics.studycalendar.grid.Delta[] periodDelta) {
        this.periodDelta = periodDelta;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.Delta getPeriodDelta(int i) {
        return this.periodDelta[i];
    }

    public void setPeriodDelta(int i, edu.northwestern.bioinformatics.studycalendar.grid.Delta _value) {
        this.periodDelta[i] = _value;
    }


    /**
     * Gets the plannedActivityDelta value for this Amendment.
     * 
     * @return plannedActivityDelta
     */
    public edu.northwestern.bioinformatics.studycalendar.grid.Delta[] getPlannedActivityDelta() {
        return plannedActivityDelta;
    }


    /**
     * Sets the plannedActivityDelta value for this Amendment.
     * 
     * @param plannedActivityDelta
     */
    public void setPlannedActivityDelta(edu.northwestern.bioinformatics.studycalendar.grid.Delta[] plannedActivityDelta) {
        this.plannedActivityDelta = plannedActivityDelta;
    }

    public edu.northwestern.bioinformatics.studycalendar.grid.Delta getPlannedActivityDelta(int i) {
        return this.plannedActivityDelta[i];
    }

    public void setPlannedActivityDelta(int i, edu.northwestern.bioinformatics.studycalendar.grid.Delta _value) {
        this.plannedActivityDelta[i] = _value;
    }


    /**
     * Gets the name value for this Amendment.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this Amendment.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the date value for this Amendment.
     * 
     * @return date
     */
    public java.util.Date getDate() {
        return date;
    }


    /**
     * Sets the date value for this Amendment.
     * 
     * @param date
     */
    public void setDate(java.util.Date date) {
        this.date = date;
    }


    /**
     * Gets the mandatory value for this Amendment.
     * 
     * @return mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }


    /**
     * Sets the mandatory value for this Amendment.
     * 
     * @param mandatory
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }


    /**
     * Gets the id value for this Amendment.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Amendment.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the previousAmendmentId value for this Amendment.
     * 
     * @return previousAmendmentId
     */
    public java.lang.String getPreviousAmendmentId() {
        return previousAmendmentId;
    }


    /**
     * Sets the previousAmendmentId value for this Amendment.
     * 
     * @param previousAmendmentId
     */
    public void setPreviousAmendmentId(java.lang.String previousAmendmentId) {
        this.previousAmendmentId = previousAmendmentId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Amendment)) return false;
        Amendment other = (Amendment) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.plannedCalendarDelta==null && other.getPlannedCalendarDelta()==null) || 
             (this.plannedCalendarDelta!=null &&
              java.util.Arrays.equals(this.plannedCalendarDelta, other.getPlannedCalendarDelta()))) &&
            ((this.epochDelta==null && other.getEpochDelta()==null) || 
             (this.epochDelta!=null &&
              java.util.Arrays.equals(this.epochDelta, other.getEpochDelta()))) &&
            ((this.studySegmentDelta==null && other.getStudySegmentDelta()==null) || 
             (this.studySegmentDelta!=null &&
              java.util.Arrays.equals(this.studySegmentDelta, other.getStudySegmentDelta()))) &&
            ((this.periodDelta==null && other.getPeriodDelta()==null) || 
             (this.periodDelta!=null &&
              java.util.Arrays.equals(this.periodDelta, other.getPeriodDelta()))) &&
            ((this.plannedActivityDelta==null && other.getPlannedActivityDelta()==null) || 
             (this.plannedActivityDelta!=null &&
              java.util.Arrays.equals(this.plannedActivityDelta, other.getPlannedActivityDelta()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.date==null && other.getDate()==null) || 
             (this.date!=null &&
              this.date.equals(other.getDate()))) &&
            this.mandatory == other.isMandatory() &&
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.previousAmendmentId==null && other.getPreviousAmendmentId()==null) || 
             (this.previousAmendmentId!=null &&
              this.previousAmendmentId.equals(other.getPreviousAmendmentId())));
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
        if (getPlannedCalendarDelta() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPlannedCalendarDelta());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPlannedCalendarDelta(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getEpochDelta() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getEpochDelta());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getEpochDelta(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getStudySegmentDelta() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getStudySegmentDelta());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getStudySegmentDelta(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getPeriodDelta() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPeriodDelta());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPeriodDelta(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getPlannedActivityDelta() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPlannedActivityDelta());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPlannedActivityDelta(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getDate() != null) {
            _hashCode += getDate().hashCode();
        }
        _hashCode += (isMandatory() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getPreviousAmendmentId() != null) {
            _hashCode += getPreviousAmendmentId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Amendment.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Amendment"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("name");
        attrField.setXmlName(new javax.xml.namespace.QName("", "name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("date");
        attrField.setXmlName(new javax.xml.namespace.QName("", "date"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("mandatory");
        attrField.setXmlName(new javax.xml.namespace.QName("", "mandatory"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("id");
        attrField.setXmlName(new javax.xml.namespace.QName("", "id"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("previousAmendmentId");
        attrField.setXmlName(new javax.xml.namespace.QName("", "previous-amendment-id"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("plannedCalendarDelta");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "planned-calendar-delta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Delta"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("epochDelta");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "epoch-delta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Delta"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("studySegmentDelta");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "study-segment-delta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Delta"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("periodDelta");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "period-delta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Delta"));
        elemField.setMinOccurs(0);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("plannedActivityDelta");
        elemField.setXmlName(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "planned-activity-delta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bioinformatics.northwestern.edu/ns/psc", "Delta"));
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
