package com.lucia.memoria.exception;

public class ServerApiException extends RuntimeException {
  public ServerApiException(String message) { super(message); }
  public ServerApiException(String message, Throwable cause) { super(message, cause); }
}
