package com.mycompany.medix.config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public final class Database {
  private Database() {}
  public static Connection connect() throws SQLException {
    String url = System.getenv().getOrDefault("MEDIX_DB_URL", "jdbc:mysql://localhost:3306/medix?useSSL=true&serverTimezone=UTC");
    String user = System.getenv("MEDIX_DB_USER"), password = System.getenv("MEDIX_DB_PASSWORD");
    if (user == null || password == null) throw new SQLException("Configura MEDIX_DB_USER y MEDIX_DB_PASSWORD en el entorno.");
    return DriverManager.getConnection(url, user, password);
  }
}