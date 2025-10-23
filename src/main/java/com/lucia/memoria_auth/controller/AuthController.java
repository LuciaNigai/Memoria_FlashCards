package com.lucia.memoria_auth.controller;

import com.lucia.memoria_auth.dto.AuthenticationRequestDto;
import com.lucia.memoria_auth.dto.AuthenticationResponseDto;
import com.lucia.memoria_auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationService authenticationService;

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponseDto> authenticate(
      @RequestBody final AuthenticationRequestDto authenticationRequestDto
  ) {
    return ResponseEntity.ok(
        authenticationService.authenticate(authenticationRequestDto));
  }
}
