package com.amtech.erp_saas_api.Security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal personalizado del ERP SaaS.
 * Cargado desde el JWT en cada request (sin tocar la BD).
 * Disponible en controllers via @AuthenticationPrincipal ErpUserDetails.
 */
@Getter
public class ErpUserDetails implements UserDetails {

    private final Integer usuarioId;
    private final String username;
    private final Integer empresaId;
    private final String rol;

    public ErpUserDetails(Integer usuarioId, String username, Integer empresaId, String rol) {
        this.usuarioId = usuarioId;
        this.username  = username;
        this.empresaId = empresaId;
        this.rol       = rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override public String getPassword()           { return null; }
    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return true; }
}