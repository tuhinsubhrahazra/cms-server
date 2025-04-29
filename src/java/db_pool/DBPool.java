package db_pool;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.*;

/**
 *
 * @author soumya
 * @version 1.0
 */
public class DBPool {
    
    public static final String DATABASE_CLASSNAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final Logger logger = Logger.getLogger(DBPool.class.getName());
    private static final String PROPERTIES_FILE = "database.properties";

    static {
        try {
            // Load the JDBC driver class for SQL Server
            Class.forName(DATABASE_CLASSNAME);
        } catch (ClassNotFoundException ex) {
            logger.severe("JDBC Driver not found: " + ex.getMessage());
        }
    }

    public static Connection get() {
        Connection connection = null;
        try {
            String dbIP = getPropertyValues("DATABASE_IP");
            String dbName = getPropertyValues("DATABASE_NAME");
            String dbUserName = getPropertyValues("DATABASE_USERNAME");
            String dbPwd = getPropertyValues("DATABASE_PWD");
            
            String databaseURL = "jdbc:sqlserver://" + dbIP + ";database=" + dbName;
//            String databaseURL="jdbc:sqlserver://DESKTOP-J214C3P\\SQLEXPRESS;databaseName=cms";

            System.out.println("databaseURL: "+databaseURL);
            connection = DriverManager.getConnection(databaseURL, dbUserName, dbPwd);
        } catch (SQLException | IOException ex) {
            logger.severe("Error getting database connection: " + ex.getMessage());
            ex.printStackTrace();  // In a real-world app, avoid printing stack traces like this.
        }
        return connection;
    }

    private static String getPropertyValues(String propName) throws IOException {
        Properties prop = new Properties();
        try (InputStream inputStream = DBPool.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Property file not found in the classpath");
            }
            prop.load(inputStream);
        }
        return prop.getProperty(propName);
    }

}
