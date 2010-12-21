package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

/**
 * An executable to help in characterizing PermGen use by osgimosis.  It's intended to be run
 * in a profiler.
 *
 * @author Rhett Sutphin
 */
public class MembraneLeak {
    @SuppressWarnings( { "InfiniteLoopStatement" })
    public static void main(String[] args) throws Exception {
        ClassLoader a = new FileClassLoader("A", root());
        ClassLoader b = new FileClassLoader("B", root());

        List<Object> people = createPeopleOnCL(a);
        Membrane membrane = new Membrane(b, Person.class.getPackage().getName());

        for (Object aPerson : people) {
            Object bPerson = membrane.farToNear(aPerson);
            System.out.println(bPerson + " once existed");
        }

        System.out.println("Waiting");
        while (true) { Thread.sleep(500); }
    }

    private static List<Object> createPeopleOnCL(ClassLoader a) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        List<Object> people = new LinkedList<Object>();
        Class<?> aPersonClass = a.loadClass(DefaultPerson.class.getName());
        Constructor<?> aPersonConstructor = aPersonClass.getConstructor(String.class, String.class);
        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 26; j++) {
                for (int k = 0; k < 26; k++) {
                    String n = new StringBuilder().
                        append((char) ('A' + i)).append((char) ('a' + j)).append((char) ('a' + k)).
                        toString();
                    people.add(aPersonConstructor.newInstance(n, "Dude " + people .size()));
                }
            }
        }
        return people;
    }

    private static File root() {
        File root = new File("target/test/classes");
        if (!root.exists()) {
            root = new File("utility/osgimosis", root.getPath());
            if (!root.exists()) {
                throw new IllegalStateException("Could not determine class directory");
            }
        }
        return root;
    }
}
