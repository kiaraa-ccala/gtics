package com.example.proyectosanmiguel.controller;
import com.example.proyectosanmiguel.entity.Credencial;
import com.example.proyectosanmiguel.entity.Rol;
import com.example.proyectosanmiguel.entity.Usuario;
import com.example.proyectosanmiguel.repository.CredencialRepository;
import com.example.proyectosanmiguel.repository.RolRepository;
import com.example.proyectosanmiguel.repository.SectorRepository;
import com.example.proyectosanmiguel.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

}
