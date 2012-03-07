package gov.nih.nci.cabig.ctms.suite.authorization.socket.internal;

import gov.nih.nci.cabig.ctms.suite.authorization.socket.internal.IdentifiedName;
import gov.nih.nci.cabig.ctms.suite.authorization.socket.internal.IdentifiedNameIndex;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
public class IdentifiedNameIndexTest {
    private IdentifiedNameIndex index;

    @Before
    public void before() throws Exception {
        index = new IdentifiedNameIndex();
    }

    @Test
    public void itReturnsANameAndIdForANewName() throws Exception {
        assertThat(index.get("bar"),
            is(new IdentifiedName(0L, "bar")));
    }

    @Test
    public void itReturnsTheSameIdForRepeatedCalls() throws Exception {
        assertThat(index.get("foo").getId(), equalTo(index.get("foo").getId()));
    }

    @Test
    public void itReturnsDifferentIdsForDifferentNames() throws Exception {
        assertThat(index.get("foo").getId(), is(not(equalTo(index.get("bar").getId()))));
    }

    @Test
    public void itReturnsTheCorrectNameForAPreviouslyDefinedId() throws Exception {
        IdentifiedName existing = index.get("quux");
        assertThat(index.get(existing.getId()), is(existing));
    }

    @Test
    public void itReturnsNullForAnUnknownId() throws Exception {
        assertThat(index.get(6), is(nullValue()));
    }

    @Test
    public void itGivesTheSameIdForTwoSimultaneousAccessesOfANewName() throws Exception {
        IdentifiedNameIndex slowLookupIndex = new IdentifiedNameIndex() {
            @Override
            protected <K, V> Map<K, V> createMap() {
                return new HashMap<K, V>() {
                    @Override
                    public boolean containsKey(Object o) {
                        boolean result = super.containsKey(o);
                        slowDown(); // so that multiple lookups see the same containsKey value
                                    // before it is changed
                        return result;
                    }
                };
            }
        };

        NameLookup
            a = new NameLookup(slowLookupIndex, "bop"),
            b = new NameLookup(slowLookupIndex, "bop");
        a.start(); b.start();
        a.join(); b.join();

        assertThat(a.getIdRetrieved(), is(equalTo(b.getIdRetrieved())));
    }

    @Test
    public void itGivesDifferentIdsForTwoSimultaneousAccessesOfDifferentNames() throws Exception {
        IdentifiedNameIndex slowStoreIndex = new IdentifiedNameIndex() {
            @Override
            protected <K, V> Map<K, V> createMap() {
                return new HashMap<K, V>() {
                    @Override
                    public V put(K k, V v) {
                        slowDown();
                        return super.put(k, v);
                    }
                };
            }
        };

        NameLookup
            a = new NameLookup(slowStoreIndex, "bop"),
            b = new NameLookup(slowStoreIndex, "blit");
        a.start(); b.start();
        a.join(); b.join();

        assertThat(a.getIdRetrieved(), is(not(equalTo(b.getIdRetrieved()))));
    }

    private static void slowDown() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Slow sleep interrupted; continuing");
        }
    }

    private static class NameLookup extends Thread {
        private IdentifiedNameIndex index;
        private String desiredName;
        private Long idRetrieved;

        private NameLookup(IdentifiedNameIndex index, String desiredName) {
            this.desiredName = desiredName;
            this.index = index;
        }

        @Override
        public void run() {
            idRetrieved = index.get(desiredName).getId();
        }

        public Long getIdRetrieved() {
            return idRetrieved;
        }
    }
}
