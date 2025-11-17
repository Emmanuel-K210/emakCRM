package com.emak.crm.service.impl;

import com.emak.crm.entity.Client;
import com.emak.crm.enums.StatutClient;
import com.emak.crm.enums.TypeClient;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class ClientSpecifications {

    public static Specification<Client> withSearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) {
                return criteriaBuilder.conjunction();
            }
            
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("entreprise")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("ville")), pattern)
            );
        };
    }

    public static Specification<Client> withTypeClient(String typeClient) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(typeClient)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("typeClient"), TypeClient.valueOf(typeClient));
        };
    }

    public static Specification<Client> withStatut(String statut) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(statut)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("statut"), StatutClient.valueOf(statut));
        };
    }

    public static Specification<Client> withVille(String ville) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(ville)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("ville"), ville);
        };
    }

    public static Specification<Client> withSecteurActivite(String secteur) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(secteur)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("secteurActivite"), secteur);
        };
    }

    public static Specification<Client> withScoreMin(Integer scoreMin) {
        return (root, query, criteriaBuilder) -> {
            if (scoreMin == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("scoreProspect"), scoreMin);
        };
    }

    public static Specification<Client> withScoreMax(Integer scoreMax) {
        return (root, query, criteriaBuilder) -> {
            if (scoreMax == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("scoreProspect"), scoreMax);
        };
    }

    public static Specification<Client> withCommercial(Long commercialId) {
        return (root, query, criteriaBuilder) -> {
            if (commercialId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("utilisateur").get("id"), commercialId);
        };
    }

    public static Specification<Client> withDateCreationBetween(LocalDateTime debut, LocalDateTime fin) {
        return (root, query, criteriaBuilder) -> {
            if (debut == null && fin == null) {
                return criteriaBuilder.conjunction();
            }
            if (debut != null && fin != null) {
                return criteriaBuilder.between(root.get("dateCreation"), debut, fin);
            }
            if (debut != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("dateCreation"), debut);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("dateCreation"), fin);
        };
    }
}