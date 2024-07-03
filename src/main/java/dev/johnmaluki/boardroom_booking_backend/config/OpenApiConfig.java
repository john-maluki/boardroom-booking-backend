package dev.johnmaluki.boardroom_booking_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "KEMRI BOARDROOM BACKEND APP",
                contact = @Contact(
                        name = "KEMRI",
                        email = "support@kemri.go.ke",
                        url = "https://kemri.go.ke"
                ),
                description = "This app is designed to streamline the booking process for KEMRI meetings.",
                summary = "OpenAi documentation for KEMRI boardroom-backend",
                termsOfService = "https://kemri-wellcome.org/terms-and-conditions/",
                license = @License(
                        name = "Apache License 2.0",
                        url = "https://github.com/john-maluki/boardroom-booking-backend/blob/main/LICENSE"
                )
        ),
        servers = {
                @Server(
                        description = "Local DEV",
                        url = "http://localhost:8080"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Authentication through JWT",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

}
