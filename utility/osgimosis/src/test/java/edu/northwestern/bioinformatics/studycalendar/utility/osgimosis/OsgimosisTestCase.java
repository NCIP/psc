/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import junit.framework.TestCase;

import java.io.File;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public abstract class OsgimosisTestCase extends TestCase {
    protected ClassLoader loaderA, loaderB;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Handle different CWDs for in-IDE vs. in-buildr
        File root = new File("target/test/classes");
        if (!root.exists()) {
            root = new File("utility/osgimosis", root.getPath());
            if (!root.exists()) {
                throw new IllegalStateException("Could not determine class directory");
            }
        }
        loaderA = new FileClassLoader("A", root);
        loaderB = new FileClassLoader("B", root);
    }

    protected Class classFromLoader(Class<?> klass, ClassLoader loader) throws ClassNotFoundException {
        return loader.loadClass(klass.getName());
    }
}