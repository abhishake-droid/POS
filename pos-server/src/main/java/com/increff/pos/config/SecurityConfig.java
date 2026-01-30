package com.increff.pos.config;

import com.increff.pos.dto.AuthDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthDto authDto;

    public SecurityConfig(AuthDto authDto) {
        this.authDto = authDto;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/api/user/**").hasRole("SUPERVISOR")
                        .requestMatchers("/api/report/**").hasRole("SUPERVISOR")
                        .requestMatchers("/api/audit-log/**").hasRole("SUPERVISOR")
                        .requestMatchers("/api/scheduler/**").hasRole("SUPERVISOR")
                        .requestMatchers(HttpMethod.POST, "/api/client/add").hasRole("SUPERVISOR")
                        .requestMatchers(HttpMethod.PUT, "/api/client/update/**").hasRole("SUPERVISOR")
                        .requestMatchers(HttpMethod.POST, "/api/product/add").hasRole("SUPERVISOR")
                        .requestMatchers(HttpMethod.PUT, "/api/product/update/**").hasRole("SUPERVISOR")
                        .requestMatchers(HttpMethod.PUT, "/api/product/update-inventory/**").hasRole("SUPERVISOR")
                        .requestMatchers("/api/product/upload-**").hasRole("SUPERVISOR")
                        .requestMatchers("/api/order/**").hasAnyRole("USER", "SUPERVISOR")
                        .requestMatchers("/api/invoice/**").hasAnyRole("USER", "SUPERVISOR")
                        .requestMatchers(HttpMethod.POST, "/api/client/get-all-paginated")
                        .hasAnyRole("USER", "SUPERVISOR")
                        .requestMatchers(HttpMethod.GET, "/api/client/get-by-id/**").hasAnyRole("USER", "SUPERVISOR")
                        .requestMatchers(HttpMethod.POST, "/api/product/get-all-paginated")
                        .hasAnyRole("USER", "SUPERVISOR")
                        .requestMatchers(HttpMethod.GET, "/api/product/get-by-id/**").hasAnyRole("USER", "SUPERVISOR")
                        .requestMatchers(HttpMethod.GET, "/api/product/get-by-barcode/**")
                        .hasAnyRole("USER", "SUPERVISOR")
                        .anyRequest().authenticated())
                .addFilterBefore(new TokenAuthenticationFilter(authDto), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
