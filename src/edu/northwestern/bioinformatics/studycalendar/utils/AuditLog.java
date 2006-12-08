package edu.northwestern.bioinformatics.studycalendar.utils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import edu.northwestern.bioinformatics.studycalendar.domain.AuditEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.AuditEventValue;
import edu.nwu.bioinformatics.commons.DataAuditInfo;

public class AuditLog {
	private static final Log log = LogFactory.getLog(AuditLog.class);

	private SessionFactory logSessionFactory;
	
    private static final String UPDATE = "update";
    private static final String INSERT = "insert";
    private static final String DELETE = "delete";
    
    private Set inserts = new HashSet();
    private Set updates = new HashSet();
    private Set deletes = new HashSet(); 
	
    public void setSessionFactory(SessionFactory logSessionFactory) {
        this.logSessionFactory = logSessionFactory;
    }
    
    public SessionFactory getSessionFactory() { 
    	return logSessionFactory; 
    } 
    

    public void logChanges(Object newObject, Object existingObject, Object parentObject,
    		String persistedObjectId, String event, String className)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException  {     
	
    	Class objectClass = newObject.getClass();      
    	Field[] fields = getAllFields(objectClass, null);
		DataAuditInfo info = DataAuditInfo.getLocal();
	    AuditEvent logRecord = new AuditEvent();
	    List<AuditEventValue> auditValueList = new ArrayList<AuditEventValue>();
	    logRecord.setClassName(className);
	    logRecord.setIpAddress("120.0.0.1");//info.getIp());
	    logRecord.setObjectId(persistedObjectId);
	    logRecord.setOperation(event);
	    logRecord.setTime(new Timestamp(new Date().getTime()));
	    logRecord.setUserName("user");//info.getBy());
	    logRecord.setUrl("todo");
 
	   	fieldIteration: for (int i = 0; i < fields.length; i++) {

    	//make private fields accessible so we can access their values
    	fields[i].setAccessible(true);
    	String fieldName = fields[i].getName();
    	if(! fieldName.equals("id")) {
    		Class interfaces[] = fields[i].getType().getInterfaces();
    		for (int j= 0; j < interfaces.length; j++) {
    			if (interfaces[j].getName().equals("java.util.Collection")) {
    				continue fieldIteration;
    			} 
    		}

    		String propertyNewState;
    		String propertyPreUpdateState;

    		//get new field values
    		try {
    			Object objPropNewState = fields[i].get(newObject);
    			if (objPropNewState != null) {
    				propertyNewState = objPropNewState.toString();
    			} else {
    				propertyNewState = "";
    			}

    		} catch (Exception e) {
    			propertyNewState = "";
    		}
  
    		if(event.equals(UPDATE)) {
    			try {
    				Object objPreUpdateState = fields[i].get(existingObject);
    				if (objPreUpdateState != null) {
    					propertyPreUpdateState = objPreUpdateState.toString();
    				} else {
    					propertyPreUpdateState = "";
    				}
    			} catch (Exception e) {
    				propertyPreUpdateState = "";
    			}
      
    			if (propertyNewState.equals(propertyPreUpdateState)) {
    				continue; 
    			} else  {
    				AuditEventValue auditValue = new AuditEventValue();
    				auditValue.setPreviousValue(propertyPreUpdateState);
    				auditValue.setNewValue(propertyNewState);
    				auditValueList.add(auditValue);
    			}
    		} else if(event.equals(DELETE)) {
    			Object returnValue = fields[i].get(newObject);

    			AuditEventValue auditValue = new AuditEventValue();
    			if (returnValue != null)	
    				auditValue.setPreviousValue(returnValue.toString());
    			auditValue.setNewValue("");
    			auditValueList.add(auditValue);
    		} else if(event.equals(INSERT)) {
          
    			Object returnValue = fields[i].get(newObject);
    			System.out.println("inside insert");
    			AuditEventValue auditValue = new AuditEventValue();
    			auditValue.setPreviousValue("");
    			if (returnValue != null)
    				auditValue.setNewValue(returnValue.toString());
    			auditValueList.add(auditValue);
    		}
    	}
    	}
	    logRecord.setAuditEventValues(auditValueList);
		if(event.equals(UPDATE)) {
		  	updates.add(logRecord);
		} else if (event.equals(DELETE)) {
		   	deletes.add(logRecord);
		} else if (event.equals(INSERT)) {
		   	inserts.add(logRecord);
		}
    }
    
    /**
     * Returns an array of all fields used by this object from it's class and all superclasses.
     * @param objectClass the class 
     * @param fields the current field list
     * @return an array of fields
     */
    private Field[] getAllFields(Class objectClass, Field[] fields) {
        
        Field[] newFields = objectClass.getDeclaredFields();
        
        int fieldsSize = 0;
        int newFieldsSize = 0;
        
        if(fields != null) {
            fieldsSize = fields.length;
        }
        
        if(newFields != null) {
            newFieldsSize = newFields.length;
        }

        Field[] totalFields = new Field[fieldsSize + newFieldsSize];
        
       if(fieldsSize > 0) {
           System.arraycopy(fields, 0, totalFields, 0, fieldsSize);
       }
       
       if(newFieldsSize > 0) { 
           System.arraycopy(newFields, 0, totalFields, fieldsSize, newFieldsSize);
       }
       
       Class superClass = objectClass.getSuperclass();
       
       Field[] finalFieldsArray;
       
       if (superClass != null && ! superClass.getName().equals("java.lang.Object")) {
           finalFieldsArray = getAllFields(superClass, totalFields);
       } else {
           finalFieldsArray = totalFields;
       }
       
       return finalFieldsArray;

    }

    /**
     * Gets the id of the persisted object
     * @param obj the object to get the id from
     * @return object Id
     */
    private Serializable getObjectId(Object obj) {
        
        Class objectClass = obj.getClass();
        Method[] methods = objectClass.getMethods();

        Serializable persistedObjectId = null;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("getId")) {
                try {
                    persistedObjectId = (Serializable)methods[i].invoke(obj, null);
                    break;      
                } catch (Exception e) {
                    log.warn("Audit Log Failed - Could not get persisted object id: " + e.getMessage());
                }
            }
        }
        return persistedObjectId;
    }
    
    public Object getObject(Class classObj, Serializable objectId) {
    	Session session = logSessionFactory.openSession();
    	 return session.get(classObj,  objectId);
    }
    
    public void saveAudit() {
    	Session session = logSessionFactory.openSession();

        try {
            for (Iterator itr = inserts.iterator(); itr.hasNext();) {
                AuditEvent logRecord = (AuditEvent) itr.next();
                logRecord.setObjectId(getObjectId(logRecord.getClassName()).toString());
                session.save(logRecord);
            }
            for (Iterator itr = updates.iterator(); itr.hasNext();) {
            	AuditEvent logRecord = (AuditEvent) itr.next();
                session.save(logRecord);
            }
            for (Iterator itr = deletes.iterator(); itr.hasNext();) {
            	AuditEvent logRecord = (AuditEvent) itr.next();
                session.save(logRecord);
            }
        } catch (HibernateException e) {
            throw new CallbackException(e);
        } finally {
            inserts.clear();
            updates.clear();
            deletes.clear();
            session.flush();
            session.close();
        }
    }
    
    public void clear() {
        // clear any audit log records potentially remaining from a rolled back
        // transaction
        inserts.clear();
        updates.clear();
        deletes.clear();
    }
}
