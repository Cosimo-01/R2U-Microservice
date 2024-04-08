package com.microservice.ms.common.util.services;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microservice.ms.model.User;
import com.microservice.ms.model.enums.UserStatus;

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

	private boolean nonExpiredCredentials = true;

	private String firstName;
	private String lastName;

	private String role;

	private String status;
	private LocalDate deletedAt;

	@JsonIgnore
	private Collection<? extends GrantedAuthority> authorities;

	///
	public UserDetailsImpl(Long id, String username, String email, String password, String firstName, String lastName,
			String role, LocalDate deletedAt, String status) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.role = role;
		this.deletedAt = deletedAt;
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
				user.getDeletedAt(),
				user.getStatus().name());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority(role));
	}

	@Override
	public boolean isAccountNonExpired() {
		if (status == UserStatus.DELETED.toString() && deletedAt.plusYears(1).isAfter(LocalDate.now())) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean isAccountNonLocked() {
		if (status == UserStatus.TEMPORARY_LOCK.toString() || status == UserStatus.PERMANENT_LOCK.toString()) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return nonExpiredCredentials;
	}

	@Override
	public boolean isEnabled() {
		if (status == UserStatus.DELETED.toString()) {
			return false;
		} else {
			return true;
		}
	}

}
