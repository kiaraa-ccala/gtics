package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.entity.Credencial;
import com.example.proyectosanmiguel.entity.Rol;
import com.example.proyectosanmiguel.entity.TokenRecuperacion;
import com.example.proyectosanmiguel.entity.Usuario;
import com.example.proyectosanmiguel.repository.*;
import com.example.proyectosanmiguel.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
public class SessionController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private SectorRepository sectorRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private CredencialRepository credencialRepository;
    @Autowired
    private TokenRecuperacionRepository tokenRecuperacionRepository;
    @Autowired
    private EmailService emailService;

    @GetMapping(value = {"/", "/inicio", ""})
    public String iniciarSesion(HttpServletRequest request, Model model) {
        Object error = request.getSession().getAttribute("error");
        System.out.println("Error : " + error);
        if (error != null) {
            System.out.println("Error desde sesión: " + error);
            model.addAttribute("error", error);
            request.getSession().removeAttribute("error"); // borra el error una vez mostrado
        }
        return "Acceso/login";
    }

    @GetMapping("/registrarse")
    public String formularioRegistro(@ModelAttribute("usuario") Usuario usuario, Model model) {

        model.addAttribute("listaSectores", sectorRepository.findAll());

        return "Acceso/registro";
    }

    @PostMapping("/crear")
    public String procesarRegistro(@ModelAttribute("usuario") Usuario usuario,
                                   @RequestParam("password2") String password2,
                                   Model model){

        // Valida que no existe usuario que haya registrado ese correo
        if (credencialRepository.existsByCorreo(usuario.getCredencial().getCorreo())) {
            model.addAttribute("error", "El correo ya está registrado");
            return "Acceso/registro";
        }

        // Valida que las contraseñas coincidan
        if (!usuario.getCredencial().getPassword().equals(password2)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "Acceso/registro";
        }

        //Se encripta la contraseña

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String passwordHashed = encoder.encode(usuario.getCredencial().getPassword());

        usuario.getCredencial().setPassword(passwordHashed);

        //Se asigna el rol de vecino

        Rol rol = rolRepository.findRolByIdRol(4); //El formulario solo permitirá crear usuarios con rol de vecino

        usuario.setRol(rol);

        //Se asigna al usuario como activo y se crea su credencial

        Credencial credencial = new Credencial();
        credencial.setCorreo(usuario.getCredencial().getCorreo());
        credencial.setPassword(usuario.getCredencial().getPassword());
        credencial.setUsuario(usuario);
        usuario.setCredencial(credencial);
        usuario.setActivo(1);

        usuarioRepository.save(usuario);
        return "redirect:/inicio";
    }

    @GetMapping("/recuperacion")
    public String recuperarContrasena() {
        return "Acceso/recuperar";
    }


    @Value("${app.url.base}")
    private String appUrlBase;

    @PostMapping("/recuperar")
    public String procesarRecuperacion(@RequestParam("correo") String correo,
                                       RedirectAttributes attr) {
        Credencial credencial = credencialRepository.findByCorreo(correo);

        if (credencial != null) {
            Usuario usuario = credencial.getUsuario();
            System.out.println("Usuario: " + usuario.getNombre() + " " + usuario.getApellido());

            // Verificar que sea usuario "Vecino"
            if (usuario.getRol() != null && usuario.getRol().getIdRol() == 4) {
                // Generar token
                String token = UUID.randomUUID().toString();
                TokenRecuperacion tokenRec = new TokenRecuperacion();
                tokenRec.setToken(token);
                tokenRec.setUsuario(usuario);
                tokenRec.setExpiracion(LocalDateTime.now().plusMinutes(30));
                tokenRec.setUsado(false);
                tokenRecuperacionRepository.save(tokenRec);

                // Enviar correo con EmailService
                Map<String, Object> datos = new HashMap<>();
                datos.put("nombreCompleto", usuario.getNombre() + " " + usuario.getApellido());
                datos.put("enlace", appUrlBase + "/resetpassword?token=" + token);
                System.out.println("Enlace: " + appUrlBase + "/resetpassword?token=" + token);


                try {
                    emailService.enviarEmail(
                            correo,
                            "Restablecer tu contraseña",
                            "email/passwordrecovery",
                            datos
                    );
                } catch (Exception e) {
                    e.printStackTrace();  // También puedes usar un logger
                }
            }
        }

        attr.addFlashAttribute("mensaje", "Si el usuario está registrado en nuestro sistema, se ha enviado un enlace para restablecer la contraseña a su correo.");
        return "redirect:/recuperacion";
    }

    @GetMapping("/resetpassword")
    public String mostrarFormularioReset(@RequestParam("token") String token, Model model) {
        Optional<TokenRecuperacion> tokenRecuperacionOpt = tokenRecuperacionRepository.findByToken(token);

        if (tokenRecuperacionOpt.isEmpty()) {
            model.addAttribute("titulo", "Token inválido");
            model.addAttribute("mensaje", "El enlace proporcionado no es válido o no existe.");
            return "ErrorPages/tokenerror";
        }

        TokenRecuperacion tokenRecuperacion = tokenRecuperacionOpt.get();

        if (tokenRecuperacion.isUsado() || tokenRecuperacion.getExpiracion().isBefore(LocalDateTime.now())) {
            model.addAttribute("titulo", "Token expirado o ya usado");
            model.addAttribute("mensaje", "Este enlace ya fue utilizado o ha expirado. Solicita uno nuevo.");
            return "ErrorPages/tokenerror";
        }

        model.addAttribute("token", token); // para el campo oculto en el form
        return "Acceso/resetpassword";
    }




}
