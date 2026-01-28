package org.Util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;

import org.Util.Exception.ForbiddenException;
import org.Util.Exception.UnauthorizedException;
import org.annotation.security.Authorized;
import org.annotation.security.Role;

public class SecurityInterceptor {
    
    /**
     * Vérifie si l'utilisateur a l'autorisation d'accéder à cette méthode
     */
    public static void checkAuthorization(Method method, HttpServletRequest req) 
            throws UnauthorizedException, ForbiddenException {
        
        HttpSession session = req.getSession(false);
        
        // Vérifier @Authorized
        if (method.isAnnotationPresent(Authorized.class)) {
            if (session == null || session.getAttribute("user") == null) {
                throw new UnauthorizedException("Vous devez être connecté pour accéder à cette ressource");
            }
        }
        
        // Vérifier @Role
        if (method.isAnnotationPresent(Role.class)) {
            if (session == null) {
                throw new UnauthorizedException("Vous devez être connecté");
            }
            
            Role roleAnnotation = method.getAnnotation(Role.class);
            String requiredRole = roleAnnotation.value();
            
            Object userRole = session.getAttribute("role");
            
            if (userRole == null || !userRole.toString().equals(requiredRole)) {
                throw new ForbiddenException(
                    "Accès refusé. Rôle requis : " + requiredRole
                );
            }
        }
    }
}