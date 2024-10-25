package fr.insee.queen.application.configuration.swagger;

import fr.insee.queen.application.configuration.properties.OidcProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@ConditionalOnProperty(value="feature.swagger.enabled", havingValue = "true")
public class SpringDocConfiguration {

    @Bean
    @ConditionalOnProperty(name = "feature.oidc.enabled", havingValue = "false")
    protected OpenAPI noAuthOpenAPI(BuildProperties buildProperties) {
        return generateOpenAPI(buildProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "feature.oidc.enabled", havingValue = "true")
    protected OpenAPI oidcOpenAPI(OidcProperties oidcProperties, BuildProperties buildProperties) {
        String authUrl = oidcProperties.authServerUrl() + "/realms/" + oidcProperties.realm() + "/protocol/openid-connect";
        String securitySchemeName = "oauth2";

        return generateOpenAPI(buildProperties)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName, Arrays.asList("read", "write")))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.OAUTH2)
                                                .flows(getFlows(authUrl))
                                )
                );

    }

    private OpenAPI generateOpenAPI(BuildProperties buildProperties) {
        return new OpenAPI().info(
                new Info()
                        .title(buildProperties.getName())
                        .description(buildProperties.get("description"))
                        .version(buildProperties.getVersion())
        );
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            var schemas = openApi.getComponents().getSchemas();
            ArraySchema arrayNodeSchema = new ArraySchema();
            arrayNodeSchema.items(new ObjectSchema());
            arrayNodeSchema.name("ArrayNode");

            ObjectSchema objectNodeSchema = new ObjectSchema();
            objectNodeSchema.name("ObjectNode");

            schemas.put(arrayNodeSchema.getName() , arrayNodeSchema);
            schemas.put(objectNodeSchema.getName() , objectNodeSchema);
        };
    }

    private OAuthFlows getFlows(String authUrl) {
        OAuthFlows flows = new OAuthFlows();
        OAuthFlow flow = new OAuthFlow();
        Scopes scopes = new Scopes();
        flow.setAuthorizationUrl(authUrl + "/auth");
        flow.setTokenUrl(authUrl + "/token");
        flow.setRefreshUrl(authUrl + "/token");
        flow.setScopes(scopes);
        return flows.authorizationCode(flow);
    }
}
