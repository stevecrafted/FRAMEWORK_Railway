package org.Entity;

import java.util.Collection;

public class JsonResponse {
    private String status;  // "success" ou "error"
    private int code;       // 200, 404, 500, etc.
    private Object data;    // données à retourner
    private Integer count;  // nombre d'éléments si c'est une liste
    
    public JsonResponse() {}
    
    public JsonResponse(String status, int code, Object data) {
        this.status = status;
        this.code = code;
        this.data = data;
        
        // Si data est une Collection, ajouter le count
        if (data instanceof Collection) {
            this.count = ((Collection<?>) data).size();
        }
    }
    
    // Méthodes statiques pour créer des réponses standard
    public static JsonResponse success(Object data) {
        return new JsonResponse("success", 200, data);
    }
    
    public static JsonResponse error(String message, int code) {
        return new JsonResponse("error", code, message);
    }
    
    public static JsonResponse notFound(String message) {
        return new JsonResponse("error", 404, message);
    }
    
    public static JsonResponse serverError(String message) {
        return new JsonResponse("error", 500, message);
    }
    
    // Getters et Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
        // Mettre à jour count si c'est une collection
        if (data instanceof Collection) {
            this.count = ((Collection<?>) data).size();
        }
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
}