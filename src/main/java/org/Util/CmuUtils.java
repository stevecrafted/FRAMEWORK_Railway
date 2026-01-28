package org.Util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.Entity.ClassMethodUrl;
import org.Entity.HttpMethod;
import org.annotation.AnnotationContoller;
import org.annotation.AnnotationMethode;
import org.annotation.GetMapping;
import org.annotation.PostMapping;
import org.annotation.RequestMapping;
import org.custom.CustomReflections;

public class CmuUtils {

    public static void saveCmuList(CustomReflections reflections, Map<String, ClassMethodUrl> urlMappings) {

        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(AnnotationContoller.class);
        for (Class<?> controller : controllers) {

            for (Method method : controller.getDeclaredMethods()) {

                String url = null;
                HttpMethod httpMethod = null;
                
                // Vérifier @GetMapping
                if (method.isAnnotationPresent(GetMapping.class)) {
                    url = method.getAnnotation(GetMapping.class).value();
                    httpMethod = HttpMethod.GET;
                }
                // Vérifier @PostMapping
                else if (method.isAnnotationPresent(PostMapping.class)) {
                    url = method.getAnnotation(PostMapping.class).value();
                    httpMethod = HttpMethod.POST;
                }
                // Vérifier @RequestMapping
                else if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                    url = annotation.value();
                    httpMethod = annotation.method();
                }
                // Vérifier @AnnotationMethode (rétrocompatibilité)
                else if (method.isAnnotationPresent(AnnotationMethode.class)) {
                    url = method.getAnnotation(AnnotationMethode.class).value();
                    // Par défaut, GET pour la rétrocompatibilité
                    httpMethod = HttpMethod.GET;
                }
                
                // Si une annotation de mapping a été trouvée
                if (url != null && httpMethod != null) {
                    // Créer la clé avec l'URL et la méthode HTTP
                    String key = httpMethod.name() + ":" + url;

                    ClassMethodUrl cmu = new ClassMethodUrl(controller, method);
                    cmu.setHttpMethod(httpMethod);
                    urlMappings.put(key, cmu);

                    System.out.println("Url : " + url + " | HTTP Method : " + httpMethod + " | Java Method : " 
                            + method.getName() + " | Controller : " + controller.getName());
                }
            }
        }
    }
    
    /*
     * Raha misy "{ }" ao anaty url dia
     * Avadika tableau ilay url par /
     * 
     * [1] = user
     * [2] = {id}
     * [3] = detail
     * 
     * Miditra ny Url dia avadika tableau koa par /
     * Raha mitovy ny longueur dia atao mapping
     * alaina ny position anle {id} ao amle url sauvegardena dia continue rehefa eo
     * aminle position id pour chaque partie de l'url
     * Break raha tsy mitovy dia retourne false
     * 
     * Raha mamerina true ilay fonction teo dia jerena ny anaranle parametre
     * ao amle methode ampitoviana aminy ao anatinle {}
     * dia jerena koa ny position any raha mitovy
     * 
     * Raha mitovy dia alaina ilay valeur ao amle url araka ny position
     */
    public static ClassMethodUrl findMapping(String url, Map<String, ClassMethodUrl> urlMappings,
            HttpServletRequest request) throws Exception {
        
        // Récupérer la méthode HTTP de la requête
        String requestMethod = request.getMethod(); // "GET" ou "POST"
        String keyWithMethod = requestMethod + ":" + url;

        System.out.println("Recherche du mapping pour : " + keyWithMethod);

        // Try exact match with HTTP method first
        if (urlMappings.containsKey(keyWithMethod)) {
            request.setAttribute("pathVars", new HashMap<String, String>());
            System.out.println("Mapping exact trouvé : " + keyWithMethod);
            return urlMappings.get(keyWithMethod);
        }

        // Try templates with {var} placeholders
        for (String key : urlMappings.keySet()) {
            // Extraire la méthode HTTP et l'URL de la clé
            String[] keyComponents = key.split(":", 2);
            if (keyComponents.length != 2) {
                continue;
            }

            String keyMethod = keyComponents[0];
            String keyUrl = keyComponents[1];

            // Vérifier si la méthode HTTP correspond
            if (!keyMethod.equals(requestMethod)) {
                continue;
            }

            // Vérifier si l'URL contient des variables
            if (!keyUrl.contains("{")) {
                continue;
            }

            String[] keyParts = keyUrl.split("/");
            String[] urlParts = url.split("/");

            if (keyParts.length != urlParts.length) {
                continue;
            }

            Map<String, String> pathVars = new HashMap<>();
            boolean isMatch = true;

            for (int i = 0; i < keyParts.length; i++) {
                String kp = keyParts[i];
                String up = urlParts[i];

                if (kp.startsWith("{") && kp.endsWith("}")) {
                    String varName = kp.substring(1, kp.length() - 1);
                    System.out.println("Variable trouvée dans l'URL : " + varName + " = " + up);
                    
                    pathVars.put(varName, up);
                } else {
                    // Raha misy iray tsy mitovy dia tsy mety zany
                    if (!kp.equals(up)) {
                        System.out.println("Partie de l'URL ne correspond pas : " + kp + " != " + up);

                        isMatch = false;
                        break;
                    }
                }
            }

            if (!isMatch) {
                // this template doesn't match the requested URL, try next mapping
                continue;
            }
            
            // matched: expose extracted path variables and return mapping
            request.setAttribute("pathVars", pathVars);
            System.out.println("Mapping avec variables trouvé : " + key);
            return urlMappings.get(key);
        }

        // No mapping found
        System.out.println("Aucun mapping trouvé pour : " + keyWithMethod);
        return null;
    }

}
