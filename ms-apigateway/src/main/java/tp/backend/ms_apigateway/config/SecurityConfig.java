package tp.backend.ms_apigateway.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebFilter;

import reactor.core.publisher.Mono;

/**
 * Configuración de seguridad para el API Gateway.
 * Define reglas de autenticación/autorización basadas en JWT de Keycloak.
 * 
 * Roles disponibles:
 * - admin: Administrador del sistema (gestión de tarifas, depósitos, camiones)
 * - operador: Operador logístico (gestión de solicitudes, rutas, asignación)
 * - cliente: Cliente final (solo ve sus propias solicitudes)
 * - transportista: Chofer (registra inicio/fin de tramos)
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())  // Deshabilitar CSRF (API REST con JWT)
            .authorizeExchange(exchanges -> exchanges
                
                // ============================================
                // SWAGGER/OPENAPI - RUTAS PÚBLICAS
                // ============================================
                // Swagger/OpenAPI del propio gateway
                .pathMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs.yaml",
                    "/v3/api-docs/**",
                    "/webjars/**"
                ).permitAll()
                
                // Swagger/OpenAPI PROXY de cada microservicio
                .pathMatchers(
                    // ms-solicitud
                    "/ms-solicitud/swagger-ui.html",
                    "/ms-solicitud/swagger-ui/**",
                    "/ms-solicitud/v3/api-docs",
                    "/ms-solicitud/v3/api-docs.yaml",
                    "/ms-solicitud/v3/api-docs/**",
                    
                    // ms-contenedor
                    "/ms-contenedor/swagger-ui.html",
                    "/ms-contenedor/swagger-ui/**",
                    "/ms-contenedor/v3/api-docs",
                    "/ms-contenedor/v3/api-docs.yaml",
                    "/ms-contenedor/v3/api-docs/**",
                    
                    // ms-tarifa
                    "/ms-tarifa/swagger-ui.html",
                    "/ms-tarifa/swagger-ui/**",
                    "/ms-tarifa/v3/api-docs",
                    "/ms-tarifa/v3/api-docs.yaml",
                    "/ms-tarifa/v3/api-docs/**"
                ).permitAll()
                
                // Preflight CORS
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // ============================================
                // RUTAS PÚBLICAS (sin autenticación requerida)
                // ============================================
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // (NO PERMIT) Asegurar que POST /api/camiones requiera roles adecuados
                // .pathMatchers("POST", "/api/camiones").hasRole("admin")
                
                // ============================================
                // RUTAS DE ADMINISTRADOR (rol: ADMIN)
                // Se permite también el acceso a 'OPERADOR' por requerimiento: administrador == operador
                // ============================================
                // Gestión de tarifas
                .pathMatchers("/api/tarifas/**").hasRole("ADMIN")
                
                // Gestión de depósitos
                .pathMatchers("/api/depositos/**").hasRole("ADMIN")
                
                // Gestión de contenedores
                // - GET listado/detalle: todos los roles autenticados
                // - POST/PUT/DELETE: solo ADMIN
                .pathMatchers(HttpMethod.GET, "/api/contenedores/**").authenticated()
                .pathMatchers(HttpMethod.POST, "/api/contenedores/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/contenedores/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/api/contenedores/**").hasRole("ADMIN")
                
                // ============================================
                // RUTAS COMPARTIDAS ADMIN + OPERADOR
                // ============================================
                // Gestión de camiones
                // - GET listado/detalle: OPERADOR + ADMIN
                // - POST/PUT/DELETE: solo ADMIN
                .pathMatchers(HttpMethod.GET, "/api/camiones/**").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/camiones/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/camiones/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/api/camiones/**").hasRole("ADMIN")
                
            
                .pathMatchers(HttpMethod.POST, "/api/solicitudes").hasRole("CLIENTE")
                .pathMatchers(HttpMethod.PUT, "/api/solicitudes/*").hasRole("CLIENTE")
                .pathMatchers(HttpMethod.DELETE, "/api/solicitudes/*").hasRole("CLIENTE")
                .pathMatchers(HttpMethod.GET, "/api/solicitudes/*").hasAnyRole("OPERADOR", "CLIENTE")
                .pathMatchers(HttpMethod.GET, "/api/solicitudes/by-cliente/*").hasRole("CLIENTE")
                .pathMatchers(HttpMethod.GET, "/api/solicitudes/*/estado").hasAnyRole("CLIENTE", "OPERADOR")
                .pathMatchers(HttpMethod.GET, "/api/solicitudes").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers("/api/solicitudes/*/rutas-tentativas").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers("/api/solicitudes/*/asignar-ruta").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/solicitudes/*/estado").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/solicitudes/*/tramos").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/solicitudes/*/total").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers("/api/solicitudes/*/ruta/**").hasRole("OPERADOR")
              
                .pathMatchers(HttpMethod.GET, "/api/tramos/*/estado").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.PATCH, "/api/tramos/*/asignar-camion").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/camiones").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/camiones/*").hasAnyRole("OPERADOR", "ADMIN")
                .pathMatchers(HttpMethod.PATCH, "/api/tramos/*/inicio").hasRole("TRANSPORTISTA")
                .pathMatchers(HttpMethod.PATCH, "/api/tramos/*/fin").hasRole("TRANSPORTISTA")
                
                // Ver tramos asignados a su camión (TRANSPORTISTA)
                // Permitir también IDs con slashes usando /**
                .pathMatchers(HttpMethod.GET, "/api/tramos/by-camion/**").hasRole("TRANSPORTISTA")
                
                // ============================================
                // TODAS LAS DEMÁS RUTAS (solo ADMIN)
                // ============================================
                // Operaciones de modificación general (solo ADMIN)
                .pathMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                // Requieren autenticación (cualquier rol) por defecto
                .anyExchange().authenticated()
            )
            // Validar JWT usando configuración de application.yml y converter de roles
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
            );
        
        return http.build();
    }

    /**
     * Convierte los roles de Keycloak (realm_access.roles) al formato esperado por Spring Security (ROLE_*).
     * 
     * Keycloak envía los roles en el claim "realm_access" como:
     * {
     *   "realm_access": {
     *     "roles": ["admin", "cliente", "operador"]
     *   }
     * }
     * 
     * Spring Security espera authorities con formato: ROLE_ADMIN, ROLE_CLIENTE, etc.
     * Este converter hace la traducción automáticamente.
     */
    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        Logger log = LoggerFactory.getLogger(SecurityConfig.class);
        return new ReactiveJwtAuthenticationConverterAdapter(jwt -> {
            // Extraer roles tanto de realm_access (realm roles) como de resource_access (client roles)
            Collection<GrantedAuthority> authorities;

            // Recolectar roles en una lista mutable
            List<String> collectedRoles = new java.util.ArrayList<>();

            // realm_access.roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                if (realmRoles != null) collectedRoles.addAll(realmRoles);
            }

            log.info("🔍 [JWT] Extracted realm_access roles: {}", collectedRoles);

            // resource_access.{client}.roles
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                for (Object clientEntryObj : resourceAccess.values()) {
                    if (clientEntryObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> clientEntry = (Map<String, Object>) clientEntryObj;
                        if (clientEntry.containsKey("roles")) {
                            @SuppressWarnings("unchecked")
                            List<String> clientRoles = (List<String>) clientEntry.get("roles");
                            if (clientRoles != null) collectedRoles.addAll(clientRoles);
                        }
                    }
                }
            }

            if (!collectedRoles.isEmpty()) {
                // Normalizar y convertir a GrantedAuthority con prefijo ROLE_
                authorities = collectedRoles.stream()
                        .distinct()
                        .map(role -> "ROLE_" + role.toUpperCase())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                
                log.info("✅ [JWT] Converted to authorities: {}", authorities);
            } else {
                authorities = List.of();
                log.warn("⚠️ [JWT] No roles found in token!");
            }

            return new JwtAuthenticationToken(jwt, authorities);
        });
    }

    /**
     * Debug filter: logs authentication and JWT claims for solicitudes endpoints.
     * This helps to see which roles/claims the gateway actually sees when validating
     * the incoming token (useful to debug 403 Forbidden).
     */
    @Bean
    public WebFilter debugAuthWebFilter() {
        Logger log = LoggerFactory.getLogger(SecurityConfig.class);
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            if (!path.startsWith("/api/solicitudes")) {
                return chain.filter(exchange);
            }

            return ReactiveSecurityContextHolder.getContext()
                .doOnNext(ctx -> {
                    if (ctx != null && ctx.getAuthentication() != null) {
                        var auth = ctx.getAuthentication();
                        log.info("[DEBUG] Authenticated principal='{}' authorities={} for path={}", auth.getName(), auth.getAuthorities(), path);
                        if (auth instanceof JwtAuthenticationToken) {
                            Jwt jwt = ((JwtAuthenticationToken) auth).getToken();
                            log.info("[DEBUG] JWT issuer='{}' aud='{}' exp='{}'", jwt.getIssuer(), jwt.getAudience(), jwt.getExpiresAt());
                            try {
                                log.info("[DEBUG] JWT realm_access={}", jwt.getClaimAsMap("realm_access"));
                            } catch (Exception e) {
                                // ignore
                            }
                            try {
                                log.info("[DEBUG] JWT resource_access={}", jwt.getClaimAsMap("resource_access"));
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                    } else {
                        log.info("[DEBUG] No authentication/security context for path={}", path);
                    }
                })
                .then(chain.filter(exchange));
        };
    }
}