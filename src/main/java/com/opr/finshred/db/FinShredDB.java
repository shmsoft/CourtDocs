package com.opr.finshred.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local db (Derby, also known as Java DB) operations.
 * This is taken out for now, for simplicity, until some time later.
 * @author mark
 */
public class FinShredDB {
    
    private static String dbURL = "jdbc:derby://localhost:1527/FinShred;create=true;user=opr;password=opr";
    private static Connection conn;
    private static Statement stmt = null;
    private static PreparedStatement pstmt = null;
    private static String settingTable = "SITE_SETTING";
    private final static Logger logger = LoggerFactory.getLogger(FinShredDB.class);

    /**
     * This should be called once, but no big harm if it is called many times.
     */
    public static void createConnection() {
//        logger.debug("Creating DB connection");
//        try {
//            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
//            //Get a connection
//            conn = DriverManager.getConnection(dbURL);
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
//            logger.error("Problem creating connection", e);
//        }
    }
    
    public static void closeConnection() {
//        logger.debug("Closing DB connection");
//        if (conn != null) {
//            try {
//                conn.close();
//            } catch (SQLException e) {
//                logger.warn("Could not close connection");
//            }
//        }
    }

    public void readSettings() {        
        try {
            stmt = conn.createStatement();
            try (ResultSet results = stmt.executeQuery("select * from " + settingTable)) {
                ResultSetMetaData rsmd = results.getMetaData();
                int numberCols = rsmd.getColumnCount();
                for (int i = 1; i <= numberCols; i++) {
                    //print Column Names
                    System.out.print(rsmd.getColumnLabel(i) + "\t\t");
                }
                
                System.out.println("\n-------------------------------------------------");
                
                while (results.next()) {
                    String site = results.getString(1);
                    String cookie = results.getString(2);
                    System.out.println(site + "\t\t" + cookie);
                }
            }
            stmt.close();
        } catch (SQLException e) {
            logger.error("Problem reading settings.", e);
        }
    }
    
    public String getJpmCookie() {
        String cookie = "";
//        try {
//            pstmt = conn.prepareStatement("select cookie from " + settingTable + " where site = ?");
//            pstmt.setString(1, "JPM");
//            ResultSet results = pstmt.executeQuery();
//            if (results.next()) {
//                cookie = results.getString("cookie");
//            }
//        } catch (Exception e) {
//            logger.error("Problem reading JPM Cookie", e);
//        }
        return cookie;
    }

    public void saveJpmCookie(String cookie) {        
//        try {
//            pstmt = conn.prepareStatement("update " + settingTable + " set cookie = ? where site = ?");
//            pstmt.setString(1, cookie);
//            pstmt.setString(2, "JPM");
//            pstmt.executeUpdate();
//        } catch (Exception e) {
//            logger.error("Problem writing JPM Cookie", e);
//        }
    }
    /**
     * find the next run_id and prepare the row in the db, with run_time of the start of the run.
     * @return 
     */
    public int getNewRun() {
        int newRunId = 1;
//        try {
//            stmt = conn.createStatement();
//            ResultSet results = stmt.executeQuery("select max(run_id) from download_run");
//            if (results.next()) {
//                newRunId = results.getInt(1) + 1;
//            }            
//            pstmt = conn.prepareStatement("insert into download_run (run_id, run_time) values (?,?)");
//            pstmt.setInt(1, newRunId);
//            pstmt.setTimestamp(2, new Timestamp(new Date().getTime()));
//            pstmt.executeUpdate();
//        } catch (Exception e) {
//            logger.error("Problem creating new run id", e);
//        }
        return newRunId;        
    }
}
