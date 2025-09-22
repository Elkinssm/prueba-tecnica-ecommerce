package org.example.pruebatecnicaecommerce.infrastructure.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.UserResponse;
import org.example.pruebatecnicaecommerce.application.dto.UserResponseMapper;
import org.example.pruebatecnicaecommerce.domain.model.user.User;
import org.example.pruebatecnicaecommerce.domain.model.user.UserRepository;
import org.example.pruebatecnicaecommerce.shared.error.ErrorResponse;
import org.example.pruebatecnicaecommerce.shared.error.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints para consultar informacion del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(
            summary = "Obtener informacion del usuario actual",
            description = "Devuelve los datos del usuario autenticado basados en el token JWT recibido"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> getCurrentUser(
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        UserResponse response = UserResponseMapper.fromDomain(user);
        return ResponseEntity.ok(response);
    }
}
