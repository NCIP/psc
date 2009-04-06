package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

/**
 * @author Rhett Sutphin
 */
public class DefaultPerson implements Person {
    private String name, kind;

    public DefaultPerson() { }

    public DefaultPerson(String name, String kind) {
        setName(name);
        setKind(kind);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;

        Person that = (Person) o;

        if (kind != null ? !kind.equals(that.getKind()) : that.getKind() != null) return false;
        if (name != null ? !name.equals(that.getName()) : that.getName() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        return result;
    }
}
