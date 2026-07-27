package com.jahm.bancocapsula.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jahm.bancocapsula.entity.GastosDTO;
import com.jahm.bancocapsula.entity.MovimientoEntity;
import com.jahm.bancocapsula.entity.UsuarioEntity;
import com.jahm.bancocapsula.repository.MovimientoCuentaRepository;
import com.jahm.bancocapsula.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/finanzas")
public class FinanzasRestController {
    private final UsuarioRepository usuarioRepository;
    private final MovimientoCuentaRepository movimientoCuentaRepository;

    //constructor
    private static final Map<String, String> COLOR_MAP = new HashMap<>();
    static {
        COLOR_MAP.put("alimentación", "#ff0000");
        COLOR_MAP.put("transporte", "#04609e");
        COLOR_MAP.put("vivienda", "#FFCE56");
        COLOR_MAP.put("otros", "#4BC0C0");
        COLOR_MAP.put("servicios", "#f10034");
        COLOR_MAP.put("ocio", "#36A2EB");
        COLOR_MAP.put("comida", "#FF6384");
        COLOR_MAP.put("renta", "#1ad7da");
        COLOR_MAP.put("nomina", "#7d5f14");
    }
    private static final List<String> PALETA_COLORES = Arrays.asList(
        "#ff0000", "#04609e", "#FFCE56", "#4BC0C0",
        "#f10034", "#36A2EB", "#FF6384", "#1ad7da", "#7d5f14"
    );

    public FinanzasRestController(UsuarioRepository usuarioRepository, MovimientoCuentaRepository movimientoCuentaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.movimientoCuentaRepository = movimientoCuentaRepository;
    }

    @GetMapping("/gastos-mes")
    public List<GastosDTO> obtenerGastosMes(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return Collections.emptyList();
        }
        String username = auth.getName();
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return Collections.emptyList();
        }

        UsuarioEntity usuario = usuarioOpt.get();
        if (usuario.getCuentas() == null || usuario.getCuentas().isEmpty()) {
            return Collections.emptyList();
        }

        String clabe = usuario.getCuentas().get(0).getClabe();
        List<MovimientoEntity> movimientos = movimientoCuentaRepository.findByCuentaOrigen(clabe);

        if(movimientos == null || movimientos.isEmpty()){
            return Collections.emptyList();
        }
        
        //agrupar por descripcion y sumar los montos
        Map<String, Double> gastosAgrupados = movimientos.stream()
            .collect(Collectors.groupingBy(MovimientoEntity::getDescripcion, 
                Collectors.summingDouble(MovimientoEntity::getMonto)));

        //mapeo a DTO y asignar colores
        List<GastosDTO> resultado = new ArrayList<>();
        int colorIdx = 0;
        for (Map.Entry<String, Double> entry : gastosAgrupados.entrySet()){
            String descripcion = entry.getKey();
            Double monto = entry.getValue();

            String key = descripcion == null ? "" : descripcion.toLowerCase().trim();
            String color = COLOR_MAP.get(key);
            if(color == null){
                color = PALETA_COLORES.get(colorIdx % PALETA_COLORES.size());
                colorIdx++;
            }
            resultado.add(new GastosDTO(descripcion, monto, color));
        }
        return resultado;
    }

    @GetMapping("/movimientos")
    public List<MovimientoEntity> obtenerMovimientos(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return Collections.emptyList();
        }

        String username = auth.getName();
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return Collections.emptyList();
        }

        UsuarioEntity usuario = usuarioOpt.get();
        if (usuario.getCuentas() == null || usuario.getCuentas().isEmpty()) {
            return Collections.emptyList();
        }

        String clabe = usuario.getCuentas().get(0).getClabe();
        List<MovimientoEntity> movimientos = movimientoCuentaRepository.findByCuentaOrigenOrCuentaDestinoOrderByFechaDesc(clabe, clabe);
        return movimientos == null ? Collections.emptyList() : movimientos;
    }
}