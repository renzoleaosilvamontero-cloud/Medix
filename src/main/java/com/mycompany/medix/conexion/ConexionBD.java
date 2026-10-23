package com.mycompany.medix.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Clase sencilla para abrir la conexión local de XAMPP (MariaDB). */
public class ConexionBD {
    private static final String URL = System.getenv().getOrDefault(
            "MEDIX_DB_URL", "jdbc:mariadb://localhost:3306/MEDIX_DB");
    private static final String USUARIO = System.getenv().getOrDefault("MEDIX_DB_USER", "root");
    private static final String CLAVE = System.getenv().getOrDefault("MEDIX_DB_PASSWORD", "");

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
