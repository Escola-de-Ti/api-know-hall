package br.com.escoladeti.api_know_hall.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailValidator {

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
  );

  public boolean isValid(String email) {
    if (email == null || email.isEmpty()) {
      return false;
    }

    email = email.trim();

    if (email.length() < 5 || email.length() > 254) {
      return false;
    }

    return EMAIL_PATTERN.matcher(email).matches();
  }

  public String normalizar(String email) {
    if (email == null) return null;
    return email.trim().toLowerCase();
  }
}
