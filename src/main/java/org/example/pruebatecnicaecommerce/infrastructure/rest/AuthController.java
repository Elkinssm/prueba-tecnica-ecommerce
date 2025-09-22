package org.example.pruebatecnicaecommerce.infrastructure.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.AuthResponse;
import org.example.pruebatecnicaecommerce.application.dto.LoginRequest;
import org.example.pruebatecnicaecommerce.application.dto.RegisterRequest;
import org.example.pruebatecnicaecommerce.application.service.LoginUserService;
import org.example.pruebatecnicaecommerce.application.service.RegisterUserService;
import org.example.pruebatecnicaecommerce.shared.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints publicos para registro y autenticacion de usuarios")
public class AuthController {

        private final RegisterUserService registerUserService;
        private final LoginUserService loginUserService;

        @PostMapping("/register")
        @Operation(summary = "Registrar un nuevo usuario", description = "Crea un usuario en la plataforma y devuelve el token JWT asociado")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Usuario registrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Usuario ya existente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
                AuthResponse response = registerUserService.execute(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PostMapping("/login")
        @Operation(summary = "Autenticar un usuario", description = "Valida las credenciales y retorna un token JWT valido")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Autenticacion exitosa", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Credenciales invalidas", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
                AuthResponse response = loginUserService.execute(request);
                return ResponseEntity.ok(response);
        }
}
