package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.KardexAjusteRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.KardexResponseDTO;
import com.amtech.erp_saas_api.Entity.Kardex;
import com.amtech.erp_saas_api.Entity.Lote;
import com.amtech.erp_saas_api.Repository.KardexRepository;
import com.amtech.erp_saas_api.Repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KardexService {

    private final KardexRepository kardexRepository;
    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public List<KardexResponseDTO> obtenerHistorialPorProducto(Integer empresaId, Integer productoId) {
        return kardexRepository.findKardexPorProducto(productoId, empresaId)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<KardexResponseDTO> obtenerKardexPorFechas(Integer empresaId, LocalDateTime inicio, LocalDateTime fin) {
        return kardexRepository.findKardexPorRangoFechas(empresaId, inicio, fin)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional
    public KardexResponseDTO registrarAjusteManual(Integer empresaId, KardexAjusteRequestDTO request) {

        Lote lote = loteRepository.findByIdAndEmpresaId(request.loteId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado o no autorizado."));

        BigDecimal nuevoSaldo = lote.getCantidadActual().add(request.cantidad());
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("No puede ajustar una cantidad que deje el stock en negativo.");
        }

        lote.setCantidadActual(nuevoSaldo);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            lote.setEstado(Lote.EstadoLote.AGOTADO);
        }
        loteRepository.save(lote);

        Kardex nuevoKardex = Kardex.builder()
                .lote(lote)
                .fechaMovimiento(LocalDateTime.now())
                .tipoMovimiento(Kardex.TipoMovimiento.AJUSTE)
                .motivo("AJUSTE MANUAL: " + request.motivo())
                .cantidad(request.cantidad())
                .saldoLote(nuevoSaldo)
                .build();

        Kardex guardado = kardexRepository.save(nuevoKardex);

        return mapearAResponse(guardado);
    }

    private KardexResponseDTO mapearAResponse(Kardex k) {
        return new KardexResponseDTO(
                k.getId(),
                k.getFechaMovimiento(),
                k.getTipoMovimiento().name(),
                k.getMotivo(),
                k.getCantidad(),
                k.getSaldoLote(),
                k.getLote().getId(),
                k.getLote().getCodigoLote(),
                k.getLote().getProducto().getId(),
                k.getLote().getProducto().getNombre()
        );
    }
}