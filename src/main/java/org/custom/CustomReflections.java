package org.custom;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Classe Reflection personnalisée qui scanne les packages
 * et trouve wles classes/méthodes annotées
 * 
 * FONCTIONNEMENT:
 * 1. Scanne le classpath pour trouver toutes les classes dans un package
 * 2. Charge ces classes en mémoire
 * 3. Inspecte leurs annotations
 * 4. Stocke les résultats dans des Sets pour un accès rapide
 */
public class CustomReflections {
    
    // Package à scanner (ex: "org.example")
    private String basePackage;
    
    // Stockage des classes trouvées
    private Set<Class<?>> allClasses;
    
    // Cache des classes par annotation
    private Map<Class<? extends Annotation>, Set<Class<?>>> classesAnnotatedWith;
    
    // Cache des méthodes par annotation
    private Map<Class<? extends Annotation>, Set<Method>> methodsAnnotatedWith;
    
    /**
     * Constructeur: initialise le scanner
     * 
     * @param basePackage Package de base à scanner (ex: "org.example")
     */
    public CustomReflections(String basePackage) {
        this.basePackage = basePackage;
        this.allClasses = new HashSet<>();
        this.classesAnnotatedWith = new HashMap<>();
        this.methodsAnnotatedWith = new HashMap<>();
        
        System.out.println(" Initialisation du scanner pour: " + basePackage);
        
        // Lancer le scan au moment de la création
        scanPackage();
    }
    
    /**
     * ÉTAPE 1: Scanner le package pour trouver toutes les classes
     */
    private void scanPackage() {
        try {
            // Convertir le nom du package en chemin de fichier
            // "org.example" -> "org/example"
            String path = basePackage.replace('.', '/');
            
            // Obtenir l'URL du package dans le classpath
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(path);
            
            // Parcourir toutes les ressources trouvées
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File directory = new File(resource.getFile());
                
                if (directory.exists()) {
                    scanDirectory(directory, basePackage);
                }
            }
            
            System.out.println("✓ Scan terminé: " + allClasses.size() + " classes trouvées");
            
        } catch (IOException e) {
            System.err.println("Erreur lors du scan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * ÉTAPE 2: Scanner récursivement un répertoire
     * 
     * @param directory Répertoire à scanner
     * @param packageName Nom du package actuel
     */
    private void scanDirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                // Sous-package: scanner récursivement
                scanDirectory(file, packageName + "." + file.getName());
                
            } else if (file.getName().endsWith(".class")) {
                // Fichier .class trouvé: charger la classe
                String className = packageName + "." 
                                 + file.getName().substring(0, file.getName().length() - 6);
                
                try {
                    Class<?> clazz = Class.forName(className);
                    allClasses.add(clazz);
                    
                } catch (ClassNotFoundException e) {
                    System.err.println("Impossible de charger: " + className);
                }
            }
        }
    }
    
    /**
     * ÉTAPE 3: Trouver toutes les classes annotées avec une annotation spécifique
     * 
     * @param annotation L'annotation à rechercher
     * @return Set de classes annotées
     */
    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        
        // Vérifier si déjà en cache
        if (classesAnnotatedWith.containsKey(annotation)) {
            return classesAnnotatedWith.get(annotation);
        }
        
        System.out.println("🔎 Recherche des classes annotées avec: @" + annotation.getSimpleName());
        
        Set<Class<?>> result = new HashSet<>();
        
        // Parcourir toutes les classes chargées
        for (Class<?> clazz : allClasses) {
            
            // Vérifier si la classe a l'annotation
            if (clazz.isAnnotationPresent(annotation)) {
                result.add(clazz);
                System.out.println("  ✓ Trouvé: " + clazz.getName());
            }
        }
        
        // Mettre en cache
        classesAnnotatedWith.put(annotation, result);
        
        System.out.println("Total: " + result.size() + " classe(s) trouvée(s)\n");
        
        return result;
    }
    
    /**
     * ÉTAPE 4: Trouver toutes les méthodes annotées avec une annotation spécifique
     * 
     * @param annotation L'annotation à rechercher
     * @return Set de méthodes annotées
     */
    public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
        
        // Vérifier si déjà en cache
        if (methodsAnnotatedWith.containsKey(annotation)) {
            return methodsAnnotatedWith.get(annotation);
        }
        
        System.out.println("🔎 Recherche des méthodes annotées avec: @" + annotation.getSimpleName());
        
        Set<Method> result = new HashSet<>();
        
        // Parcourir toutes les classes
        for (Class<?> clazz : allClasses) {
            
            // Récupérer toutes les méthodes de la classe
            Method[] methods = clazz.getDeclaredMethods();
            
            for (Method method : methods) {
                
                // Vérifier si la méthode a l'annotation
                if (method.isAnnotationPresent(annotation)) {
                    result.add(method);
                    System.out.println("  ✓ Trouvé: " + clazz.getSimpleName() 
                                     + "." + method.getName() + "()");
                }
            }
        }
        
        // Mettre en cache
        methodsAnnotatedWith.put(annotation, result);
        
        System.out.println("Total: " + result.size() + " méthode(s) trouvée(s)\n");
        
        return result;
    }
    
    /**
     * Obtenir toutes les classes scannées
     */
    public Set<Class<?>> getAllClasses() {
        return new HashSet<>(allClasses);
    }
    
    /**
     * Afficher un résumé du scan
     */
    public void printSummary() {
        System.out.println("\n========== RÉSUMÉ DU SCAN ==========");
        System.out.println("Package de base: " + basePackage);
        System.out.println("Classes trouvées: " + allClasses.size());
        System.out.println("Types d'annotations scannées: " + classesAnnotatedWith.size());
        System.out.println("=====================================\n");
    }
}