package com.mycompany.medix.security;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class PasswordServiceTest {
  @Test void hashVerifiesOnlyCorrectPassword() {
    char[] password="Correct horse battery staple 2026".toCharArray();
    String hash=PasswordService.hash(password);
    assertNotEquals(new String(password),hash);
    assertTrue(PasswordService.verify(password,hash));
    assertFalse(PasswordService.verify("wrong password".toCharArray(),hash));
  }
  @Test void temporaryPasswordsAreRandomAndLongEnough() {
    String first=PasswordService.temporaryPassword(),second=PasswordService.temporaryPassword();
    assertTrue(first.length()>=20);
    assertNotEquals(first,second);
  }
}
