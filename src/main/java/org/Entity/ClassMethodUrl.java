package org.Entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class ClassMethodUrl {
    Class<?> Class;
    Method Method;
    HttpMethod httpMethod;

    // HttpMethod ato (get na post)
    // Au moment mampiditra anle map dia verifiena method anle request (request
    // getmethode)
    // setena
    // Manao anle url
    // Supportena
    // Miexiste ve le methode
    // Request mapping dia iray iany

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod method) {
        this.httpMethod = method;
    }

    public ClassMethodUrl(Class<?> Class, Method method) {
        this.Class = Class;
        this.Method = method;
    }

    public Class<?> getMyClass() {
        return this.Class;
    }

    public void setMyClass(Class<?> c) {
        this.Class = c;
    }

    public Method getMyMethod() {
        return this.Method;
    }

    public void SetMethod(Method m) {
        this.Method = m;
    }

    // Mi execute anle methode raha string no averiny
    public String ExecuteMethodeString(Object[] argumentArgs) throws Exception {
        try {
            Object controller = this.Class.getDeclaredConstructor().newInstance();
            Object result = this.Method.invoke(controller, argumentArgs);

            if (result instanceof String) {
                String viewName = (String) result;
                return viewName;
            } else {
                throw new Exception("le type de retour doit etre de type String");
            }

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        return "";
    }

    // Mi execute anle methode raha model view no averiny
    public String ExecuteMethodeModelView(HttpServletRequest req, Object[] argumentArgs) throws Exception {
        try {
            Object controller = this.Class.getDeclaredConstructor().newInstance();
            Object result = this.Method.invoke(controller, argumentArgs);

            if (result instanceof ModelView) {
                ModelView modelViewResultExecution = (ModelView) result;
                Map<String, Object> resultExecution = modelViewResultExecution.getAllAttributes();

                if (req == null) {
                    throw new Exception("L'objet req est null");
                }

                for (Map.Entry<String, Object> resultatModelView : resultExecution.entrySet()) {
                    req.setAttribute(resultatModelView.getKey(), resultatModelView.getValue());
                }

                return ((ModelView) result).getView();
            } else {
                throw new Exception("le type de retour doit etre de type model view");
            }

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Sprint 9 : Exécute la méthode et retourne l'objet brut
     * Utilisé pour les réponses JSON
     */
    public Object executeMethod(Object... args) throws Exception {
        Object instance = this.Class.getDeclaredConstructor().newInstance();
        return this.Method.invoke(instance, args);
    }

    @Override
    public String toString() {
        return "ClassMethodUrl [Class=" + Class.getName() + ",.Method=" + Method.getName()
                + ", httpMethod=" + httpMethod + "]";
    }
}
