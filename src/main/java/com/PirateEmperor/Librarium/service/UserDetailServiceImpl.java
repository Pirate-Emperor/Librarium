package com.PirateEmperor.Librarium.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.PirateEmperor.Librarium.UserProfile;
import com.PirateEmperor.Librarium.model.User;
import com.PirateEmperor.Librarium.model.UserRepository;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
	@Autowired
	private UserRepository urepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> user = urepository.findByUsername(username);

		UserProfile UserProfile = null;

		if (user.isPresent()) {
			User currentUser = user.get();

			boolean enabled = currentUser.isAccountVerified();

			UserProfile = new UserProfile(currentUser.getId(), username,
					currentUser.getPassword(), enabled, true, true, true,
					AuthorityUtils.createAuthorityList(currentUser.getRole()));
		} else {
			throw new UsernameNotFoundException("User (" + username + ") not found.");
		}

		return UserProfile;
	}
}
