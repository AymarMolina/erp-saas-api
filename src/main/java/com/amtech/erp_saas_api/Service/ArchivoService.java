package com.amtech.erp_saas_api.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ArchivoService {

    @Value("${app.uploads.directorio}")
    private String directorioBase;

    @Value("${app.uploads.url-base}")
    private String urlBase;

    private static final long MAX_BYTES = 2 * 1024 * 1024; // 2 MB
    private static final java.util.List<String> TIPOS_PERMITIDOS =
            java.util.List.of("image/jpeg", "image/png", "image/webp");

    public String guardar(MultipartFile archivo, String subcarpeta) {
        validar(archivo);

        try {
            Path directorio = Paths.get(directorioBase, subcarpeta);
            Files.createDirectories(directorio);

            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreUnico = UUID.randomUUID() + "." + extension;

            Files.copy(archivo.getInputStream(), directorio.resolve(nombreUnico));

            return urlBase + "/" + subcarpeta + "/" + nombreUnico;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }

    private void validar(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("El archivo supera el límite de 2 MB");
        }
        if (!TIPOS_PERMITIDOS.contains(archivo.getContentType())) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPG, PNG o WEBP");
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) return "jpg";
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase();
    }
    public void eliminar(String url) {
        if (url == null || url.isBlank()) return;

        try {
            // Tu URL se ve así: "http://localhost:8080/uploads/logos/nombre-unico.png"
            // Rompemos la URL a partir de "/uploads/" para obtener la ruta del archivo en el servidor
            String[] partes = url.split("/uploads/");

            if (partes.length > 1) {
                String rutaRelativa = "uploads/" + partes[1]; // Queda: "uploads/logos/nombre-unico.png"
                Path path = Paths.get(rutaRelativa);

                // Borra el archivo del disco si es que existe
                Files.deleteIfExists(path);
            }
        } catch (Exception e) {
            // Capturamos cualquier error (como problemas de permisos) para que no rompa el flujo
            System.err.println("No se pudo eliminar el archivo físico del disco: " + e.getMessage());
        }
    }
}