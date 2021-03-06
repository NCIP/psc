/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package gov.nih.nci.cabig.ctms.suite.authorization.socket.internal;

/**
 * @author Rhett Sutphin
 */
public class IdentifiedName {
    private final Long id;
    private final String name;

    public IdentifiedName(Long id, String name) {
        if (id == null) throw new IllegalArgumentException("id is required");
        if (name == null) throw new IllegalArgumentException("name is required");
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdentifiedName that = (IdentifiedName) o;

        return getId().equals(that.getId()) && getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return 31 * getId().hashCode() + getName().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("[").
            append(getId()).append(" <=> \"").append(getName()).append("\"]").
            toString();
    }
}
