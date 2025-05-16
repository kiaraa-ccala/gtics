package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.dto.ComplejoDetalleDTO;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import com.example.proyectosanmiguel.repository.ReservaRepository;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/complejos")
public class ComplejoApiController {

    @Autowired
    private ComplejoRepository complejoDeportivoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private InstanciaServicioRepository instanciaServicioRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private FotoRepository fotoRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ComplejoDetalleDTO> obtenerComplejoPorId(@PathVariable("id") Integer id) {
        Optional<ComplejoDeportivo> complejoOpt = complejoDeportivoRepository.findById(id);

        if (complejoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ComplejoDeportivo complejo = complejoOpt.get();

        ComplejoDetalleDTO dto = new ComplejoDetalleDTO();
        dto.setId(complejo.getIdComplejoDeportivo());
        dto.setNombre(complejo.getNombre());
        dto.setDireccion(complejo.getDireccion());
        dto.setLatitud(new BigDecimal(complejo.getLatitud()));
        dto.setLongitud(new BigDecimal(complejo.getLongitud()));
        dto.setSector(complejo.getSector().getNombre());

        // Servicios
        List<InstanciaServicio> instancias = instanciaServicioRepository
                .findInstanciaServicioByComplejoDeportivo(id);

        List<ComplejoDetalleDTO.ServicioDTO> serviciosDTO = instancias.stream().map(inst -> {
            ComplejoDetalleDTO.ServicioDTO s = new ComplejoDetalleDTO.ServicioDTO();
            s.setNombre(inst.getServicio().getNombre());
            s.setNombreInstancia(inst.getNombre());
            s.setCapacidadMaxima(inst.getCapacidadMaxima());
            s.setModoAcceso(inst.getModoAcceso());
            s.setIdInstancia(inst.getIdInstanciaServicio());
            return s;
        }).toList();

        dto.setServicios(serviciosDTO);

        // Tarifas
        List<Tarifa> tarifasFiltradas = tarifaRepository.findAll().stream()
                .filter(t -> instancias.stream()
                        .anyMatch(inst -> inst.getServicio().getIdServicio().equals(t.getServicio().getIdServicio()))
                ).toList();

        List<ComplejoDetalleDTO.TarifaDTO> tarifasDTO = tarifasFiltradas.stream().map(t -> {
            ComplejoDetalleDTO.TarifaDTO tarifa = new ComplejoDetalleDTO.TarifaDTO();
            tarifa.setNombreServicio(t.getServicio().getNombre());
            tarifa.setDiaSemana(t.getDiaSemana());
            tarifa.setHoraInicio(t.getHoraInicio());
            tarifa.setHoraFin(t.getHoraFin());
            tarifa.setMonto(BigDecimal.valueOf(t.getMonto()));
            return tarifa;
        }).toList();
        dto.setTarifas(tarifasDTO);

        // Horarios globales (apertura/cierre semana y finde)
        LocalTime aperturaSemana = null;
        LocalTime cierreSemana = null;
        LocalTime aperturaFin = null;
        LocalTime cierreFin = null;

        for (Tarifa t : tarifasFiltradas) {
            String dia = t.getDiaSemana().toLowerCase();
            if (dia.contains("sábado") || dia.contains("domingo")) {
                if (aperturaFin == null || t.getHoraInicio().isBefore(aperturaFin)) {
                    aperturaFin = t.getHoraInicio();
                }
                if (cierreFin == null || t.getHoraFin().isAfter(cierreFin)) {
                    cierreFin = t.getHoraFin();
                }
            } else {
                if (aperturaSemana == null || t.getHoraInicio().isBefore(aperturaSemana)) {
                    aperturaSemana = t.getHoraInicio();
                }
                if (cierreSemana == null || t.getHoraFin().isAfter(cierreSemana)) {
                    cierreSemana = t.getHoraFin();
                }
            }
        }

        dto.setHorarioAperturaSemana(aperturaSemana != null ? aperturaSemana.toString() : "--:--");
        dto.setHorarioCierreSemana(cierreSemana != null ? cierreSemana.toString() : "--:--");
        dto.setHorarioAperturaFin(aperturaFin != null ? aperturaFin.toString() : "--:--");
        dto.setHorarioCierreFin(cierreFin != null ? cierreFin.toString() : "--:--");

        // Fotos (vacío por ahora)
        dto.setFotos(new ArrayList<>());

        return ResponseEntity.ok(dto);
    }
    @GetMapping("/disponibilidad")
    @ResponseBody
    public ResponseEntity<?> verificarDisponibilidad(
            @RequestParam("idInstancia") Integer idInstancia,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<Reserva> reservas = reservaRepository
                .findByInstanciaServicio_IdInstanciaServicioAndFecha(idInstancia, fecha);

        List<Map<String, String>> bloquesOcupados = reservas.stream().map(r -> {
            Map<String, String> bloque = new HashMap<>();
            bloque.put("inicio", r.getHoraInicio().toString());
            bloque.put("fin", r.getHoraFin().toString());
            return bloque;
        }).toList();

        List<String> horasOcupadas = reservas.stream()
                .flatMap(r -> Stream.of(r.getHoraInicio(), r.getHoraFin()))
                .distinct()
                .sorted()
                .map(LocalTime::toString)
                .toList();

        boolean fechaLlena = reservas.size() >= 15;

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("bloques", bloquesOcupados);
        respuesta.put("horas", horasOcupadas);
        respuesta.put("fechaLlena", fechaLlena);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/fechasOcupadas")
    public ResponseEntity<Map<String, Object>> fechasOcupadas(
            @RequestParam("idInstancia") Integer idInstancia) {

        List<LocalDate> fechas = reservaRepository.findFechasLlenasByInstancia(idInstancia);

        Map<String, Object> response = new HashMap<>();
        response.put("fechas", fechas.stream().map(LocalDate::toString).toList());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/fechasDisponibles")
    @ResponseBody
    public List<String> obtenerFechasDisponibles(@RequestParam("idInstancia") Integer idInstancia) {
        LocalDate hoy = LocalDate.now();
        LocalDate hasta = hoy.plusMonths(3);

        List<Reserva> reservas = reservaRepository.findByInstanciaServicio_IdInstanciaServicioBetweenDates(idInstancia, hoy, hasta);

        Map<LocalDate, Integer> conteoPorFecha = new HashMap<>();
        for (Reserva r : reservas) {
            conteoPorFecha.put(r.getFecha(), conteoPorFecha.getOrDefault(r.getFecha(), 0) + 1);
        }

        // Supongamos que una fecha se considera "llena" si tiene 15 reservas
        return conteoPorFecha.entrySet().stream()
                .filter(e -> e.getValue() < 15)
                .map(e -> e.getKey().toString())
                .toList();
    }




}

