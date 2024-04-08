package com.microservice.ms.common.util.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.microservice.ms.common.util.services.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthTokenFilter extends OncePerRequestFilter {
	private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private TokenBlacklist tokenBlacklist;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain)
			throws ServletException,
			IOException {

		try {
			String jwt = parseJwt(request);
			if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

				if (!tokenBlacklist.isBlacklisted(jwt)) {

					String username = jwtUtils.getUserNameFromJwtToken(jwt);

					UserDetails userDetails = userDetailsService.loadUserByUsername(username);
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails,
							null,
							userDetails.getAuthorities());
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authentication);
				} else {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}

			}
		} catch (Exception e) {
			logger.error("Cannot set user authentication: {}", e);
		}

		filterChain.doFilter(request, response);
	}

	public static String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}

		return null;
	}

}
