package br.com.easynr6.gestaoepi.identity.application.port;

import java.time.LocalDateTime;

public interface Clock {

  LocalDateTime now();
}
