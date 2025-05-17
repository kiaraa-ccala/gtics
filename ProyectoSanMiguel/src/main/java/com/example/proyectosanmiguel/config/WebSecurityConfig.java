package com.example.proyectosanmiguel.config;
import com.example.proyectosanmiguel.repository.CredencialRepository;
import com.example.proyectosanmiguel.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
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

        http.formLogin(form -> form
                        .loginPage("/inicio")
                        .loginProcessingUrl("/procesarInicio")
                        .successHandler((request, response, authentication) -> {
                            try {
                                HttpSession session = request.getSession();
                                Object usuario = credencialRepository.findByCorreo(authentication.getName());
                                if (usuario == null) {
                                    System.out.println("Error: No se encontró usuario para el correo " + authentication.getName());
                                    response.sendRedirect("/inicio?error='Usuario no encontrado' ");
                                }
                                session.setAttribute("usuario", usuario);

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
                );

        http.authorizeHttpRequests(authz -> authz
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
