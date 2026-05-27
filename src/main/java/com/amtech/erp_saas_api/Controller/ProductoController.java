package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.ProductoRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.ProductoResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @Valid @RequestBody ProductoRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoService.crearProducto(userDetails.getEmpresaId(), request));
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarOBuscar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) String nombre) { // <-- required = false es la clave

        if (nombre != null && !nombre.isBlank()) {
            // Si envían un nombre, buscamos por coincidencias
            return ResponseEntity.ok(productoService.buscarPorNombre(userDetails.getEmpresaId(), nombre));
        }
        
        // Si no envían nombre, devolvemos todo el catálogo
        return ResponseEntity.ok(productoService.listarTodos(userDetails.getEmpresaId()));
    }

    @GetMapping("/codigo/{codigoBarras}")
    public ResponseEntity<ProductoResponseDTO> escanearCodigo(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable String codigoBarras) {

        return ResponseEntity.ok(
                productoService.buscarPorCodigoBarrasParaVenta(userDetails.getEmpresaId(), codigoBarras));
    }


    // Agrégalo debajo de tu método @PostMapping en ProductoController.java

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody ProductoRequestDTO request) {

        return ResponseEntity.ok(
                productoService.actualizarProducto(userDetails.getEmpresaId(), id, request));
    }
}