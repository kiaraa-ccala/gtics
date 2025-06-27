package com.example.proyectosanmiguel.config;
import com.example.proyectosanmiguel.entity.Credencial;
import com.example.proyectosanmiguel.repository.CredencialRepository;
import com.example.proyectosanmiguel.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

import javax.sql.DataSource;

@Configuration
public class WebSecurityConfig {

    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    CredencialRepository credencialRepository;
    @Autowired
    DataSource dataSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Agregar soporte para Basic Auth
        http.httpBasic(Customizer.withDefaults());

        http.formLogin(form -> form
                        .loginPage("/inicio")
                        .loginProcessingUrl("/procesarInicio")
                        .successHandler((request, response, authentication) -> {
                            try {
                                HttpSession session = request.getSession();
                                Credencial credencial = credencialRepository.findByCorreo(authentication.getName());
                                if (credencial == null) {
                                    System.out.println("Error: No se encontró usuario para el correo " + authentication.getName());
                                    request.getSession().setAttribute("error", "Usuario no encontrado");
                                    response.sendRedirect("/inicio");

                                    return;
                                }

                                session.setAttribute("credencial", credencial);

                                DefaultSavedRequest defaultSavedRequest = (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
                                String rol = authentication.getAuthorities().iterator().next().getAuthority();
                                System.out.println("Usuario: " + authentication.getName() + ", Rol: " + rol);

                                if (defaultSavedRequest != null) {
                                    String targetURL = defaultSavedRequest.getRequestURL();
                                    System.out.println("Intentando redirigir a DefaultSavedRequest: " + targetURL);
                                    boolean hasAccess = false;

                                    if (rol.equals("Superadministrador") && targetURL.contains("/superadmin")) {
                                        hasAccess = true;
                                    } else if (rol.equals("Administrador") && targetURL.contains("/admin")) {
                                        hasAccess = true;
                                    } else if (rol.equals("Coordinador") && targetURL.contains("/coord")) {
                                        hasAccess = true;
                                    } else if (rol.equals("Vecino") && targetURL.contains("/vecino")) {
                                        hasAccess = true;
                                    }

                                    if (hasAccess) {
                                        System.out.println("Redirigiendo a: " + targetURL);
                                        new DefaultRedirectStrategy().sendRedirect(request, response, targetURL);
                                        return;
                                    } else {
                                        System.out.println("Acceso denegado a: " + targetURL + " para el rol " + rol);
                                    }
                                }

                                String redirectUrl = switch (rol) {
                                    case "Superadministrador" -> "/superadmin";
                                    case "Administrador" -> "/admin/agenda";
                                    case "Coordinador" -> "/coord/inicio";
                                    case "Vecino" -> "/vecino/misReservas";
                                    default -> "/inicio";
                                };
                                System.out.println("Redirigiendo a página por defecto: " + redirectUrl);
                                response.sendRedirect(redirectUrl);

                            } catch (Exception e) {
                                System.err.println("Error en successHandler: " + e.getMessage());
                                e.printStackTrace();
                                response.sendRedirect("/inicio?error='Inicio de sesion fallido, intente más tarde' ");
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            System.out.println("Fallo de login: " + exception.getMessage());

                            request.getSession().setAttribute("error", "Credenciales incorrectas o usuario no válido");
                            response.sendRedirect("/inicio");
                        })                );        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/ai/**", "/api/**", "/chat", "/ai/chat", "/ai/chat/stream", "/superadmin/reportes/generarpdf", "/vecino/public/**", "/vecino/webhook/**")
        );        http.authorizeHttpRequests(authz -> authz
                .requestMatchers("/ai/**", "/ai/chat", "/ai/chat/stream", "/api/**", "/chat", "/inicio", "/", "/css/**", "/js/**", "/images/**", "/vecino/public/**", "/vecino/webhook/**", "/vecino/debug/**", "/vecino/forzarActualizacionEstado").permitAll()
                .requestMatchers("/superadmin", "/superadmin/**").hasAnyAuthority("Superadministrador")
                .requestMatchers("/admin", "/admin/**").hasAnyAuthority("Administrador")
                .requestMatchers("/coord", "/coord/**").hasAnyAuthority("Coordinador")
                .requestMatchers("/vecino", "/vecino/**").hasAnyAuthority("Vecino")
                .anyRequest().permitAll());

        http.logout(logout -> logout
                .logoutUrl("/salir")
                .logoutSuccessUrl("/inicio")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
        );

        http.exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                    if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                        System.out.println("Acceso denegado: usuario no autenticado");
                        response.sendRedirect("/inicio");
                    } else {
                        // Obtener el rol principal
                        String rol = auth.getAuthorities().iterator().next().getAuthority();
                        System.out.println("Tu rol es: " + rol);

                        // Redirigir según el rol
                        switch (rol) {
                            case "Superadministrador" -> {
                                System.out.println("Redirigiendo a /superadmin");
                                response.sendRedirect("/superadmin");
                            }
                            case "Administrador" -> {
                                System.out.println("Redirigiendo a /admin/agenda");
                                response.sendRedirect("/admin/agenda");
                            }
                            case "Coordinador" -> {
                                System.out.println("Redirigiendo a /coord/inicio");
                                response.sendRedirect("/coord/inicio");
                            }
                            case "Vecino" -> {
                                System.out.println("Redirigiendo a /vecino/misReservas");
                                response.sendRedirect("/vecino/misReservas");
                            }
                            default -> {
                                System.out.println("Redirigiendo a /inicio por rol desconocido");
                                response.sendRedirect("/inicio");
                            }
                        }
                    }
                })
        );

        return http.build();
    }

    @Bean
    public UserDetailsManager users(DataSource dataSource) {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
        //para loguearse sqlAuth -> username | password | enable
        String sqlAuth = "SELECT c.correo,c.password, u.activo FROM credencial c join usuario u on c.idUsuario=u.idUsuario where c.correo = ?";

        //para autenticación -> username, nombre del rol
        String sqlAuto = "SELECT c.correo, r.nombre FROM credencial c join usuario u on c.idUsuario=u.idUsuario join rol r on u.idRol=r.idRol where c.correo = ?";

        users.setUsersByUsernameQuery(sqlAuth);
        users.setAuthoritiesByUsernameQuery(sqlAuto);

        return users;
    }

}

