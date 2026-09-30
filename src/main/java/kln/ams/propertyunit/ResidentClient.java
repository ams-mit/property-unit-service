package kln.ams.propertyunit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
class ResidentClient {
    private final RestClient client; private final GatewayTokens tokens; private final ObjectMapper mapper;
    ResidentClient(@Value("${project.gateway.base-url}") String baseUrl,
                   @Value("${project.http.connect-timeout:2s}") Duration connectTimeout,
                   @Value("${project.http.read-timeout:5s}") Duration readTimeout,
                   GatewayTokens tokens,ObjectMapper mapper) {
        var factory=new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);factory.setReadTimeout(readTimeout);
        this.client=RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.tokens=tokens;this.mapper=mapper;
    }
    void validateOwner(UUID ownerId) {
        try {
            String body=client.get().uri("/api/v1/internal/residents/{residentId}/validate",ownerId)
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+tokens.serviceToken())
                .header("X-Request-ID",RequestIds.current()).retrieve().body(String.class);
            JsonNode root=mapper.readTree(body);
            JsonNode data=root==null?null:root.path("data");
            if(root==null || !root.path("success").asBoolean(false) || data==null || data.isMissingNode()
                || data.isNull() || data.isBoolean() && !data.booleanValue()
                || data.path("valid").isBoolean() && !data.path("valid").booleanValue()
                || data.path("exists").isBoolean() && !data.path("exists").booleanValue())
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"DEPENDENCY_UNAVAILABLE","Resident validation unavailable");
        } catch(RestClientResponseException e) {
            if(e.getStatusCode().value()==404) throw new ApiException(HttpStatus.NOT_FOUND,"OWNER_NOT_FOUND","Owner profile not found");
            throw unavailable();
        } catch(ApiException e) {throw e;}
        catch(Exception e) {throw unavailable();}
    }
    private ApiException unavailable() { return new ApiException(HttpStatus.SERVICE_UNAVAILABLE,"DEPENDENCY_UNAVAILABLE","Required service is temporarily unavailable",java.util.Map.of("service","resident-management-service")); }
}
