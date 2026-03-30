package com.vendalume.vendalume.security;

import com.vendalume.vendalume.domain.entity.User;
import com.vendalume.vendalume.domain.entity.Permission;
import com.vendalume.vendalume.repository.ProfileRepository;
import com.vendalume.vendalume.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filtro de autenticação JWT para requisições protegidas.
 *
 * @author VendaLume
 * @version 1.0.0
 * @since 2025-02-16
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CookieAuthHelper cookieAuthHelper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.isAccessToken(jwt)) {
                JwtClaims claims = jwtTokenProvider.parseToken(jwt);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    Optional<User> userOpt = userRepository.findById(claims.getUserId());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        if (user.isEnabled() && claims.getTokenVersion().equals(user.getRefreshTokenVersion())) {
                            Set<GrantedAuthority> authorities = new HashSet<>();
                            // Sempre mantém autoridades do role (ex.: ROLE_* e PERMISSION_FULL_SYSTEM_ACCESS)
                            authorities.addAll(user.getAuthorities().stream().map(a -> (GrantedAuthority) a).collect(Collectors.toSet()));

                            // Se usuário tiver profileId, adiciona permissões granulares do perfil
                            if (user.getProfileId() != null) {
                                profileRepository.findByIdWithPermissions(user.getProfileId()).ifPresent(profile -> {
                                    if (profile.getPermissions() != null) {
                                        for (Permission p : profile.getPermissions()) {
                                            if (p != null && p.getCode() != null && !p.getCode().isBlank()) {
                                                authorities.add(new SimpleGrantedAuthority("PERMISSION_" + p.getCode().trim()));
                                            }
                                        }
                                    }
                                });
                            }

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(user, null, authorities);
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            }
        } catch (JwtException e) {
            logger.debug("JWT inválido: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String fromCookie = cookieAuthHelper.getAccessTokenFromCookie(request);
        if (StringUtils.hasText(fromCookie)) {
            return fromCookie;
        }
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
