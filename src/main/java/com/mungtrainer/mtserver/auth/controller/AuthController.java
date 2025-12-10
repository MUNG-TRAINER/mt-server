package com.mungtrainer.mtserver.auth.controller;

import com.mungtrainer.mtserver.auth.dto.request.AuthTrainerJoinRequest;
import com.mungtrainer.mtserver.auth.dto.request.AuthUserJoinRequest;
import com.mungtrainer.mtserver.auth.dto.response.AuthJoinResponse;
import com.mungtrainer.mtserver.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/join/trainer")
  public ResponseEntity<AuthJoinResponse> trainerJoin(@Valid @RequestBody AuthTrainerJoinRequest authTrainerJoinRequest) {
    AuthJoinResponse response = authService.trainerJoin(authTrainerJoinRequest);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/join/user")
  public ResponseEntity<AuthJoinResponse> userJoin(@Valid @RequestBody AuthUserJoinRequest authUserJoinRequest) {
    AuthJoinResponse response = authService.userJoin(authUserJoinRequest);
    return ResponseEntity.ok(response);
  }
}
