package com.microservice.ms.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.microservice.ms.common.util.jwt.AuthEntryPointJwt;

import static org.springframework.security.config.Customizer.withDefaults;
import com.microservice.ms.common.util.jwt.AuthTokenFilter;
import com.microservice.ms.common.util.services.UserDetailsServiceImpl;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @SuppressWarnings({ "deprecation" })
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        ///
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf
                        .disable())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeRequests(requests -> requests
                        .requestMatchers("/api/auth/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated());

        http
                .authenticationProvider(
                        authenticationProvider());

        http
                .addFilterBefore(
                        authenticationJwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class);
        //*/

        return http.build();
    }

}
