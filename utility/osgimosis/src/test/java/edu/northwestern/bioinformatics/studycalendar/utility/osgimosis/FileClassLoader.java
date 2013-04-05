/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class FileClassLoader extends ClassLoader {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String displayName;
    private File root;

    public FileClassLoader(String name, File root) {
        this.displayName = name;
        this.root = root;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        log.trace("Looking for " + name);
        Class<?> found = findLoadedClass(name);
        if (found != null) {
            log.trace("Already loaded");
            return found;
        }

        File f = new File(root, name.replaceAll("\\.", "/") + ".class");
        if (!f.exists()) {
            log.trace(f + " does not exist; going to system classloader");
            return super.loadClass(name, resolve);
        }

        byte[] contents;
        try {
            contents = IOUtils.toByteArray(new FileInputStream(f));
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not read " + f, e);
        }
        log.trace("Defining from " + f);
        return defineClass(name, contents, 0, contents.length);
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append('[').append(displayName).append(']').toString();
    }
}
