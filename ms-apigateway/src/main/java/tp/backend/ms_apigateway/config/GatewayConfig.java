package tp.backend.ms_apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;

@Configuration
public class GatewayConfig {

        @Value("${apigw.url-solicitud:http://localhost:8084}")
        private String solicitudUri;

        @Value("${apigw.url-tarifa:http://localhost:8085}")
        private String tarifaUri;

        @Value("${apigw.url-contenedor:http://localhost:8083}")
        private String contenedorUri;

    @Bean
    //reglas para enrutamiento
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route /api/solicitudes/** -> {solicitudUri}/api/solicitudes/**
                .route("solicitud_route", r -> r.path("/api/solicitudes/**")
                        .uri(solicitudUri))

                // Route /api/contenedores/** -> {contenedorUri}/api/contenedores/**
                .route("contenedor_route", r -> r.path("/api/contenedores/**")
                        .uri(contenedorUri))

                // Route /api/depositos/** -> {contenedorUri}/api/depositos/**
                // Depositos están en el mismo microservicio que contenedores
                .route("deposito_route", r -> r.path("/api/depositos/**")
                        .uri(contenedorUri))

                // Route /api/tarifas/** -> {tarifaUri}/api/tarifas/**
                .route("tarifa_route", r -> r.path("/api/tarifas/**")
                        .uri(tarifaUri))

                // ms-solicitud routes (other controllers)
                .route("ruta_route", r -> r.path("/api/rutas/**")
                        .uri(solicitudUri))
                .route("tramo_route", r -> r.path("/api/tramos/**")
                        .uri(solicitudUri))
                .route("punto_route", r -> r.path("/api/puntos/**")
                        .uri(solicitudUri))
                .route("cliente_route", r -> r.path("/api/clientes/**")
                        .uri(solicitudUri))
                .route("camion_route", r -> r.path("/api/camiones/**")
                        .uri(solicitudUri))
                .route("tipo_route", r -> r.path("/api/tipos/**")
                        .uri(solicitudUri))

                // ============================================
                // SWAGGER/OPENAPI PROXY ROUTES
                // ============================================
                // ms-solicitud docs
                .route("ms-solicitud-docs", r -> r
                        .path(
                            "/ms-solicitud/swagger-ui.html",
                            "/ms-solicitud/swagger-ui/**",
                            "/ms-solicitud/v3/api-docs",
                            "/ms-solicitud/v3/api-docs.yaml",
                            "/ms-solicitud/v3/api-docs/**"
                        )
                        .filters(f -> f.stripPrefix(1))
                        .uri(solicitudUri)
                )

                // ms-contenedor docs
                .route("ms-contenedor-docs", r -> r
                        .path(
                            "/ms-contenedor/swagger-ui.html",
                            "/ms-contenedor/swagger-ui/**",
                            "/ms-contenedor/v3/api-docs",
                            "/ms-contenedor/v3/api-docs.yaml",
                            "/ms-contenedor/v3/api-docs/**"
                        )
                        .filters(f -> f.stripPrefix(1))
                        .uri(contenedorUri)
                )

                // ms-tarifa docs
                .route("ms-tarifa-docs", r -> r
                        .path(
                            "/ms-tarifa/swagger-ui.html",
                            "/ms-tarifa/swagger-ui/**",
                            "/ms-tarifa/v3/api-docs",
                            "/ms-tarifa/v3/api-docs.yaml",
                            "/ms-tarifa/v3/api-docs/**"
                        )
                        .filters(f -> f.stripPrefix(1))
                        .uri(tarifaUri)
                )

                .build();
    }

    /**
     * Filtro global que propaga información de autenticación a los microservicios downstream.
     * Agrega headers HTTP con información del usuario autenticado (username, roles).
     * Los microservicios pueden leer estos headers para implementar autorización a nivel de dominio.
     */
    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            return ReactiveSecurityContextHolder.getContext()
                    .map(securityContext -> {
                        if (securityContext.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                            // Extraer información del JWT
                            String username = jwtAuth.getName();
                            String roles = jwtAuth.getAuthorities().stream()
                                    .map(Object::toString)
                                    .reduce((a, b) -> a + "," + b)
                                    .orElse("");

                            // Agregar headers para los microservicios
                            ServerWebExchange mutatedExchange = exchange.mutate()
                                    .request(r -> r
                                            .header("X-Auth-Username", username)
                                            .header("X-Auth-Roles", roles)
                                    )
                                    .build();

                            return mutatedExchange;
                        }
                        return exchange;
                    })
                    .defaultIfEmpty(exchange)
                    .flatMap(chain::filter);
        };
    }
}
