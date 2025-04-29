/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package cms_servlet;

import db_pool.DBPool;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author soumya
 */
public class VerifyUser extends HttpServlet {
    
     @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // CORS headers for preflight (OPTIONS) request
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(HttpServletResponse.SC_OK);
    }
    

     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException, JSONException {

    // Allow cross-origin requests from localhost:3000 (or any domain you want)
    response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000"); // Replace with your frontend URL
    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    response.setHeader("Access-Control-Allow-Credentials", "true");

    // Handle preflight request for OPTIONS method
    if ("OPTIONS".equals(request.getMethod())) {
        response.setStatus(HttpServletResponse.SC_OK);
        return;
    }

    // Your existing logic for handling POST requests (authentication)
    Connection connection = DBPool.get();

    response.setContentType("application/json");
    response.setHeader("Set-Cookie", "HttpOnly;Secure;SameSite=Strict;Max-Age=0");
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
    response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; sandbox");

    PrintWriter out = response.getWriter();
    StringBuilder sb = new StringBuilder();
    String line;

    try (BufferedReader reader = request.getReader()) {
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
    }

    try {
        if (sb.length() == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("error", "Request body is empty or malformed.");
            out.print(jsonResponse.toString());
            out.flush();
            return;
        }

        JSONObject requestBody = new JSONObject(sb.toString());
        String email = requestBody.optString("email");
        String password = requestBody.optString("password");

        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            String storedSaltAndHashedPassword = getStoredSaltAndHashedPassword(connection, email);

            if (storedSaltAndHashedPassword != null) {
                String[] parts = storedSaltAndHashedPassword.split(":");
                String storedSalt = parts[0];
                String storedHashedPassword = parts[1];

                if (password.equals(storedSaltAndHashedPassword)) {
                    JSONObject json = new JSONObject();
                    json.put("message", "Login successful");
                    out.print(json.toString());
                    out.flush();
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    JSONObject json = new JSONObject();
                    json.put("error", "Invalid password");
                    out.print(json.toString());
                    out.flush();
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                JSONObject json = new JSONObject();
                json.put("error", "Email not found");
                out.print(json.toString());
                out.flush();
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject json = new JSONObject();
            json.put("error", "Missing email or password");
            out.print(json.toString());
            out.flush();
        }
    } catch (org.json.JSONException e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("error", "Invalid or malformed JSON body. Make sure the body is a valid JSON.");
        out.print(jsonResponse.toString());
        out.flush();
    } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("error", "An error occurred: " + e.getMessage());
        out.print(jsonResponse.toString());
        out.flush();
    }
}
 
    private String getStoredSaltAndHashedPassword(Connection connection, String email) throws Exception {
        String sql = "SELECT Pwd FROM CMS_UserDetails WHERE Email = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, email);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getString("Pwd"); 
        }
        return null;  
    }


    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            Logger.getLogger(VerifyUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            Logger.getLogger(VerifyUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
