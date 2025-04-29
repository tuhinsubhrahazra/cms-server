package cms_servlet;

import db_pool.DBPool;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author soumy
 */
public class AddUserDetails extends HttpServlet {
    
     @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Set the necessary headers for preflight requests
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000"); // Adjust if necessary
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
     public static String generateUniqueUserId() {
        // Generate a random UUID and take the first 10 characters
        String uuid = UUID.randomUUID().toString().replaceAll("-", ""); // Remove dashes
        String userId = uuid.substring(0, 10); // Get the first 10 characters of the UUID
        return userId;
    }

   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException, SQLException, JSONException {
       response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000"); // Allow requests from React app
    response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
    response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    
    response.setContentType("application/json");

    // Read the request body
    StringBuilder stringBuilder = new StringBuilder();
    String line;
    try (BufferedReader reader = request.getReader()) {
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
    }

    try {
        // Parse JSON request
        JSONObject jsonData = new JSONObject(stringBuilder.toString());

        // Extract data from the request
        String name = jsonData.getString("name");
        String email = jsonData.getString("email");
        String userType = jsonData.getString("userType");
        String address = jsonData.getString("address");
        String phoneNumber = jsonData.getString("phoneNumber");

        // Employee specific data
        String employeeId = null;
        String employeeRoll = null;
        if (userType.equals("Employee")) {
            employeeId = jsonData.getString("employeeId");
            employeeRoll = jsonData.getString("employeeRoll");
        }

        // User specific data
        String userOrgId = null;
        String userSubType = null;
        String userExpiry = null;
        if (userType.equals("User")) {
            userOrgId = jsonData.getString("userOrgId");
            userSubType = jsonData.getString("userSubType");
            userExpiry = jsonData.getString("userExpiry");
        }
        
         String userId = generateUniqueUserId();

        // Here you can save this data to your database or process it as needed
        // Now call the stored procedure
        Connection connection = DBPool.get();

        if (connection == null || connection.isClosed()) {
            response.getWriter().println("Failed to establish a connection to the database.");
            return;
        }

        // SQL query to call the stored procedure
        String sql = "{CALL Proc_CMS_UserDetails(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        CallableStatement callableStatement = connection.prepareCall(sql);

        // Set the parameters for the stored procedure
        callableStatement.setString(1, userId); // Placeholder for userId
        callableStatement.setString(2, name);
        callableStatement.setString(3, email);
        callableStatement.setString(4, userType);
        callableStatement.setString(5, address);
        callableStatement.setString(6, phoneNumber);
        callableStatement.setString(7, userOrgId);
        callableStatement.setString(8, userSubType);
        callableStatement.setString(9, userExpiry);
        callableStatement.setString(10, employeeId);
        callableStatement.setString(11, employeeRoll);

        // Execute the stored procedure
        int rowsAffected = callableStatement.executeUpdate();

        // Check if the insertion was successful
        if (rowsAffected > 0) {
            // Send a successful response back
            JSONObject responseJson = new JSONObject();
            responseJson.put("message", "User details added successfully");

            response.getWriter().write(responseJson.toString());
        } else {
            // Send an error response if insertion failed
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Failed to insert user details into the database");
            response.getWriter().write(errorJson.toString());
        }

    } catch (Exception e) {
        // Handle exceptions and send an error response
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", "Failed to process the request: " + e.getMessage());
        response.getWriter().write(errorJson.toString());
    }
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
        } catch (SQLException ex) {
            Logger.getLogger(AddUserDetails.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
           Logger.getLogger(AddUserDetails.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (SQLException ex) {
            Logger.getLogger(AddUserDetails.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
           Logger.getLogger(AddUserDetails.class.getName()).log(Level.SEVERE, null, ex);
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
