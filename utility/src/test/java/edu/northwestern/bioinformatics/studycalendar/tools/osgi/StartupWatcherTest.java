package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.springframework.osgi.mock.MockBundle;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Rhett Sutphin
 */
public class StartupWatcherTest {
    private static final int EXPECTED_START_LEVEL = 9;

    private StartupWatcher watcher;
    private MockStartLevel startLevelService;
    private Bundle systemBundle;

    @Before
    public void before() throws Exception {
        startLevelService = new MockStartLevel();
        BundleContext bundleContext = new InstallingMockBundleContext(startLevelService);
        systemBundle = new MockBundle(bundleContext);

        watcher = new StartupWatcher(EXPECTED_START_LEVEL);
    }

    @Test
    public void itThrowsAnExceptionFromWaitIfAnExceptionEventIsReceived() throws Exception {
        fireEvent(FrameworkEvent.ERROR, new Exception("expected"));

        try {
            watcher.waitForStart(100);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException actual) {
            assertThat(actual.getMessage(), is("Embedded OSGi framework startup failed"));
            assertThat(actual.getCause().getMessage(), is("expected"));
        }
    }

    @Test
    public void waitThrowsAnExceptionAfterTheTimeoutInterval() throws Exception {
        try {
            watcher.waitForStart(100);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException actual) {
            assertThat(actual.getMessage(), is("Embedded OSGi framework did not start within 100ms"));
        }
    }

    @Test
    public void waitForStartReturnsImmediatelyWhenAlreadyStarted() throws Exception {
        changeStartLevel(EXPECTED_START_LEVEL);

        long start = System.currentTimeMillis();
        watcher.waitForStart(2000L);
        assertThat(System.currentTimeMillis() - start, lessThan(500L));
    }

    @Test
    public void waitForStartSucceedsWhenStartLevelChangedWhileWaiting() throws Exception {
        final long[] waitDuration = new long[1];
        Thread watching = new Thread(new Runnable() {
            public void run() {
                long start = System.currentTimeMillis();
                watcher.waitForStart(10000L);
                waitDuration[0] = System.currentTimeMillis() - start;
            }
        }, "watching");
        watching.start();

        pauseFor(250L);
        changeStartLevel(EXPECTED_START_LEVEL - 1);

        pauseFor(250L);
        changeStartLevel(EXPECTED_START_LEVEL);

        watching.join();
        assertThat(waitDuration[0], is(greaterThan(400L)));
        assertThat(waitDuration[0], is(lessThan(9900L)));
    }

    private void pauseFor(long milliseconds) throws InterruptedException {
        long timeRemaining = milliseconds;
        long start, elapsed;
        while (timeRemaining > 0) {
            start = System.currentTimeMillis();
            Thread.sleep(timeRemaining);
            elapsed = System.currentTimeMillis() - start;
            timeRemaining -= elapsed;
        }
    }

    @Test
    public void itIsNotSatisfiedWhenTheCurrentRunLevelIsLowerThanTheExpected() throws Exception {
        changeStartLevel(EXPECTED_START_LEVEL - 1);

        assertThat(watcher.isAtExpectedStartLevel(), is(false));
    }

    @Test
    public void itIsSatisfiedWhenTheCurrentRunLevelIsWhatWasExpected() throws Exception {
        changeStartLevel(EXPECTED_START_LEVEL);

        assertThat(watcher.isAtExpectedStartLevel(), is(true));
    }

    @Test
    public void itIsSatisfiedWhenTheCurrentRunLevelIsHigherThanExpected() throws Exception {
        changeStartLevel(EXPECTED_START_LEVEL + 1);

        assertThat(watcher.isAtExpectedStartLevel(), is(true));
    }

    private void changeStartLevel(int desiredLevel) throws InterruptedException {
        startLevelService.setStartLevel(desiredLevel);
        fireEvent(FrameworkEvent.STARTLEVEL_CHANGED, null);
    }

    private void fireEvent(final int type, final Throwable err) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                watcher.frameworkEvent(new FrameworkEvent(type, systemBundle, err));
            }
        }, "firing " + type);
        thread.start();
        thread.join();
    }
}
