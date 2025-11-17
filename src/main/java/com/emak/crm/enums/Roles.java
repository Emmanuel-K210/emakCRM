package com.emak.crm.enums;

import java.util.stream.Stream;

import lombok.Getter;

@Getter

public enum Roles {

	ADMIN("ADMIN"), COMMERCIAL("COMMERCIAL"), MARKETING("MARKETING"), SUPPORT("SUPPORT"),
	MANAGER_COMMERCIAL("MANAGER_COMMERCIAL");

	private final String nom;

	private Roles(String nom) {
		this.nom = nom;
	}

	public static Roles of(String role) {
		return Stream
				.of(values())
				.filter(r -> r.nom.equals(role))
				.findFirst()
				.orElse(MANAGER_COMMERCIAL);
	}
}
