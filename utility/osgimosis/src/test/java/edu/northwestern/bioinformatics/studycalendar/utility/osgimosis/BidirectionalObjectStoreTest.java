/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class BidirectionalObjectStoreTest extends TestCase {
    private BidirectionalObjectStore store;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        store = new BidirectionalObjectStore();
    }

    public void testGetUnmatchedIsNull() throws Exception {
        assertNull(store.get("nope"));
    }

    public void testGetRegetsFirstPutItem() throws Exception {
        String near = "near", far = "far";
        store.put(near, far);
        assertSame(far, store.get(near));
    }

    public void testGetRegetsSecondPutItem() throws Exception {
        String near = "near", far = "far";
        store.put(near, far);
        assertSame(near, store.get(far));
    }
    
    public void testPutWithTheSameFirstObjectClearsBothRefs() throws Exception {
        String s1 = "s1", s2 = "s2", s3 = "s3";
        store.put(s1, s2);
        store.put(s1, s3);
        assertSame("s1 ref not updated", s3, store.get(s1));
        assertNull("s2 ref not purged", store.get(s2));
    }

    public void testPutWithTheSameSecondObjectClearsBothRefs() throws Exception {
        String s1 = "s1", s2 = "s2", s3 = "s3";
        store.put(s1, s2);
        store.put(s2, s3);
        assertNull("s1 ref not purged", store.get(s1));
        assertSame("s2 ref not updated", s3, store.get(s2));
        assertSame("s3 ref not available", s2, store.get(s3));
    }

    public void testMemoryUtilizationIsSoft() throws Exception {
        if (canDoMemoryTest()) {
            Process p = performMemoryTest("soft");
            assertEquals("Memory test process completed with error", 0, p.exitValue());
        }
    }

    public void testMemoryUtilizationTestFailsWithoutSoftRefs() throws Exception {
        if (canDoMemoryTest()) {
            Process p = performMemoryTest("strong");
            assertEquals("Memory test process completed without error", 1, p.exitValue());
        }
    }

    private boolean canDoMemoryTest() {
        // The Sun JVM on the (Linux) build server does not appear to obey Xmx,
        // so it is difficult to get it to reliably run out of memory.
        return "Mac OS X".equals(System.getProperty("os.name"));
    }

    private Process performMemoryTest(String refType) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
            "java", "-Xmx16M", "-cp", "target/classes:target/test/classes",
            MemTest.class.getName(), refType);
        builder.redirectErrorStream(true);
        builder.directory(detectBaseDirectory());
        Process p = builder.start();
        p.waitFor();
        IOUtils.copy(p.getInputStream(), System.out);
        return p;
    }

    private File detectBaseDirectory() {
        Collection<File> candidates = Arrays.asList(new File("utility/osgimosis"), new File("."));
        for (File candidate : candidates) {
            if (new File(candidate, "target/classes").exists()) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not find base directory for running test subprocess");
    }

    public static class MemTest {
        private static final int ALLOCATION_UNITS = 13;
        private static final int ALLOCATION_SIZE = 1024 * 1024; // bytes

        public static void main(String[] args) throws Exception {
            boolean soft = "soft".equals(args[0]);
            BidirectionalObjectStore store = new BidirectionalObjectStore(soft);
            System.out.println(String.format("Using %s references", soft ? "soft" : "strong"));

            System.out.println(String.format("   Total memory: %8d", Runtime.getRuntime().totalMemory()));
            System.out.println(String.format("    Free memory: %8d", Runtime.getRuntime().freeMemory()));
            Element lastL = null, lastR = null;
            for (int i = 0 ; i < ALLOCATION_SIZE / 128 ; i++) {
                lastL = new Element("L" + i);
                lastR = new Element("R" + i);
                store.put(lastL, lastR);
            }

            System.out.println(String.format("Reference count: %13d", store.referenceCount()));
            System.out.println(String.format("   Total memory: %8d", Runtime.getRuntime().totalMemory()));
            System.out.println(String.format("    Free memory: %8d", Runtime.getRuntime().freeMemory()));
            byte[][] mem = new byte[ALLOCATION_UNITS][];
            for (int i = 0 ; i < mem.length ; i++) {
                System.out.println(String.format("                 Allocating round %d", i));
                mem[i] = new byte[ALLOCATION_SIZE];
                System.out.println(String.format(" Hard allocated: %8d", mem[i].length * (i + 1)));
                System.out.println(String.format("Reference count: %13d", store.referenceCount()));
                System.out.println(String.format("   Total memory: %8d", Runtime.getRuntime().totalMemory()));
                System.out.println(String.format("    Free memory: %8d", Runtime.getRuntime().freeMemory()));
            }

            if (store.get(lastL) != lastR) {
                throw new AssertionError("Strongly referenced pair not retained");
            }
        }
    }

    public static class Element {
        private String name;
        
        public Element(String n) {
            this.name = n;
        }
        
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Element element = (Element) o;

            return !(name != null ? !name.equals(element.name) : element.name != null);
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }
}
