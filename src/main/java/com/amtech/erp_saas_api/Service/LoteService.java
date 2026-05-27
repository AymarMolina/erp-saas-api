package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Response.LoteResponseDTO;
import com.amtech.erp_saas_api.Repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> obtenerLotesActivos(Integer empresaId) {
        return loteRepository.findLotesDisponiblesByEmpresa(empresaId).stream()
                .map(l -> new LoteResponseDTO(
                        l.getId(),
                        l.getProducto().getId(),
                        l.getProducto().getNombre(),
                        l.getCompra().getId(),
                        l.getCodigoLote(),
                        l.getFechaFabricacion(),
                        l.getFechaVencimiento(),
                        l.getCostoUnitario(),
                        l.getCantidadInicial(),
                        l.getCantidadActual(),
                        l.getEstado().name()
                ))
                .toList();
    }
}