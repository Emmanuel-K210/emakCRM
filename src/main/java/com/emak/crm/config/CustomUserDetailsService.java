package com.emak.crm.config;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.emak.crm.entity.Utilisateur;
import com.emak.crm.repository.UtilisateurRepository;

import lombok.AllArgsConstructor;

/**
 * Charge un {@link Utilisateur} par son email pour l'authentification Spring Security.
 * L'email sert d'identifiant de connexion (username).
 */
@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur avec l'email : " + email));

        if (utilisateur.getMotPasse() == null || utilisateur.getMotPasse().isBlank()) {
            throw new UsernameNotFoundException("Ce compte n'a pas encore de mot de passe défini");
        }

        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotPasse())
                .disabled(!Boolean.TRUE.equals(utilisateur.getActif()))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())))
                .build();
    }
}
