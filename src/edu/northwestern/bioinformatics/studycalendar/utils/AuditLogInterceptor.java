package edu.northwestern.bioinformatics.studycalendar.utils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;


public class AuditLogInterceptor extends EmptyInterceptor {

    private static final Log log = LogFactory.getLog(AuditLogInterceptor.class);
    private AuditLog auditLog;
    
    private static final String UPDATE = "update";
    private static final String INSERT = "insert";
    private static final String DELETE = "delete";
    
    public void setAuditLog(AuditLog auditLog) {
    	this.auditLog = auditLog;
    }


    public boolean onLoad(Object arg0, Serializable arg1, Object[] arg2, String[] arg3, Type[] arg4)
            throws CallbackException {
        return false;
    }

    public boolean onFlushDirty(Object obj, Serializable id, Object[] newValues, Object[] oldValues,
            String[] properties, Type[] types) throws CallbackException {
   
            Class objectClass = obj.getClass();
            String className = objectClass.getName();

            String[] tokens = className.split("\\.");
            int lastToken = tokens.length - 1;
            className = tokens[lastToken];

            Serializable persistedObjectId = getObjectId(obj);
            Object preUpdateState = auditLog.getObject(objectClass,  persistedObjectId);
            log.debug(" flush dirty call : " + preUpdateState.toString());
            
            try {
                
                auditLog.logChanges(obj, preUpdateState, null, persistedObjectId.toString(), UPDATE, className);
                
            } catch (IllegalArgumentException e) {
                 e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                 e.printStackTrace();
            }
        return false;
    }

    public boolean onSave(Object obj, Serializable id, Object[] newValues, String[] properties, Type[] types)
            throws CallbackException {
                 
            try {
                Class objectClass = obj.getClass();
                String className = objectClass.getName();
                String[] tokens = className.split("\\.");
                int lastToken = tokens.length - 1;
                className = tokens[lastToken];
                                
                auditLog.logChanges(obj, null, null, null, INSERT, className);
                
            } catch (IllegalArgumentException e) {
                log.debug(e.getMessage());
            } catch (IllegalAccessException e) {
                log.debug(e.getMessage());
            } catch (InvocationTargetException e) {
                log.debug(e.getMessage());
            }
     
        return false;
    }


    public void onDelete(Object obj, Serializable id, Object[] newValues, String[] properties, Type[] types)
            throws CallbackException {
        

           try {
                
                Class objectClass = obj.getClass();
                String className = objectClass.getName();
                String[] tokens = className.split("\\.");
                int lastToken = tokens.length - 1;
                className = tokens[lastToken];
                
                auditLog.logChanges(obj, null, null, id.toString(), DELETE, className);
                
            } catch (IllegalArgumentException e) {
                log.debug(e.getMessage());
            } catch (IllegalAccessException e) {
                log.debug(e.getMessage());
            } catch (InvocationTargetException e) {
                log.debug(e.getMessage());
            }
    }


    public void preFlush(Iterator arg0) throws CallbackException {
    }


    public void postFlush(Iterator arg0) throws CallbackException {
        log.debug("In postFlush of AuditLogInterceptor..");
        auditLog.saveAudit();
    }


    public Boolean isUnsaved(Object arg0) {
        return null;
    }


    public int[] findDirty(Object arg0, Serializable arg1, Object[] arg2, Object[] arg3, String[] arg4, Type[] arg5) {
        return null;
    }


    public Object instantiate(Class arg0, Serializable arg1) throws CallbackException {
        return null;
    }

    public Boolean isTransient(Object arg0) {
        return null;
    }


    private Serializable getObjectId(Object obj) {
        
        Class objectClass = obj.getClass();
        Method[] methods = objectClass.getMethods();

        Serializable persistedObjectId = null;
        for (int ii = 0; ii < methods.length; ii++) {
            // If the method name equals 'getId' then invoke it to get the id of the object.
            if (methods[ii].getName().equals("getId")) {
                try {
                    persistedObjectId = (Serializable)methods[ii].invoke(obj, null);
                    break;      
                } catch (Exception e) {
                    log.warn("Audit Log Failed - Could not get persisted object id: " + e.getMessage());
                }
            }
        }
        return persistedObjectId;
    }
    
}
