package com.dev.user_manage.filter;

import com.dev.user_manage.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        SecurityContext context = SecurityContextHolder.getContext();
        String authorizationHeader = request.getHeader("Authorization");

        // Verificar que el encabezado no sea nulo y comience con "Bearer "
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token sin "Bearer "
        String jwtToken = authorizationHeader.substring(7);

        try {
            String username = jwtService.extractSubject(jwtToken);

            if (username != null && context.getAuthentication() == null && jwtService.isTokenValid(jwtToken)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Manejar errores de autenticación (loggear, respuesta de error, etc.)
            System.err.println("Error al procesar el JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}


//@Component
//@RequiredArgsConstructor
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private final JwtService jwtService;
//    private final UserDetailsService userDetailsService;
//
//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest request,
//            @NonNull HttpServletResponse response,
//            @NonNull FilterChain filterChain
//    ) throws ServletException, IOException {
//        SecurityContext context = SecurityContextHolder.getContext();
//        String jwtToken = request.getHeader("Authorization");
//        if(jwtToken == null || context.getAuthentication() != null) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        jwtToken = jwtToken.substring(7);
//        String username = jwtService.extractSubject(jwtToken);
//        if (username != null && jwtService.isTokenValid(jwtToken)){
//            User user = (User) userDetailsService.loadUserByUsername(username);
//            var authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
//            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//            context.setAuthentication(authToken);
//        }
//        filterChain.doFilter(request, response);
//    }
//}
