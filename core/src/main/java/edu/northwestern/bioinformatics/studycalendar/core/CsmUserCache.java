/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core;

import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A cache used as a substitute for reloading CSM User objects when they are being loaded
 * by ID. Uses soft references to preserve memory and allows for invalidation.
 *
 * Motivation: CSM does not reuse hibernate sessions across API calls, even though it would often
 * be safe to do so. This means that repeated lookups of the same CSM object instances, even by ID,
 * hit the database and create new object instances. When loading many StudySubjectAssignments, for
 * example, the original implementation of AssignmentManagerResolverListener would issue a separate
 * CSM call for each one.
 *
 * @author Rhett Sutphin
 */
public class CsmUserCache {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private AuthorizationManager csmAuthorizationManager;

    private final Map<Integer, ReadWriteLock> locks;
    private final Map<Integer, SoftReference<User>> cache;

    public CsmUserCache() {
        locks = Collections.synchronizedMap(new HashMap<Integer, ReadWriteLock>());
        cache = new ConcurrentHashMap<Integer, SoftReference<User>>();
    }

    /**
     * Get the given user, either from the cache if available, or via the authorization manager
     * if not. Returns null if the given userId does not map to a user.
     */
    public User getCsmUser(int userId) {
        ReadWriteLock l = getLock(userId);
        l.readLock().lock();
        try {
            User fromCache = getFromCache(userId);
            if (fromCache != null) {
                return fromCache;
            } else {
                return loadStoreAndReturn(userId, l);
            }
        } finally {
            releaseIfPossible(l.readLock());
        }
    }

    private void releaseIfPossible(Lock lock) {
        try {
            lock.unlock();
        } catch (IllegalMonitorStateException imse) {
            log.debug(
                "Could not release a lock. This is probably due to another exception, so logging only.", imse);
        }
    }

    // assumes appropriate read lock is already held
    private User getFromCache(int userId) {
        SoftReference<User> reference = cache.get(userId);
        if (reference != null) {
            User fromCache = reference.get();
            if (fromCache != null) {
                return fromCache;
            }
        }
        return null;
    }

    private User loadStoreAndReturn(int userId, ReadWriteLock l) {
        l.readLock().unlock();
        l.writeLock().lock();
        try {
            User user = lookupUser(userId);
            if (user != null) {
                cache.put(userId, new SoftReference<User>(user));
            }

            // reacquire read lock for outer scope consistency
            l.readLock().lock();

            return user;
        } finally {
            releaseIfPossible(l.writeLock());
        }
    }

    private User lookupUser(int userId) {
        try {
            return csmAuthorizationManager.getUserById(Integer.toString(userId));
        } catch (CSObjectNotFoundException e) {
            return null;
        }
    }

    private ReadWriteLock getLock(int userId) {
        if (!locks.containsKey(userId)) {
            synchronized (locks) {
                if (!locks.containsKey(userId)) {
                    locks.put(userId, new ReentrantReadWriteLock());
                }
            }
        }
        return locks.get(userId);
    }

    /**
     * Invalidate the cache for the given ID, if there is one.
     */
    public void invalidate(int userId) {
        ReadWriteLock l = getLock(userId);
        l.writeLock();
        try {
            cache.remove(userId);
        } finally {
            releaseIfPossible(l.writeLock());
        }
    }

    ////// CONFIGURATION

    @Required
    public void setCsmAuthorizationManager(AuthorizationManager csmAuthorizationManager) {
        this.csmAuthorizationManager = csmAuthorizationManager;
    }
}
