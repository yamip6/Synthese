package bdd;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;

import java.io.FileNotFoundException;
import java.io.IOException;

@SuppressWarnings("unused")
public class ConnectBdd {    
    /**
     * Nom de la base de donn�es
     */
	private String _nomBdd;
    /**
     * URL de connexion, nom de l'h�te
     */
    private String _url;
    /**
     * Nom d'utilisateur
     */
    private String _user;
    /**
     * Mot de passe de connexion
     */
    private String _passwd;
    /**
     * Objet d�finissant une instance de connexion
     */
    private static Connection _conn;

    /**
     * M�thode permettant d'�tablir la connexion avec la bdd
     * @param Nom_bdd : String repr�entant le nom de la base de donn�es
     * @param url : String d�finissant l'url de connexion
     * @param user : String donnant le nom d'utilisateur
     * @param passwd : String repr�sentant le mot de passe de connexion
     * @throws ClassNotFoundException : Exception lev�e si un driver n'a pu �tre charg�
     * @throws SQLException : Exception durant la connexion sql
     */
    public void connection(String Nom_bdd, String url, String user, String passwd) throws ClassNotFoundException, SQLException {        
        _nomBdd = Nom_bdd;
        _url = url;
        _user = user;
        _passwd = passwd;
            
        // On charge diff�rents drivers (mysql pour les tests)
        Class.forName("org.postgresql.Driver"); 
        //Class.forName("oracle.jdbc.OracleDriver");
        Class.forName("com.mysql.jdbc.Driver");
            
        _conn = DriverManager.getConnection(url + "/" + Nom_bdd, user, passwd);      
    } // connection()
    
    /**
     * M�thode permettant de g�n�rer une instance unique de connexion
     * @param Nom_bdd : String repr�entant le nom de la base de donn�es
     * @param url : String d�finissant l'url de connexion
     * @param user : String donnant le nom d'utilisateur
     * @param passwd : String repr�sentant le mot de passe de connexion
     * @return Connection : Objet repr�sentant l'instance de connexion
     * @throws ClassNotFoundException : Exception lev�e si une classe de connexion n'a pu �tre trouv�e durant l'appel � connection
     * @throws SQLException : Exception durant la cr�ation de l'instance de connexion sql
     */
    public Connection getInstance(String Nom_bdd, String url, String user, String passwd) throws ClassNotFoundException, SQLException {
        if(_conn == null)
            new ConnectBdd().connection(Nom_bdd, url, user, passwd);
        return _conn;
    } // getInstance()
    
    /**
     * M�thode permettant d'ex�cuter une requ�te de recherche (s�lection)
     * @param requete : String repr�sentant la requ�te SQL � ex�cuter
     * @throws SQLException : Exception lev�e durant l'ex�cution de la requ�te
     */
    public static ResultSet rechercheBdd(String requete) throws SQLException {   
        Statement state = _conn.createStatement();
        return state.executeQuery(requete);
    } // rechercheBdd()
    
    /**
     * M�thode permettant d'ex�cuter une requ�te de modification (suppression, ...) de la bdd
     * @param requete : String repr�sentant la requ�te SQL � ex�cuter
     * @throws SQLException : Exception lev�e durant l'ex�cution de la requ�te SQL
     */
    public static void manipulerBdd(String requete) throws SQLException {
        Statement state = _conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        state.executeUpdate(requete);
        state.close();  
    } // modifierBdd()
    
    /**
     * M�thode permettant de se connecter � la bdd
     * @throws ClassNotFoundException : Exception lev�e durant la r�cup�ration d'un fichier
     * @throws SQLException : Exception lev�e durant la connexion � la bdd
     */
    public static void connecter() throws ClassNotFoundException, SQLException { // M�thode de debug
        String bdd = "exchange_group";
        String url = "jdbc:mysql://localhost:3306";
        String user = "root";
        String passwd = "";
        
        ConnectBdd connect = new ConnectBdd();
        connect.getInstance(bdd, url, user, passwd);
    } // connecter()
    
    /**
     * M�thode main regroupant les tests unitaire des m�thodes de la classe
     * @param args
     */
    public static void main(String[] args) throws FileNotFoundException, IOException{  
        try {
            assert false;
            throw new RuntimeException("JVM option '-ea'");
        } catch (AssertionError e) {} 
        
        // Connexion � la bdd
        String bdd = "exchange_group";
        String url = "jdbc:mysql://localhost:3306";
        String user = "root";
        String passwd = "";

        try{
            ConnectBdd connect = new ConnectBdd();
            assert null != connect.getInstance(bdd, url, user, passwd);
            rechercheBdd("SELECT * FROM members WHERE id = 1");
            manipulerBdd("DELETE FROM members WHERE username = 'test'");
            manipulerBdd("UPDATE members SET username = 'test' WHERE id = 1");
            manipulerBdd("INSERT INTO members VALUES ('test', 'pass')");
        } catch (SQLException e) {
            System.err.println("Erreur durant l'ex�cution d'une requ�te SQL (ou durant la connexion).");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Impossible de charger les drivers de la bdd.");
            e.printStackTrace();
        } finally {
            try {
                _conn.close();
            } catch (SQLException e) {
                System.err.println("Impossible de fermer la connexion SQL.");
                e.printStackTrace();
            }
        }
    } // main ()
    
} // ConnectBdd