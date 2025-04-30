package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private InstanciaServicioRepository instanciaServicioRepository;

    @Autowired
    private ComplejoRepository complejoRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    // ========== Incidencias ==========
    @GetMapping("/reportes")
    public String listarReportes(Model model) {
        model.addAttribute("listaReportes", reporteRepository.findAll());
        return "Admin/admin_reporte_incidencia";
    }

    @GetMapping("/reportes/detalle/{id}")
    public String detalleReporte(@PathVariable Integer id, Model model) {
        model.addAttribute("reporte", reporteRepository.findById(id).orElseThrow());
        List<Comentario> comentarios = comentarioRepository.findByReporteIdReporteOrderByFechaHoraAsc(id);
        model.addAttribute("comentarios", comentarios);
        return "Admin/admin_incidente";
    }

    // ========== Monitoreo ==========
    @GetMapping("/servicios/monitoreo")
    public String monitoreoServicios(Model model) {
        List<InstanciaServicio> lista = instanciaServicioRepository.findAll();
        model.addAttribute("instancias", lista);
        return "Admin/admin_mantenimiento_modal";
    }


    // ========== Guardar Nuevo Complejo ==========
    @PostMapping("/servicios/guardarComplejo")
    @ResponseBody
    public String guardarNuevoComplejo(
            @RequestParam("nombreComplejo") String nombreComplejo,
            @RequestParam("direccionComplejo") String direccionComplejo,
            @RequestParam("numSoporte") String numSoporte,
            @RequestParam("serviciosSeleccionados") String serviciosSeleccionadosJson,
            @RequestParam(value = "cantidadLosa", required = false) String cantidadLosaStr,
            @RequestParam(value = "cantidadGrass", required = false) String cantidadGrassStr,
            @RequestParam("latitud") Double latitud,
            @RequestParam("longitud") Double longitud,
            @RequestParam(value = "cantidadCarriles", required = false) String cantidadCarrilesStr
    ) {
        ComplejoDeportivo nuevoComplejo = new ComplejoDeportivo();
        nuevoComplejo.setNombre(nombreComplejo);
        nuevoComplejo.setDireccion(direccionComplejo);
        nuevoComplejo.setNumeroSoporte(numSoporte);
        nuevoComplejo.setLatitud(latitud);
        nuevoComplejo.setLongitud(longitud);

        Sector sector = sectorRepository.findById(1).orElseThrow(); // Busca un sector por defecto
        nuevoComplejo.setSector(sector);


        complejoRepository.save(nuevoComplejo);

        List<String> servicios = new Gson().fromJson(serviciosSeleccionadosJson, new TypeToken<List<String>>() {}.getType());

        Integer cantidadLosa = (cantidadLosaStr != null && !cantidadLosaStr.isEmpty()) ? Integer.parseInt(cantidadLosaStr) : null;
        Integer cantidadGrass = (cantidadGrassStr != null && !cantidadGrassStr.isEmpty()) ? Integer.parseInt(cantidadGrassStr) : null;
        Integer cantidadCarriles = (cantidadCarrilesStr != null && !cantidadCarrilesStr.isEmpty()) ? Integer.parseInt(cantidadCarrilesStr) : null;

        for (String nombreServicio : servicios) {
            Servicio servicioEncontrado = servicioRepository.findByNombre(nombreServicio);
            if (servicioEncontrado != null) {
                InstanciaServicio instancia = new InstanciaServicio();
                instancia.setComplejoDeportivo(nuevoComplejo);
                instancia.setServicio(servicioEncontrado);
                instancia.setNombre(nombreServicio);

                if ("Cancha de losa".equals(nombreServicio) && cantidadLosa != null) {
                    instancia.setCapacidadMaxima(String.valueOf(cantidadLosa));
                } else if ("Cancha de grass".equals(nombreServicio) && cantidadGrass != null) {
                    instancia.setCapacidadMaxima(String.valueOf(cantidadGrass));
                } else if ("Piscina".equals(nombreServicio) && cantidadCarriles != null) {
                    instancia.setCapacidadMaxima(String.valueOf(cantidadCarriles));
                } else {
                    instancia.setCapacidadMaxima("1");
                }


                instancia.setModoAcceso("Normal");
                instanciaServicioRepository.save(instancia);
            }
        }
        return "ok";
    }
}