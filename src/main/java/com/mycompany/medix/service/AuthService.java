package com.mycompany.medix.service;
import com.mycompany.medix.domain.*;
import com.mycompany.medix.repository.MedixRepository;
import com.mycompany.medix.security.PasswordService;
import java.sql.SQLException;
import java.util.Optional;
public class AuthService {
  private final MedixRepository repo;
  public AuthService(MedixRepository repo){this.repo=repo;}
  public Optional<Session> login(String user,char[] password)throws SQLException{return repo.authenticate(user,password);}
  public void changePassword(Session s,char[] old,char[] next)throws SQLException{repo.changePassword(s,old,next);}
  public String temporaryPassword(){return PasswordService.temporaryPassword();}
}