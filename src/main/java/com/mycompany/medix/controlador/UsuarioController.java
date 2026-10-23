package com.mycompany.medix.controlador;
import com.mycompany.medix.modelo.*;
import com.mycompany.medix.dao.MedixDAO;
import com.mycompany.medix.seguridad.PasswordService;
import java.sql.SQLException;
import java.util.Optional;
public class UsuarioController {
  private final MedixDAO repo;
  public UsuarioController(MedixDAO repo){this.repo=repo;}
  public Optional<Usuario> login(String user,char[] password)throws SQLException{return repo.authenticate(user,password);}
  public void changePassword(Usuario s,char[] old,char[] next)throws SQLException{repo.changePassword(s,old,next);}
  public String temporaryPassword(){return PasswordService.temporaryPassword();}
}