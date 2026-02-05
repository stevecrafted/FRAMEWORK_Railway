package org.custom;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Classe Reflection personnalisÃ©e qui scanne les packages
 * et trouve wles classes/mÃ©thodes annotÃ©es
 * 
 * FONCTIONNEMENT:
 * 1. Scanne le classpath pour trouver toutes les classes dans un package
 * 2. Charge ces classes en mÃ©moire
 * 3. Inspecte leurs annotations
 * 4. Stocke les rÃ©sultats dans des Sets pour un accÃ¨s rapide
 */
public class CustomReflections {
    
    // Package Ã  scanner (ex: "org.example")
    private String basePackage;
    
    // Stockage des classes trouvÃ©es
    private Set<Class<?>> allClasses;
    
    // Cache des classes par annotation
    private Map<Class<? extends Annotation>, Set<Class<?>>> classesAnnotatedWith;
    
    // Cache des mÃ©thodes par annotation
    private Map<Class<? extends Annotation>, Set<Method>> methodsAnnotatedWith;
    
    /**
     * Constructeur: initialise le scanner
     * 
     * @param basePackage Package de base Ã  scanner (ex: "org.example")
     */
    public CustomReflections(String basePackage) {
        this.basePackage = basePackage;
        this.allClasses = new HashSet<>();
        this.classesAnnotatedWith = new HashMap<>();
        this.methodsAnnotatedWith = new HashMap<>();
        
        System.out.println(" Initialisation du scanner pour: " + basePackage);
        
        // Lancer le scan au moment de la crÃ©ation
        scanPackage();
    }
    
    /**
     * Ã‰TAPE 1: Scanner le package pour trouver toutes les classes
     */
    private void scanPackage() {
        try {
            // Convertir le nom du package en chemin de fichier
            // "org.example" -> "org/example"
            String path = basePackage.replace('.', '/');
            
            // Obtenir l'URL du package dans le classpath
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(path);
            
            // Parcourir toutes les ressources trouvÃ©es
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();
                
                if ("file".equals(protocol)) {
                    String decodedPath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8.name());
                    File directory = new File(decodedPath);
                    if (directory.exists()) {
                        scanDirectory(directory, basePackage);
                    }
                } else if ("jar".equals(protocol)) {
                    scanJar(resource, path);
                } else {
                    System.out.println("Protocole non supporte pour le scan: " + protocol);
                }
            }
            
            System.out.println("âœ“ Scan terminÃ©: " + allClasses.size() + " classes trouvÃ©es");
            
        } catch (IOException e) {
            System.err.println("Erreur lors du scan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Scanner un JAR pour trouver les classes d'un package
     */
    private void scanJar(URL resource, String path) {
        try {
            JarURLConnection connection = (JarURLConnection) resource.openConnection();
            JarFile jarFile = connection.getJarFile();
            
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                
                if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    loadClass(className);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du scan du JAR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ã‰TAPE 2: Scanner rÃ©cursivement un rÃ©pertoire
     * 
     * @param directory RÃ©pertoire Ã  scanner
     * @param packageName Nom du package actuel
     */
    private void scanDirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                // Sous-package: scanner rÃ©cursivement
                scanDirectory(file, packageName + "." + file.getName());
                
            } else if (file.getName().endsWith(".class")) {
                // Fichier .class trouvÃ©: charger la classe
                String className = packageName + "." 
                                 + file.getName().substring(0, file.getName().length() - 6);
                
                loadClass(className);
            }
        }
    }
    
    private void loadClass(String className) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<?> clazz = Class.forName(className, false, classLoader);
            allClasses.add(clazz);
            
        } catch (ClassNotFoundException e) {
            System.err.println("Impossible de charger: " + className);
        }
    }
    
    /**
     * Ã‰TAPE 3: Trouver toutes les classes annotÃ©es avec une annotation spÃ©cifique
     * 
     * @param annotation L'annotation Ã  rechercher
     * @return Set de classes annotÃ©es
     */
    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        
        // VÃ©rifier si dÃ©jÃ  en cache
        if (classesAnnotatedWith.containsKey(annotation)) {
            return classesAnnotatedWith.get(annotation);
        }
        
        System.out.println(" Recherche des classes annotÃ©es avec: @" + annotation.getSimpleName());
        
        Set<Class<?>> result = new HashSet<>();
        
        // Parcourir toutes les classes chargÃ©es
        for (Class<?> clazz : allClasses) {
            
            // VÃ©rifier si la classe a l'annotation
            if (clazz.isAnnotationPresent(annotation)) {
                result.add(clazz);
                System.out.println("  âœ“ TrouvÃ©: " + clazz.getName());
            }
        }
        
        // Mettre en cache
        classesAnnotatedWith.put(annotation, result);
        
        System.out.println("Total: " + result.size() + " classe(s) trouvÃ©e(s)\n");
        
        return result;
    }
    
    /**
     * Ã‰TAPE 4: Trouver toutes les mÃ©thodes annotÃ©es avec une annotation spÃ©cifique
     * 
     * @param annotation L'annotation Ã  rechercher
     * @return Set de mÃ©thodes annotÃ©es
     */
    public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
        
        // VÃ©rifier si dÃ©jÃ  en cache
        if (methodsAnnotatedWith.containsKey(annotation)) {
            return methodsAnnotatedWith.get(annotation);
        }
        
        System.out.println("ðŸ”Ž Recherche des mÃ©thodes annotÃ©es avec: @" + annotation.getSimpleName());
        
        Set<Method> result = new HashSet<>();
        
        // Parcourir toutes les classes
        for (Class<?> clazz : allClasses) {
            
            // RÃ©cupÃ©rer toutes les mÃ©thodes de la classe
            Method[] methods = clazz.getDeclaredMethods();
            
            for (Method method : methods) {
                
                // VÃ©rifier si la mÃ©thode a l'annotation
                if (method.isAnnotationPresent(annotation)) {
                    result.add(method);
                    System.out.println("  âœ“ TrouvÃ©: " + clazz.getSimpleName() 
                                     + "." + method.getName() + "()");
                }
            }
        }
        
        // Mettre en cache
        methodsAnnotatedWith.put(annotation, result);
        
        System.out.println("Total: " + result.size() + " mÃ©thode(s) trouvÃ©e(s)\n");
        
        return result;
    }
    
    /**
     * Obtenir toutes les classes scannÃ©es
     */
    public Set<Class<?>> getAllClasses() {
        return new HashSet<>(allClasses);
    }
    
    /**
     * Afficher un rÃ©sumÃ© du scan
     */
    public void printSummary() {
        System.out.println("\n========== RÃ‰SUMÃ‰ DU SCAN ==========");
        System.out.println("Package de base: " + basePackage);
        System.out.println("Classes trouvÃ©es: " + allClasses.size());
        System.out.println("Types d'annotations scannÃ©es: " + classesAnnotatedWith.size());
        System.out.println("=====================================\n");
    }
}
