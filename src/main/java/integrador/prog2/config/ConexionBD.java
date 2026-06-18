package integrador.prog2.config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


//Donde estan los "..." puntos suspensivos va el nombre de la base de datos que creemos, en este caso es "food_store_db"
public class ConexionBD {
    // URL de conexión con parámetros obligatorios (Timezone y SSL deshabilitado para local)
    private static final String URL = "jdbc:mysql://localhost:3306/...?useSSL=false&serverTimezone=America/Argentina/Buenos_Aires&allowPublicKeyRetrieval=true";
    private static final String USER = "root"; // Cambialo por tu usuario de MySQL - sino se deja en root
    private static final String PASSWORD = ""; // Cambialo por tu contraseña (en XAMPP suele ser vacía) - si no estableciste una contraseña

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
