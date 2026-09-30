package kln.ams.propertyunit;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
class OpenApiConfiguration {
    @Bean ObjectMapper jacksonTwoMapper() { return new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); }
    @Bean OpenAPI openApi() {
        return new OpenAPI().info(new Info().title("Project A Property Unit API").version("v1")
            .description("12 canonical property endpoints. JSON success/error envelopes include timestamp and requestId. Internal APIs require a Gateway signed service JWT."))
            .components(new Components().addSecuritySchemes("gatewayBearer",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
