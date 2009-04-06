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
        Process p = performMemoryTest("soft");
        assertEquals("Memory test process completed with error", 0, p.exitValue());
    }

    public void testMemoryUtilizationTestFailsWithoutSoftRefs() throws Exception {
        Process p = performMemoryTest("strong");
        assertEquals("Memory test process completed without error", 1, p.exitValue());
    }

    private Process performMemoryTest(String refType) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
            "java", "-Xms16M", "-Xmx16M", "-cp", "target/classes:target/test/classes", MemTest.class.getName(), refType);
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
        public static void main(String[] args) {
            BidirectionalObjectStore store = new BidirectionalObjectStore("soft".equals(args[0]));

            System.out.println(String.format("   Total memory: %8d", Runtime.getRuntime().totalMemory()));
            System.out.println(String.format("    Free memory: %8d", Runtime.getRuntime().freeMemory()));
            String lastL = null, lastR = null;
            for (int i = 0 ; i < 2500 ; i++) {
                lastL = "L" + i;
                lastR = "R" + i;
                store.put(lastL, lastR);
            }
            System.out.println("           last: " + lastL + ", " + lastR);

            System.out.println(String.format("Reference count: %13d", store.referenceCount()));
            System.out.println(String.format("    Free memory: %8d", Runtime.getRuntime().freeMemory()));
            int[][] mem = new int[15][];
            for (int i = 0 ; i < mem.length ; i++) {
                mem[i] = new int[256 * 1024]; // 1MB
                System.out.println(String.format(" Hard allocated: %8d", mem[i].length * (i + 1)));
                System.out.println(String.format("Reference count: %13d", store.referenceCount()));
                System.out.println(String.format("    Free memory: %8d", Runtime.getRuntime().freeMemory()));
            }
            if (store.get(lastL) != lastR) {
                throw new AssertionError("Strongly referenced pair not retained");
            }
        }
    }
}
