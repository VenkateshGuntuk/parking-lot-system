package com.example.parking.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Value("${app.security.admin-emails}")
	private String adminEmailsCsv;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/token").permitAll()
						.requestMatchers("/h2-console/**").permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/**").authenticated()
						.anyRequest().permitAll()
				)
				.headers(h -> h.frameOptions(f -> f.disable()))
//				.oauth2Login(o -> o
//						.userInfoEndpoint(u -> u.oidcUserService(oidcUserService()))
//				)
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(Customizer.withDefaults())
				)
				.exceptionHandling(e -> e
						.authenticationEntryPoint((request, response, authException) ->
								response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
						)
				);

		return http.build();
	}

	@Bean
	OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
		OidcUserService delegate = new OidcUserService();
		Set<String> adminEmails = Arrays.stream(adminEmailsCsv.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toSet());

		return userRequest -> {
			OidcUser user = delegate.loadUser(userRequest);
			String email = Optional.ofNullable(user.getEmail())
					.orElse(user.getUserInfo().getEmail());
			System.out.println("User email: " + email);
			List<GrantedAuthority> mapped = new ArrayList<>();
			mapped.add(new SimpleGrantedAuthority("ROLE_USER"));
			if (adminEmails.contains(email)) mapped.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			System.out.println("Authorities: " + mapped);
			return new DefaultOidcUser(mapped, user.getIdToken(), user.getUserInfo());
		};
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("https://accounts.google.com"));
		return decoder;
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		Set<String> adminEmails = Arrays.stream(adminEmailsCsv.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toSet());
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			String email = jwt.getClaimAsString("email");
			System.out.println("User email: " + email);
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
			if (adminEmails.contains(email)) {
				authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			}
			System.out.println("Authorities: " + authorities);
			return authorities;
		});
		return converter;
	}


}
