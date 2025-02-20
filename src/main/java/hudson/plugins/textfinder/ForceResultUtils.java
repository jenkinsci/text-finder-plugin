package hudson.plugins.textfinder;

import hudson.model.TaskListener;
import java.lang.reflect.Field;

public class ForceResultUtils {

    private ForceResultUtils() {}

    static void changeField(final Object source, final String name, final Object value, final TaskListener listener) {
        try {
            changeFieldImpl(source, name, value, listener);
        } catch (Exception ex) {
            listener.getLogger().println(ex.toString());
        }
    }

    private static void changeFieldImpl(
            final Object source, final String name, final Object value, final TaskListener listener)
            throws IllegalAccessException, NoSuchFieldException {
        Field field = findField(source.getClass(), name);
        if (field == null) {
            throw new NoSuchFieldException(name + " not fund in " + source.getClass() + " not in supperclasses");
        }
        field.setAccessible(true);
        listener.getLogger().println(source.getClass() + " of " + source + ": " + name + " = " + field.get(source));
        field.set(source, value);
        listener.getLogger().println(source.getClass() + " of " + source + ": " + name + " = " + field.get(source));
    }

    private static Field findField(final Class clazz, final String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            if (clazz.getSuperclass() != null) {
                return findField((clazz.getSuperclass()), name);
            } else {
                return null;
            }
        }
    }
}
