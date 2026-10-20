package org.greta.eshop_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.greta.eshop_api.exceptions.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

// Placé avant JwtAuthenticationFilter : son try/catch entoure tout ce qui vient après lui
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

    private final JsonMapper jsonMapper;

    public JwtExceptionHandlerFilter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtValidationException e) {
            // Seules les erreurs de jeton sont traitées ici : les autres continuent vers Spring
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            jsonMapper.writeValue(response.getWriter(), new ErrorResponse(e.getMessage()));
        }
    }
}
