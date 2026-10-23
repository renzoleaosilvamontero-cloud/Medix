package com.mycompany.medix.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Conexión a la base medix_db de XAMPP (MariaDB). */
public class ConexionBD {
    private static final String URL = System.getenv().getOrDefault(
            "MEDIX_DB_URL", "jdbc:mariadb://localhost:3306/medix_db");
    private static final String USUARIO = System.getenv().getOrDefault("MEDIX_DB_USER", "root");
    private static final String CLAVE = System.getenv().getOrDefault("MEDIX_DB_PASSWORD", "");

    private ConexionBD() {}

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
