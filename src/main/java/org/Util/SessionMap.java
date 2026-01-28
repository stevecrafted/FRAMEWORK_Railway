package org.Util;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Wrapper qui permet de manipuler la session HTTP comme une Map
 */
public class SessionMap implements Map<String, Object> {
    
    private final HttpSession session;

    public SessionMap(HttpSession session) {
        this.session = session;
    }

    @Override
    public Object get(Object key) {
        return session.getAttribute(key.toString());
    }

    @Override
    public Object put(String key, Object value) {
        Object oldValue = session.getAttribute(key);
        session.setAttribute(key, value);
        return oldValue;
    }

    @Override
    public Object remove(Object key) {
        Object oldValue = session.getAttribute(key.toString());
        session.removeAttribute(key.toString());
        return oldValue;
    }

    @Override
    public void clear() {
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            session.removeAttribute(attributeNames.nextElement());
        }
    }

    @Override
    public int size() {
        int count = 0;
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            attributeNames.nextElement();
            count++;
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        return !session.getAttributeNames().hasMoreElements();
    }

    @Override
    public boolean containsKey(Object key) {
        return session.getAttribute(key.toString()) != null;
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            keys.add(attributeNames.nextElement());
        }
        return keys;
    }

    @Override
    public Collection<Object> values() {
        List<Object> values = new ArrayList<>();
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            values.add(session.getAttribute(attributeNames.nextElement()));
        }
        return values;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> entries = new HashSet<>();
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String key = attributeNames.nextElement();
            Object value = session.getAttribute(key);
            entries.add(new AbstractMap.SimpleEntry<>(key, value));
        }
        return entries;
    }

    // Méthodes non essentielles
    @Override
    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        for (Entry<? extends String, ?> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
}