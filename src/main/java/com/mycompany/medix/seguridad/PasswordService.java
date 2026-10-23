package com.mycompany.medix.seguridad;
import java.security.SecureRandom;
import java.util.Base64;
import org.mindrot.jbcrypt.BCrypt;
public final class PasswordService {
  private static final SecureRandom RANDOM = new SecureRandom();
  private PasswordService() {}
  public static String hash(char[] password) {
    if (password == null || password.length < 12) throw new IllegalArgumentException("La contraseña debe tener al menos 12 caracteres.");
    return BCrypt.hashpw(new String(password), BCrypt.gensalt(12));
  }
  public static boolean verify(char[] candidate, String hash) {
    if (candidate == null || hash == null || !hash.startsWith("$2")) return false;
    try { return BCrypt.checkpw(new String(candidate), hash); } catch (IllegalArgumentException ex) { return false; }
  }
  public static String temporaryPassword() {
    byte[] bytes = new byte[18]; RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}