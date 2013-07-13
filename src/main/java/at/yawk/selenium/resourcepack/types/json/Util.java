package at.yawk.selenium.resourcepack.types.json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Util {
    private Util() {}
    
    @SuppressWarnings("unchecked")
    static <K, V> V putIfNotExists(Map<? super K, ? super V> map, K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
        return (V) map.get(key);
    }
    
    @SuppressWarnings("unchecked")
    static <T> T deepClone(T original) {
        if (original instanceof Set<?>) {
            Set<Object> copy = new HashSet<>(((Set<?>) original).size());
            for (Object o : ((Set<?>) original)) {
                copy.add(deepClone(o));
            }
            return (T) copy;
        }
        if (original instanceof List<?>) {
            List<Object> copy = original instanceof JSONArray ? new JSONArray() : new ArrayList<>(((List<?>) original).size());
            for (Object o : ((List<?>) original)) {
                copy.add(deepClone(o));
            }
            return (T) copy;
        }
        if (original instanceof Map<?, ?>) {
            Map<Object, Object> copy = original instanceof JSONObject ? new JSONObject() : new HashMap<>(((Map<?, ?>) original).size());
            for (Entry<?, ?> e : ((Map<?, ?>) original).entrySet()) {
                copy.put(deepClone(e.getKey()), deepClone(e.getValue()));
            }
            return (T) copy;
        }
        if (original instanceof Cloneable) {
            try {
                Method method = Object.class.getDeclaredMethod("clone");
                method.setAccessible(true);
                return (T) method.invoke(original);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {}
        }
        return original;
    }
}
