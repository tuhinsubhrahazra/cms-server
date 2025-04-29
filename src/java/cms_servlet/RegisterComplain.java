 
package cms_servlet;

import db_pool.DBPool;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Random;

/**
 *
 * @author soumya
 */
public class RegisterComplain extends HttpServlet {
    
    private String generateUniqueComplaintId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();

        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }
 
    
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        StringBuilder sb = new StringBuilder();
        String line;
 
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        try { 
            JSONObject requestBody = new JSONObject(sb.toString());
 
            String productType = requestBody.optString("productType", "").trim();
            String textRiseComplain = requestBody.optString("textRiseComplain", "").trim();
            JSONArray uploadedImages = requestBody.optJSONArray("uploadedImages");
            String uploadedAudio = requestBody.optString("uploadedAudio", "").trim();
            String Email = requestBody.optString("Email", "").trim();
            String PhoneNumber = requestBody.optString("PhoneNumber", "").trim();
 
            if (productType.isEmpty() || textRiseComplain.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Missing required fields: productType or textRiseComplain.");
                out.write(jsonResponse.toString());
                return;
            }
 
            List<String> imageList = new ArrayList<>();
            if (uploadedImages != null) {
                for (int i = 0; i < uploadedImages.length(); i++) {
                    imageList.add(uploadedImages.getString(i));
                }
            }
            String imageBase64String = String.join(",", imageList); // Save all images as comma-separated base64
 
            String ComplaintId = generateUniqueComplaintId();
            String Status = "Pending";
            
            try (Connection conn = DBPool.get()) {
                String sql = "INSERT INTO CMS_UserComplain (ProductType, ComplainMsg, ComplainImg, ComplainAudio, Email, PhoneNumber, ComplaintId, Status) VALUES (?, ?, ?, ?, ?, ?,?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, productType);
                    ps.setString(2, textRiseComplain);
                    ps.setString(3, imageBase64String);
                    ps.setString(4, uploadedAudio);
                    ps.setString(5, Email);
                    ps.setString(6, PhoneNumber);
                    ps.setString(7, ComplaintId);
                    ps.setString(8, Status);
                    ps.executeUpdate();
                }
            }
 
            response.setStatus(HttpServletResponse.SC_OK);
            jsonResponse.put("stat", "success");
            jsonResponse.put("message", "Complaint saved successfully.");
            JSONObject data = new JSONObject();
            data.put("productType", productType);
            data.put("textRiseComplain", textRiseComplain);
            data.put("uploadedImages", uploadedImages != null ? uploadedImages : new JSONArray());
            data.put("uploadedAudio", uploadedAudio.isEmpty() ? JSONObject.NULL : uploadedAudio);
            data.put("PhoneNumber", PhoneNumber);
            data.put("complaintId", ComplaintId);
            data.put("Status", Status);
            jsonResponse.put("data", data);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Something went wrong: " + e.getMessage());
        }

        out.write(jsonResponse.toString());
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
             Logger.getLogger(RegisterComplain.class.getName()).log(Level.SEVERE, null, ex);
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
             Logger.getLogger(RegisterComplain.class.getName()).log(Level.SEVERE, null, ex);
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
