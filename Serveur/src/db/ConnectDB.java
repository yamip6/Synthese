package db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;

import java.io.FileNotFoundException;
import java.io.IOException;

@SuppressWarnings("unused")
public class ConnectDB {    
	
    /** Name of the database */
	private String _dbname;
    /** Connection URL, host name */
    private String _url;
    /** User name */
    private String _user;
    /** Connection password */
    private String _passwd;
    /** Object defining a connection instance */
    private static Connection _conn;

    /**
     * Method to connect to the db
     * @param dbname : The name of the database
     * @param url : Connection URL
     * @param user : The user name
     * @param passwd : The password for the connection
     * @throws ClassNotFoundException : Exception thrown if a driver could not be loaded
     * @throws SQLException : Exception while connecting sql
     */
    public void connection(String dbname, String url, String user, String passwd) throws ClassNotFoundException, SQLException {        
        _dbname = dbname;
        _url = url;
        _user = user;
        _passwd = passwd;
            
        // It supports different drivers (mysql for testing)
        Class.forName("org.postgresql.Driver"); 
        // Class.forName("oracle.jdbc.OracleDriver");
        Class.forName("com.mysql.jdbc.Driver");
            
        _conn = DriverManager.getConnection(url + "/" + dbname, user, passwd);    
        
    } // connection()
    
    /**
     * Method for generating a single instance of connection
     * @param dbname : The name of the database
     * @param url : Connection URL
     * @param user : The user name
     * @param passwd : The password for the connection
     * @return The connection instance
     * @throws ClassNotFoundException : Exception thrown if a class of connection could not be found during the call connection
     * @throws SQLException : Exception while creating the instance of sql connection
     */
    public Connection getInstance(String dbname, String url, String user, String passwd) throws ClassNotFoundException, SQLException {
        if(_conn == null)
            new ConnectDB().connection(dbname, url, user, passwd);
        return _conn;
        
    } // getInstance()
    
    /**
     * Method to perform a search request (selection)
     * @param query : SQL query to execute
     * @throws SQLException : Exception thrown during the execution of the query
     */
    public static ResultSet dbSelect(String query) throws SQLException {   
        Statement state = _conn.createStatement();
        return state.executeQuery(query);
        
    } // dbSelect()
    
    /**
     * Method to perform a modification query (deleting, ...) of the database
     * @param query : SQL query to execute
     * @throws SQLException : Exception thrown during the execution of the query
     */
    public static void dbManipulate(String query) throws SQLException {
        Statement state = _conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        state.executeUpdate(query);
        state.close();  
        
    } // dbManipulate()
    
    /**
     * Method to connect to the db
     * @throws ClassNotFoundException : Exception thrown during retrieval of a file
     * @throws SQLException : Exception thrown while connecting to db
     */
    public static void connect() throws ClassNotFoundException, SQLException { // DEBUG method
        String db = "exchange_group";
        String url = "jdbc:mysql://localhost:3306";
        String user = "root";
        String passwd = "";
        
        ConnectDB connect = new ConnectDB();
        connect.getInstance(db, url, user, passwd);
        
    } // connect()
    
    /**
     * Main method containing the unit test class methods
     * @param args
     */
    public static void main(String[] args) throws FileNotFoundException, IOException{  
        try {
            assert false;
            throw new RuntimeException("JVM option '-ea'");
        } catch (AssertionError e) {} 
        
        // Connection to the database
        String bdd = "exchange_group";
        String url = "jdbc:mysql://localhost:3306";
        String user = "root";
        String passwd = "";

        try{
            ConnectDB connect = new ConnectDB();
            assert null != connect.getInstance(bdd, url, user, passwd);
            dbSelect("SELECT * FROM members WHERE id = 1");
            dbManipulate("DELETE FROM members WHERE username = 'test'");
            dbManipulate("UPDATE members SET username = 'test' WHERE id = 1");
            dbManipulate("INSERT INTO members VALUES ('test', 'pass')");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                _conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
    } // main ()
    
} // ConnectBdd
