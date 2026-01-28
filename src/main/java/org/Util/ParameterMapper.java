package org.Util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.annotation.AnnotationRequestParam;
import org.annotation.GigaSession;
import org.Util.CmuMapperUtils.Utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ParameterMapper {

    public static Object[] mapParameters(Parameter[] methodParameters, HttpServletRequest req, Method method)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, IOException, ServletException {
        Object[] methodArgs = new Object[methodParameters.length];

        System.out.println(methodParameters.length + " parametre(s) trouve(s)");
        System.out.println("Mise en place des parametres : ");

        for (int i = 0; i < methodArgs.length; i++) {
            Parameter param = methodParameters[i];
            String paramName = param.getName();
            Class<?> paramType = param.getType();

            // ========== SPRINT 11 : GESTION DE SESSION ==========
            if (param.isAnnotationPresent(GigaSession.class)) {
                if (Map.class.isAssignableFrom(paramType)) {
                    // Créer un wrapper autour de la session HTTP
                    Map<String, Object> sessionMap = new SessionMap(req.getSession());
                    methodArgs[i] = sessionMap;
                    System.out.println("Parametre @GigaSession injecté");
                    continue;
                } else {
                    throw new IllegalArgumentException(
                            "Le paramètre annoté avec @GigaSession doit être de type Map<String, Object>");
                }
            }

            /*
             * Sprint 8,
             * Raha misy Map<String, String> ao anaty Parametre anle Controlleur
             * jerena aloha raha Map String string le izy raha de type map le parametre
             * Raha oui dia micrée Map vaovao de alefa ao daolo le nom param sy value
             * alefa any am method args aveo dia lasa ho azy
             * 
             * Methode mitsatsaka :)
             */
            if (Map.class.isAssignableFrom(paramType)) {
                System.out.println("Map aloa le parametre anh");

                if (Utils.isMapStringString(param)) {
                    Map<String, String> dataMap = extractAllParameters(req);
                    methodArgs[i] = dataMap;
                    System.out.println("Parametre " + paramName + " (Map<String, String>) mis en place avec "
                            + dataMap.size() + " entree(s)");
                    continue;
                }

                /*
                 * sprint 10
                 * Map String, byte[]
                 */
                else if (Utils.isMapStringByteArray(param)) {
                    System.out.println("String byte[] izy zany");

                    // Verifiena raha : multipart/form-data
                    if (!Utils.isMultipart(req)) {
                        System.out.println("Tsy multipart lee");

                        throw new RuntimeException(
                                "Le paramètre '" + paramName +
                                        "' requiert une requête multipart/form-data");
                    }

                    System.out.println("multipart le izy zany anh");

                    Map<String, byte[]> data = Utils.extractFiles(req);
                    System.out.println("Vita ny extraction zany anh");

                    methodArgs[i] = data;

                    System.out.println("Parametre " + paramName + " (Map<String, byte[]>) mis en place avec "
                            + data.size() + " entree(s)");
                    continue;
                }

                else {
                    System.out.println(
                            "Type Map non supporte pour " + paramName + " (seul Map<String, String> est accepte)");
                    methodArgs[i] = new HashMap<String, String>();
                    continue;
                }

            }

            /*
             * Sprint 6
             * Raha ita any anaty requet ?id=zavatra na post avy any am formulaire dia alefa
             * direct any am
             * parametre le valeur
             */
            String paramValue = req.getParameter(paramName);
            if (paramValue != null) {
                methodArgs[i] = Utils.convertParameter(paramValue, param.getType(), paramName);
                continue;
            }

            /*
             * Sprint 6 Ter, raha toa ka /{id}/url...
             * dia ao omena direct anle
             * 
             * Efa sauvegarde ato aminy "req.getAttribute("pathVars")
             * daolo ny nom anle parametre sy ny valeur any
             */
            Object attr = req.getAttribute("pathVars");
            if (attr instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> pathVars = (Map<String, String>) attr;
                String raw = pathVars.get(paramName);
                if (raw != null) {
                    methodArgs[i] = Utils.convertParameter(raw, param.getType(), paramName);
                    continue;
                }
            }

            /*
             * Sprint 6 bis
             * Raha mampiasa annotation @RequestParam dia matchena ilay valeur
             */
            if (param.isAnnotationPresent(AnnotationRequestParam.class)) {
                AnnotationRequestParam requestParamAnnotation = param.getAnnotation(AnnotationRequestParam.class);
                String requestParamName = requestParamAnnotation.value();
                String requestParamValue = req.getParameter(requestParamName);

                if (requestParamValue != null) {
                    methodArgs[i] = Utils.convertParameter(requestParamValue, param.getType(), requestParamName);
                    continue;
                } else {
                    System.out.println("Parametre " + requestParamName + " non trouve dans la requete");
                }
            } else {
                System.out.println("Parametre " + paramName + " non trouve dans la requete");
            }

            /*
             * Sprint 8 bis
             * sprintHuitBis(Employe e)
             * requete: e.name = zavatra
             * e.dept[0].name = zavatra
             *
             * Afaka tonga dia jerena ao am paramType raha tsy objet
             * Primitive le parametre (string, int, Boolean, etc.)
             * Verifiena raha misy "." ao anatinle requete (Post na Get)
             * 
             * Raha misy dia :
             * alaina ny eo alohanle "."
             * 
             * - jerena raha misy mitovy anarana amle izy ao anaty
             * methode anle controlleur
             * - Micréer instance anle Objet iny
             * - setena alaina ny ao ariana ".name"
             * 
             * - jerena raha misy attribut otraniny ao amle Objet
             * setena
             * 
             * Iny objet iny no alefa ao aminy controlleur
             */
            Map<String, String[]> parametre = req.getParameterMap();
            /*
             * renvoie un truc de type
             * "e.name" = ["John"],
             * "e.departement[0].name" = ["IT"],
             * "e.departement[1].name" = ["HR"],
             * 
             * "id" = [12]
             * "d.name" = ["Info"]
             */
            // Eto izy raha controlleur(Employe[] e)
            if (paramType.isArray()) {
                Map<Integer, Map<String, String>> arrayData = extractArrayParameters(parametre, paramName);

                if (!arrayData.isEmpty()) {
                    System.out.println("-> Paramètre tableau détecté : " + paramName + "[]");
                    System.out.println("   " + arrayData.size() + " élément(s) trouvé(s)");

                    Object array = populateArray(paramType.getComponentType(), arrayData);
                    methodArgs[i] = array;
                    continue;
                }

            }

            // Eto izy raha controlleur(Employe e)
            if (!Utils.isPrimitive(paramType)) {
                // stocker les paramètres qui concernent cet objet
                Map<String, String> filtered = new HashMap<>();

                for (Map.Entry<String, String[]> entry : parametre.entrySet()) {
                    String parameterKey = entry.getKey();
                    String[] parameterValue = entry.getValue();

                    System.out.println("parameterKey : " + parameterKey);
                    System.out.println("paramName : " + paramName);

                    if (parameterKey.startsWith(paramName + ".")) {
                        filtered.put(parameterKey.substring(paramName.length() + 1), parameterValue[0]);

                        for (String value : parameterValue) {
                            System.out.println("value : " + value);
                        }
                    }

                }

                if (!filtered.isEmpty()) {
                    System.out.println("-> Paramètre objet détecté : " + paramName);
                    System.out.println("   " + filtered.size() + " champs trouvés pour " + paramName);

                    Object instance = populateObject(paramType, filtered);
                    methodArgs[i] = instance;
                    continue;
                }
            }

        }

        return methodArgs;

    }

    private static Object populateObject(Class<?> clazz, Map<String, String> values)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        try {
            /*
             * Eto mahazo Employee de le map String, String :
             * "name" = ["John"],
             * "departement[0].name" = ["IT"],
             * "departement[1].name" = ["HR"],
             * 
             * * "departement[1].manager.parent.name" = ["Papi drac"],
             */
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Map.Entry<String, String> entry : values.entrySet()) {
                String paramKey = entry.getKey();
                String paramValue = entry.getValue();

                populateRecursive(instance, paramKey, paramValue);
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du mapping objet", e);
        }
    }

    private static void populateRecursive(Object currentObj, String key, String value) throws Exception {

        // Exemple : departement[0].manager.name
        String[] parts = key.split("\\.", 2);

        String currentPart = parts[0]; // "departement[0]"
        String remaining = (parts.length > 1 ? parts[1] : null);

        // Extraire nom + index
        String fieldName = Utils.extractFieldName(currentPart); // → "departement"
        Integer index = Utils.extractIndex(currentPart); // → 0 ou null

        System.out.println(" FieldName : " + fieldName);
        Field field = currentObj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        Class<?> fieldType = field.getType();
        Object fieldValue = field.get(currentObj);

        // ============================================================
        // CAS TABLEAU (Employee[], String[], etc.)
        // ============================================================
        if (fieldType.isArray()) {
            Class<?> elementType = fieldType.getComponentType();

            int arrayLength = index != null ? index + 1 : 1;

            Object array = fieldValue;
            if (array == null) {
                array = Array.newInstance(elementType, arrayLength);
            } else {
                int currentLength = Array.getLength(array);
                if (arrayLength > currentLength) {
                    // créer un nouveau tableau plus grand et copier l'ancien
                    Object newArray = Array.newInstance(elementType, arrayLength);
                    System.arraycopy(array, 0, newArray, 0, currentLength);
                    array = newArray;
                }
            }

            // Créer instance si nécessaire
            Object element = Array.get(array, index != null ? index : 0);
            if (element == null && !Utils.isPrimitive(elementType)) {
                element = elementType.getDeclaredConstructor().newInstance();
                Array.set(array, index != null ? index : 0, element);
            }

            // Affectation finale ou récursion
            if (remaining == null) {
                Array.set(array, index != null ? index : 0, Utils.convert(value, elementType));
            } else {
                populateRecursive(element, remaining, value);
            }

            // Mettre à jour le champ dans l'objet
            field.set(currentObj, array);
            return;
        }

        // ============================================================
        // 1. CAS LISTE (departement[0]...)
        // ============================================================
        if (List.class.isAssignableFrom(fieldType)) {

            if (fieldValue == null) {
                fieldValue = new ArrayList<>();
                field.set(currentObj, fieldValue);
            }
            List list = (List) fieldValue;

            // Déterminer type des éléments de la liste
            Class<?> elementType = Utils.getListElementType(field);

            // Agrandir la liste si nécessaire
            while (list.size() <= index) {
                list.add(null);
            }

            Object element = list.get(index);
            if (element == null) {
                element = elementType.getDeclaredConstructor().newInstance();
                list.set(index, element);
            }

            if (remaining == null) {
                // affectation finale
                list.set(index, Utils.convert(value, elementType));
                return;
            }

            // Récursion pour la suite du chemin
            populateRecursive(element, remaining, value);
            return;
        }

        // ============================================================
        // 2. CAS OBJET NON PRIMITIF (manager, parent...)
        // ============================================================
        if (!Utils.isPrimitive(fieldType)) {

            if (fieldValue == null) {
                fieldValue = fieldType.getDeclaredConstructor().newInstance();
                field.set(currentObj, fieldValue);
            }

            if (remaining == null) {
                field.set(currentObj, Utils.convert(value, fieldType));
                return;
            }

            populateRecursive(fieldValue, remaining, value);
            return;
        }

        // ============================================================
        // 3. CAS FINAL : champ simple (name, age, city, ...)
        // ============================================================
        if (remaining == null) {
            field.set(currentObj, Utils.convert(value, fieldType));
        }
    }

    /**
     * Extrait les paramètres d'un tableau
     * Exemple: e[0].name=Alice, e[1].name=Bob, e[0].age=25
     * Retourne: {0 -> {name=Alice, age=25}, 1 -> {name=Bob}}
     */
    private static Map<Integer, Map<String, String>> extractArrayParameters(
            Map<String, String[]> allParams,
            String paramName) {

        Map<Integer, Map<String, String>> result = new HashMap<>();

        for (Map.Entry<String, String[]> entry : allParams.entrySet()) {
            String key = entry.getKey();

            // Vérifier si le paramètre correspond au pattern: paramName[index]...
            if (key.startsWith(paramName + "[")) {
                try {
                    // Extraire l'index et le reste du chemin
                    String afterParamName = key.substring(paramName.length());
                    Integer index = Utils.extractIndex(afterParamName);

                    if (index != null) {
                        String fieldPath = afterParamName.substring(afterParamName.indexOf("]") + 1);

                        // Enlever le "." au début si présent
                        if (fieldPath.startsWith(".")) {
                            fieldPath = fieldPath.substring(1);
                        }

                        // Créer la map pour cet index si elle n'existe pas
                        if (!result.containsKey(index)) {
                            result.put(index, new HashMap<>());
                        }

                        // Ajouter le champ et sa valeur
                        result.get(index).put(fieldPath, entry.getValue()[0]);

                        System.out.println("  - Extraction : " + paramName + "[" + index + "]." + fieldPath
                                + " = " + entry.getValue()[0]);
                    }
                } catch (Exception e) {
                    System.out.println("Erreur lors de l'extraction de : " + key);
                }
            }
        }

        return result;
    }

    /**
     * Crée un tableau d'objets à partir des données extraites
     */
    private static Object populateArray(Class<?> componentType, Map<Integer, Map<String, String>> arrayData)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        // Trouver la taille maximale du tableau
        int maxIndex = arrayData.keySet().stream().max(Integer::compareTo).orElse(0);
        Object array = Array.newInstance(componentType, maxIndex + 1);

        // Remplir chaque élément du tableau
        for (Map.Entry<Integer, Map<String, String>> entry : arrayData.entrySet()) {
            int index = entry.getKey();
            Map<String, String> objectFields = entry.getValue();

            Object element;
            if (Utils.isPrimitive(componentType)) {
                // Si c'est un type primitif, prendre directement la valeur
                if (objectFields.size() == 1) {
                    String value = objectFields.values().iterator().next();
                    element = Utils.convert(value, componentType);
                } else {
                    element = null;
                }
            } else {
                // Si c'est un objet, utiliser populateObject
                element = populateObject(componentType, objectFields);
            }

            Array.set(array, index, element);
        }

        return array;
    }

    /*
     * Methode manala any valeur anle parametre ao anaty requetes
     * Dia par rapport amininy valeur reny no icréena anle Map String, String vaovao
     * 
     * Micréer Map vaovao aminy le Map taloha
     */
    private static Map<String, String> extractAllParameters(HttpServletRequest req) {
        Map<String, String> dataMap = new HashMap<>();

        // 1. Récupérer tous les paramètres de la requête (GET ou POST)
        Enumeration<String> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = req.getParameter(paramName);
            dataMap.put(paramName, paramValue);
            System.out.println("  - Ajout parametre : " + paramName + " = " + paramValue);
        }

        // 2. Récupérer les variables de path (ex: /user/{id}/detail)
        Object pathVarsAttr = req.getAttribute("pathVars");
        if (pathVarsAttr instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> pathVars = (Map<String, String>) pathVarsAttr;
            for (Map.Entry<String, String> entry : pathVars.entrySet()) {
                dataMap.put(entry.getKey(), entry.getValue());
                System.out.println("  - Ajout variable path : " + entry.getKey() + " = " + entry.getValue());
            }
        }

        return dataMap;
    }

}