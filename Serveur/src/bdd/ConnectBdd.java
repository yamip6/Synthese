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
     * Nom de la base de données
     */
	private String _nomBdd;
    /**
     * URL de connexion, nom de l'hôte
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
     * Objet définissant une instance de connexion
     */
    private static Connection _conn;

    /**
     * Méthode permettant d'établir la connexion avec la bdd
     * @param Nom_bdd : String repréentant le nom de la base de données
     * @param url : String définissant l'url de connexion
     * @param user : String donnant le nom d'utilisateur
     * @param passwd : String représentant le mot de passe de connexion
     * @throws ClassNotFoundException : Exception levée si un driver n'a pu être chargé
     * @throws SQLException : Exception durant la connexion sql
     */
    public void connection(String Nom_bdd, String url, String user, String passwd) throws ClassNotFoundException, SQLException {        
        _nomBdd = Nom_bdd;
        _url = url;
        _user = user;
        _passwd = passwd;
            
        // On charge différents drivers (mysql pour les tests)
        Class.forName("org.postgresql.Driver"); 
        //Class.forName("oracle.jdbc.OracleDriver");
        Class.forName("com.mysql.jdbc.Driver");
            
        _conn = DriverManager.getConnection(url + "/" + Nom_bdd, user, passwd);      
    } // connection()
    
    /**
     * Méthode permettant de générer une instance unique de connexion
     * @param Nom_bdd : String repréentant le nom de la base de données
     * @param url : String définissant l'url de connexion
     * @param user : String donnant le nom d'utilisateur
     * @param passwd : String représentant le mot de passe de connexion
     * @return Connection : Objet représentant l'instance de connexion
     * @throws ClassNotFoundException : Exception levée si une classe de connexion n'a pu être trouvée durant l'appel à connection
     * @throws SQLException : Exception durant la création de l'instance de connexion sql
     */
    public Connection getInstance(String Nom_bdd, String url, String user, String passwd) throws ClassNotFoundException, SQLException {
        if(_conn == null)
            new ConnectBdd().connection(Nom_bdd, url, user, passwd);
        return _conn;
    } // getInstance()
    
    /**
     * Méthode permettant d'exécuter une requète de recherche (sélection)
     * @param requete : String représentant la requète SQL à exécuter
     * @throws SQLException : Exception levée durant l'exécution de la requète
     */
    public static ResultSet rechercheBdd(String requete) throws SQLException {   
        Statement state = _conn.createStatement();
        return state.executeQuery(requete);
    } // rechercheBdd()
    
    /**
     * Méthode permettant d'exécuter une requète de modification (suppression, ...) de la bdd
     * @param requete : String représentant la requète SQL à exécuter
     * @throws SQLException : Exception levée durant l'exécution de la requète SQL
     */
    public static void manipulerBdd(String requete) throws SQLException {
        Statement state = _conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        state.executeUpdate(requete);
        state.close();  
    } // modifierBdd()
    
    /**
     * Méthode permettant de se connecter à la bdd
     * @throws ClassNotFoundException : Exception levée durant la récupération d'un fichier
     * @throws SQLException : Exception levée durant la connexion à la bdd
     */
    public static void connecter() throws ClassNotFoundException, SQLException { // Méthode de debug
        String bdd = "exchange_group";
        String url = "jdbc:mysql://localhost:3306";
        String user = "root";
        String passwd = "";
        
        ConnectBdd connect = new ConnectBdd();
        connect.getInstance(bdd, url, user, passwd);
    } // connecter()
    
    /**
     * Méthode main regroupant les tests unitaire des méthodes de la classe
     * @param args
     */
    public static void main(String[] args) throws FileNotFoundException, IOException{  
        try {
            assert false;
            throw new RuntimeException("JVM option '-ea'");
        } catch (AssertionError e) {} 
        
        // Connexion à la bdd
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
            System.err.println("Erreur durant l'exécution d'une requète SQL (ou durant la connexion).");
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