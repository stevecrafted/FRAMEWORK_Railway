package org;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

import java.io.*;
import java.util.Map;
import java.util.HashMap;

import org.Entity.ClassMethodUrl;
import org.Entity.JsonResponse;
import org.Entity.ModelView;
import org.Util.CmuUtils;
import org.Util.JsonConverter;
import org.Util.ParameterMapper;
import org.Util.SecurityInterceptor;
import org.Util.Exception.ForbiddenException;
import org.Util.Exception.UnauthorizedException;
import org.annotation.Json;
import org.custom.CustomReflections;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

// AJOUTEZ CETTE ANNOTATION ICI
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class FrontServlet extends HttpServlet {

    private RequestDispatcher defaultDispatcher;
    private CustomReflections reflections;
    Map<String, ClassMethodUrl> urlMappings = new HashMap<>();

    @Override
    public void init() {
        defaultDispatcher = getServletContext().getNamedDispatcher("default");

        reflections = new CustomReflections(
                "org.example");

        // Sauvegardena anaty classeMethodeUrl daolo izay mampiasa anle Annotation
        // namboarina
        System.out.println("---------- Sauvegarde des url ----------");
        CmuUtils.saveCmuList(reflections, urlMappings);

        ServletContext context = getServletContext();
        context.setAttribute("urlMappings", urlMappings);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        ClassMethodUrl cmu;

        try {
            // Jerena raha misy ao amle Url mapping le url miditra io
            cmu = CmuUtils.findMapping(path, urlMappings, req);

            resp.setContentType("text/plain; charset=UTF-8");

            // resp.getWriter().write("ETU003861" + "\n");
            // resp.getWriter().write("FrontServlet a reçu : " + req.getRequestURL() + "\n");

            if (cmu != null) {
                Method methode = cmu.getMyMethod();

                // ========== SPRINT 11 : VÉRIFICATION DE SÉCURITÉ ==========
                try {
                    SecurityInterceptor.checkAuthorization(methode, req);
                } catch (UnauthorizedException e) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("401 Unauthorized: " + e.getMessage());
                    return;
                } catch (ForbiddenException e) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write("403 Forbidden: " + e.getMessage());
                    return;
                }

                Parameter[] methodParameters = methode.getParameters();

                /**
                 * Sprint 6 : Matching des parametres entre l'url get ou
                 * post par formulaire avec les attributs
                 * de la methode
                 */
                Object[] methodArgs = ParameterMapper.mapParameters(methodParameters, req, methode);

                // printToClient(resp, "Cette url existe dans la classe " + cmu.getMyClass() + " dans la methode "
                //         + cmu.getMyMethod());

                // Vérifier si la méthode a l'annotation @Json
                boolean isJsonResponse = methode.isAnnotationPresent(Json.class);
                if (isJsonResponse) {
                    handleJsonResponse(resp, cmu, methodArgs);
                }

                // Exécution selon le type de retour
                if (cmu.getMyMethod().getReturnType() == String.class) {
                    // printToClient(resp, "Cette methode renvoie un String\n");

                    // Invocation avec les arguments
                    String result = cmu.ExecuteMethodeString(methodArgs);

                    // printToClient(resp, "Resultat de la fonction : \n");
                    printToClient(resp, result);

                } else if (cmu.getMyMethod().getReturnType() == ModelView.class) {
                    // printToClient(resp, "Cette methode renvoie un Model View\n");

                    // Ato no mandefa ny attribute rehetra any am client
                    String result = cmu.ExecuteMethodeModelView(req, methodArgs);

                    // Affichage du resultat
                    defaultDispatcher = req.getRequestDispatcher("/" + result);
                    defaultDispatcher.forward(req, resp);

                } else {
                    printToClient(resp, "Sady tsy String no tsy Model View ny averiny");
                }

            } else {
                printToClient(resp, "Error 404");
            }
        } catch (Exception e) {
            e.printStackTrace();
            printToClient(resp, "Erreur : " + e.getMessage());
        }
    }

    /**
     * Sprint 9 : Gère les réponses JSON avec l'annotation @Json
     */
    private void handleJsonResponse(HttpServletResponse resp, ClassMethodUrl cmu, Object[] methodArgs)
            throws Exception {

        resp.setContentType("application/json; charset=UTF-8");

        try {
            // Exécuter la méthode du contrôleur
            Object result = cmu.executeMethod(methodArgs);

            // Créer la réponse JSON standardisée
            JsonResponse jsonResponse = JsonResponse.success(result);

            // Convertir en JSON et envoyer
            String json = JsonConverter.toJson(jsonResponse);
            resp.getWriter().write(json);

            System.out.println("Réponse JSON envoyée : " + json);

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonError(resp, e.getMessage(), 500);
        }
    }

    private void sendJsonError(HttpServletResponse resp, String message, int code) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setStatus(code);

        JsonResponse errorResponse = JsonResponse.error(message, code);
        String json = JsonConverter.toJson(errorResponse);

        resp.getWriter().write(json);
    }

    public void printToClient(HttpServletResponse resp, String message) throws IOException {
        resp.getWriter().write(message);
    }

}
