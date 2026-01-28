package org.Util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class JsonConverter {
    
    /**
     * Convertit un objet Java en JSON
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        // Types primitifs et String
        if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        // Collections (List, Set, etc.)
        if (obj instanceof Collection) {
            return collectionToJson((Collection<?>) obj);
        }
        
        // Map
        if (obj instanceof Map) {
            return mapToJson((Map<?, ?>) obj);
        }
        
        // Tableaux
        if (obj.getClass().isArray()) {
            return arrayToJson(obj);
        }
        
        // Objets personnalisés
        return objectToJson(obj);
    }
    
    /**
     * Convertit une Collection en JSON array
     */
    private static String collectionToJson(Collection<?> collection) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        
        for (Object item : collection) {
            if (!first) {
                sb.append(",");
            }
            sb.append(toJson(item));
            first = false;
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Convertit un Map en JSON object
     */
    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(toJson(entry.getValue()));
            first = false;
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Convertit un tableau en JSON array
     */
    private static String arrayToJson(Object array) {
        StringBuilder sb = new StringBuilder("[");
        int length = java.lang.reflect.Array.getLength(array);
        
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            Object item = java.lang.reflect.Array.get(array, i);
            sb.append(toJson(item));
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Convertit un objet personnalisé en JSON
     */
    private static String objectToJson(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        
        try {
            Class<?> clazz = obj.getClass();
            Field[] fields = clazz.getDeclaredFields();
            
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                
                // Ignorer les champs null
                if (value == null) {
                    continue;
                }
                
                if (!first) {
                    sb.append(",");
                }
                
                sb.append("\"").append(field.getName()).append("\":");
                sb.append(toJson(value));
                first = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Échappe les caractères spéciaux pour JSON
     */
    private static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}