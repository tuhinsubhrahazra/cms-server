
package cms_servlet;

import db_pool.DBPool;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * @author soumya
 */
public class CreateUser extends HttpServlet {
    
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
            throws ServletException, IOException {
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
            JSONObject requestBody = new JSONObject(sb.toString());
            String email = requestBody.optString("email");

            if (email != null && !email.isEmpty()) { 
                String password = generateRandomPassword(12);
 
                String salt = generateSalt();
 
                String hashedPassword = hashPasswordPBKDF2(password, salt);
 
                String saltAndHashedPassword = salt + ":" + hashedPassword;
 
                try {
                    String sql = "INSERT INTO CMS_UserDetails (Email, Pwd) VALUES (?, ?)";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, email);
                    statement.setString(2, saltAndHashedPassword); // Save salt + hashed password
                    statement.executeUpdate();
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"error\": \"Database error: " + e.getMessage() + "\"}");
                    out.flush();
                    return;
                }
 
                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("password", password);  
                json.put("hashedPassword", hashedPassword);
                json.put("salt", salt);
                json.put("message", "Account created successfully");

                out.print(json.toString());
                out.flush();
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Missing email in JSON body.\"}");
                out.flush();
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Invalid or malformed JSON body.\"}");
            out.flush();
        }
    }
 
    private String generateRandomPassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#&!";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
 
    private String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
 
    private String hashPasswordPBKDF2(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        int iterations = 65536;
        int keyLength = 128; // bits
        char[] chars = password.toCharArray();
        byte[] saltBytes = Base64.getDecoder().decode(salt);

        PBEKeySpec spec = new PBEKeySpec(chars, saltBytes, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();

        return Base64.getEncoder().encodeToString(hash);
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
        processRequest(request, response);
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
        processRequest(request, response);
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
