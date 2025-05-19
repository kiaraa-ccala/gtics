package com.example.proyectosanmiguel.controller;

import com.example.proyectosanmiguel.dto.ComplejoDetalleDTO;
import com.example.proyectosanmiguel.entity.*;
import com.example.proyectosanmiguel.repository.*;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.time.format.TextStyle;
import java.util.Locale;
import java.text.Normalizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import com.example.proyectosanmiguel.repository.ReservaRepository;

import org.springframework.format.annotation.DateTimeFormat;
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

        dto.setFotos(new ArrayList<>());

        return ResponseEntity.ok(dto);
    }
    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidad(
            @RequestParam("idInstancia") Integer idInstancia,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        Map<String, Object> respuesta = new HashMap<>();

        Optional<InstanciaServicio> instanciaOpt = instanciaServicioRepository.findById(idInstancia);
        if (instanciaOpt.isEmpty()) {
            respuesta.put("fechaLlena", true);
            respuesta.put("bloques", List.of());
            return ResponseEntity.ok(respuesta);
        }

        Servicio servicio = instanciaOpt.get().getServicio();
        String diaSemana = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        // Normalizar para comparación robusta
        String diaNormalizado = Normalizer.normalize(diaSemana.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        // Buscar tarifas para ese día y servicio
        List<Tarifa> tarifas = tarifaRepository.findAll().stream()
                .filter(t -> t.getServicio().getIdServicio().equals(servicio.getIdServicio()))
                .filter(t -> {
                    String diaT = Normalizer.normalize(t.getDiaSemana().toLowerCase(), Normalizer.Form.NFD)
                            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                    return diaT.equals(diaNormalizado);
                })
                .toList();

        if (tarifas.isEmpty()) {
            respuesta.put("fechaLlena", true);
            respuesta.put("bloques", List.of());
            return ResponseEntity.ok(respuesta);
        }

        // Horas ocupadas por reservas
        List<Reserva> reservas = reservaRepository.findByInstanciaServicio_IdInstanciaServicioAndFecha(idInstancia, fecha);
        List<LocalTime> horasOcupadas = reservas.stream()
                .flatMap(r -> {
                    List<LocalTime> horas = new ArrayList<>();
                    for (LocalTime h = r.getHoraInicio(); h.isBefore(r.getHoraFin()); h = h.plusHours(1)) {
                        horas.add(h);
                    }
                    return horas.stream();
                }).toList();

        // Armar bloques con horas disponibles
        List<Map<String, Object>> bloques = new ArrayList<>();

        for (Tarifa tarifa : tarifas) {
            List<String> horasDisponibles = new ArrayList<>();

            for (LocalTime h = tarifa.getHoraInicio(); h.isBefore(tarifa.getHoraFin()); h = h.plusHours(1)) {
                if (!horasOcupadas.contains(h)) {
                    horasDisponibles.add(h.toString());
                }
            }

            if (!horasDisponibles.isEmpty()) {
                Map<String, Object> bloque = new HashMap<>();
                bloque.put("inicioTarifa", tarifa.getHoraInicio().toString());
                bloque.put("finTarifa", tarifa.getHoraFin().toString());
                bloque.put("monto", tarifa.getMonto());
                bloque.put("horasDisponibles", horasDisponibles);
                bloques.add(bloque);
            }
        }

        respuesta.put("fechaLlena", bloques.isEmpty());
        respuesta.put("bloques", bloques);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/fechasOcupadas")
    public ResponseEntity<Map<String, Object>> fechasOcupadas(@RequestParam("idInstancia") Integer idInstancia) {
        List<LocalDate> fechas = reservaRepository.findFechasLlenasByInstancia(idInstancia);
        Map<String, Object> response = new HashMap<>();
        response.put("fechas", fechas.stream().map(LocalDate::toString).toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fechasDisponibles")
    public ResponseEntity<List<String>> obtenerFechasDisponibles(@RequestParam("idInstancia") Integer idInstancia) {
        LocalDate hoy = LocalDate.now();
        LocalDate hasta = hoy.plusMonths(3);

        Optional<InstanciaServicio> instanciaOpt = instanciaServicioRepository.findById(idInstancia);
        if (instanciaOpt.isEmpty()) return ResponseEntity.badRequest().build();

        Servicio servicio = instanciaOpt.get().getServicio();

        List<String> fechasConDisponibilidad = new ArrayList<>();

        for (LocalDate fecha = hoy; !fecha.isAfter(hasta); fecha = fecha.plusDays(1)) {
            String diaSemana = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

            List<Tarifa> tarifas = tarifaRepository.findAll().stream()
                    .filter(t -> t.getServicio().getIdServicio().equals(servicio.getIdServicio()) &&
                            t.getDiaSemana().equalsIgnoreCase(diaSemana))
                    .toList();

            List<LocalTime> horasPosibles = new ArrayList<>();
            for (Tarifa t : tarifas) {
                for (LocalTime h = t.getHoraInicio(); h.isBefore(t.getHoraFin()); h = h.plusHours(1)) {
                    horasPosibles.add(h);
                }
            }

            List<Reserva> reservas = reservaRepository.findByInstanciaServicio_IdInstanciaServicioAndFecha(idInstancia, fecha);
            List<LocalTime> horasOcupadas = reservas.stream()
                    .flatMap(r -> {
                        List<LocalTime> horas = new ArrayList<>();
                        for (LocalTime h = r.getHoraInicio(); h.isBefore(r.getHoraFin()); h = h.plusHours(1)) {
                            horas.add(h);
                        }
                        return horas.stream();
                    }).toList();

            boolean hayHoraLibre = horasPosibles.stream().anyMatch(h -> !horasOcupadas.contains(h));

            if (hayHoraLibre) {
                fechasConDisponibilidad.add(fecha.toString());
            }
        }

        return ResponseEntity.ok(fechasConDisponibilidad);
    }



}
