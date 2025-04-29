/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package cms_servlet;

import db_pool.DBPool;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

/**
 *
 * @author soumy
 */
public class FetchAllComplain extends HttpServlet {

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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        JSONArray dataArray = new JSONArray();

        try (Connection connection = DBPool.get()) {
            String query = "SELECT * FROM CMS_UserComplain ORDER BY SrNo DESC";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                JSONObject complain = new JSONObject();
                complain.put("SrNo", rs.getInt("SrNo"));
                complain.put("ComplaintId", rs.getString("ComplaintId"));
                complain.put("Email", rs.getString("Email"));
                complain.put("ProductType", rs.getString("ProductType"));
                complain.put("PhoneNumber", rs.getString("PhoneNumber"));
                complain.put("ComplainMsg", rs.getString("ComplainMsg"));
                complain.put("ComplainImg", rs.getString("ComplainImg"));
                complain.put("ComplainAudio", rs.getString("ComplainAudio"));
                complain.put("Status", rs.getString("Status"));
                complain.put("DateTimeStamp", rs.getString("DateTimeStamp"));

                dataArray.put(complain);
            }

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Complaints fetched successfully");
            jsonResponse.put("data", dataArray);

        } catch (Exception e) {
            try {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Error fetching complaints: " + e.getMessage());
                jsonResponse.put("data", new JSONArray());
            } catch (Exception innerEx) {
                System.out.println("JSON creation error: " + innerEx);
            }

            System.out.println("Exception in FetchAllComplain: " + e);
        }

        out.print(jsonResponse.toString());
        out.flush();
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
