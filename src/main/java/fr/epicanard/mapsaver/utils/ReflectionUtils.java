package fr.epicanard.mapsaver.utils;


import java.lang.reflect.Field;
import java.util.Optional;

public class ReflectionUtils {

    /**
     * Get the value of searched field path
     *
     * @param obj Object on which search the field path
     * @param path Name of the field path
     * @return Value of the field path
     */
    public static Optional<Object> getField(Object obj, String path) {
        Object result = obj;
        try {
            for (String field : path.split("\\.")) {
                result = getOneField(result, field);
            }
            return Optional.ofNullable(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Get the value of one searched field
     *
     * @param obj Object on which search the field
     * @param fieldName Name of the field
     * @return Value of the field
     * @throws Exception Exception throws when reflection doesn't work
     */
    private static Object getOneField(Object obj, String fieldName) throws Exception {
        final Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}
