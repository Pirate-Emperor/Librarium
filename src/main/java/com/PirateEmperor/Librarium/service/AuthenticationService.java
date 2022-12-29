package com.PirateEmperor.Librarium.service;

import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

@Service
public class AuthenticationService {
	static final long EXPIRATION_TIME = 864_000_00;
	static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	static final String PREFIX = "Bearer";

	public String getToken(String username) {
		String token = Jwts.builder()
				.setSubject(username)
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(key)
				.compact();
		return token;
	}

	public String getAuthUser(HttpServletRequest request) {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (token != null) {
			String user = Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(token.replace(PREFIX, ""))
					.getBody()
					.getSubject();

			if (user != null)
				return user;
		}

		return null;
	}
}
