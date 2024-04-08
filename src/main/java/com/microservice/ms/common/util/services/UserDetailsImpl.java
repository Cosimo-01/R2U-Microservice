package com.microservice.ms.common.util.services;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microservice.ms.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = 1L;

	private Long id;

	private String username;

	private String email;

	@JsonIgnore
	private String password;

	private String firstName;
	private String lastName;

	private String role;

	private String status;

	@JsonIgnore
	private Collection<? extends GrantedAuthority> authorities;

	///
	public UserDetailsImpl(Long id, String username, String email, String password, String firstName, String lastName,
			String role, String status) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.role = role;
		this.status = status;
	}
	//*/

	public static UserDetailsImpl build(User user) {
		return new UserDetailsImpl(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getPassword(),
				user.getFirstName(),
				user.getLastName(),
				user.getRole().name(),
				user.getStatus().name());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority(role));
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
