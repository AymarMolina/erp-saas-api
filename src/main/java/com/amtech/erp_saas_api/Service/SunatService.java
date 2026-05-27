package com.amtech.erp_saas_api.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.amtech.erp_saas_api.DTO.Response.SunatResponseDTO;

import lombok.RequiredArgsConstructor;

// src/main/java/com/amtech/erp_saas_api/Service/SunatService.java
@Service
@RequiredArgsConstructor
public class SunatService {

    @Value("${api.sunat.token}")
    private String sunatToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public SunatResponseDTO consultarRuc(String ruc) {
        String url = "https://api.decolecta.com/v1/sunat/ruc?numero=" + ruc;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + sunatToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            // Mapeamos directamente al DTO estructurado en lugar de Object.class
            return restTemplate.exchange(url, HttpMethod.GET, entity, SunatResponseDTO.class).getBody();
        } catch (HttpClientErrorException e) {
            // Captura errores de validación de la API externa (422, 404, etc.)
            throw new RuntimeException("Error al consultar en SUNAT: RUC no encontrado o inválido.");
        } catch (Exception e) {
            // Captura caídas del servicio externo o problemas de red
            throw new RuntimeException("El servicio de consulta SUNAT no está disponible en este momento.");
        }
    }
}