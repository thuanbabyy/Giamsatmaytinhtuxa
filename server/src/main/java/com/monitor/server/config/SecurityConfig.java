package com.monitor.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(
                    "/",
                    "/server",
                    "/api",
                    "/api/**",
                    "/ws-client/**",
                    "/ws-stomp/**",
                    "/topic/**",
                    "/app/**",
                    "/user/**",
                    "/**/*.html", 
                    "/**/*.css", 
                    "/**/*.js",
                    "/**/*.png",
                    "/**/*.jpg",
                    "/**/*.jpeg",
                    "/**/*.gif",
                    "/**/*.svg",
                    "/**/*.ico",
                    "/**/*.woff",
                    "/**/*.woff2",
                    "/**/*.ttf",
                    "/**/static/**"
                ).permitAll()
                .anyRequest().authenticated()
            .and()
            .headers().frameOptions().sameOrigin()
            .and()
            .formLogin().disable()
            .httpBasic().disable();
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Dùng allowedOriginPatterns thay vì allowedOrigins để có thể dùng "*" với allowCredentials
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Tắt allowCredentials để tránh xung đột với "*" pattern
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        // Explicitly allow WebSocket headers
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers",
            "Access-Control-Max-Age",
            "Access-Control-Request-Headers",
            "Access-Control-Request-Method"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
