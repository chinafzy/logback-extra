package me.in1978.third.logback;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
@RequiredArgsConstructor
public class ScopedContext {

    private final ScopedContext parent;
    private final Map<String, Object> local = new ConcurrentHashMap<>();


    public <T> T get(String name) {
        if (local.containsKey(name)) {
            return (T) local.get(name);
        }

        if (parent != null) {
            return parent.get(name);
        }

        return null;
    }

    public void put(String name, Object obj) {
        if (local.containsKey(name)) {
            local.put(name, obj);
            return;
        }

        if (parent != null) {
            parent.put(name, obj);
            return;
        }

        throw new RuntimeException("var not declared:" + name);
    }

    public <T> T getLocal(String name) {
        return (T) local.get(name);
    }


    public void putLocal(String name, Object obj) {
        if (!hasLocal(name)) {
            throw new RuntimeException("var not declared in local:" + name);
        }

        local.put(name, obj);
    }

    public void declareAndPutLocal(String name, Object obj) {
        if (hasLocal(name)) {
            throw new RuntimeException("var already declared in local:" + name);
        }

        local.put(name, obj);
    }


    public boolean hasLocal(String name) {
        return local.containsKey(name);
    }

    public void declareLocal(String name) {
        if (local.containsKey(name)) {
            throw new RuntimeException("local var already declared:" + name);
        }

        local.put(name, null);
    }

    public boolean deleteLocal(String name) {
        if (!local.containsKey(name)) {
            return false;
        }

        local.remove(name);
        return true;
    }

    public boolean delete(String name) {
        if (deleteLocal(name)) {
            return true;
        }

        return parent != null && parent.delete(name);
    }

    public Set<String> names() {
        Set<String> ret = new HashSet<>(local.keySet());
        if (parent != null) {
            ret.addAll(parent.names());
        }

        return ret;
    }

    public boolean has(String name) {
        return local.containsKey(name) ||
                (parent != null && parent.has(name));
    }


    public Map<String, Object> all() {

        Map<String, Object> ret = new HashMap<>();

        if (parent != null) {
            ret.putAll(parent.all());
        }
        ret.putAll(local);

        return ret;
    }

    /**
     * Generate a new child ScopedContext.
     *
     * @return
     */
    public ScopedContext pushStack() {
        return new ScopedContext(this);
    }

    /**
     * Get parent ScopedContext.
     *
     * @return
     */
    public ScopedContext popStack() {
        return parent;
    }

}
