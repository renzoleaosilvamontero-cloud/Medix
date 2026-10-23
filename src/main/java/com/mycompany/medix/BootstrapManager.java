package com.mycompany.medix;

import com.mycompany.medix.conexion.ConexionBD;
import com.mycompany.medix.seguridad.PasswordService;
import java.io.Console;
import java.sql.*;

/** Creates the initial manager only when the application has no accounts yet. */
public final class BootstrapManager {
  private BootstrapManager() {}
  public static void main(String[] args) throws Exception {
    Console console=System.console();
    if(console==null) throw new IllegalStateException("Ejecuta este comando desde una terminal interactiva.");
    String username=console.readLine("Usuario inicial de Medix: ");
    char[] name=console.readPassword("Nombre del gerente: ");
    String password=PasswordService.temporaryPassword();
    String displayName=new String(name);
    String hash=PasswordService.hash(password.toCharArray());
    try(Connection c=ConexionBD.conectar()){
      c.setAutoCommit(false);
      try(Statement check=c.createStatement();ResultSet r=check.executeQuery("SELECT COUNT(*) FROM app_users")){
        r.next();if(r.getLong(1)>0)throw new SQLException("Ya existen cuentas; el alta inicial está deshabilitada.");
      }
      try(PreparedStatement w=c.prepareStatement("INSERT INTO workers(display_name) VALUES(?)",Statement.RETURN_GENERATED_KEYS)){
        w.setString(1,displayName);w.executeUpdate();
        try(ResultSet keys=w.getGeneratedKeys()){
          if(!keys.next())throw new SQLException("No se pudo crear el gerente.");
          try(PreparedStatement u=c.prepareStatement("INSERT INTO app_users(username,password_hash,role,worker_id,must_change_password) VALUES(?,?,'MANAGER',?,1)")){
            u.setString(1,username.trim());u.setString(2,hash);u.setLong(3,keys.getLong(1));u.executeUpdate();
          }
        }
      }
      c.commit();console.printf("Cuenta creada. Usuario: %s%nContraseña temporal (se muestra una vez): %s%nCámbiala al iniciar sesión.%n",username,password);
    } finally { java.util.Arrays.fill(name,'\0'); }
  }
}
