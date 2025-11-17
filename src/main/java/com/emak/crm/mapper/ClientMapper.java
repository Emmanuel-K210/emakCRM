package com.emak.crm.mapper;

import com.emak.crm.dto.ClientRequest;
import com.emak.crm.dto.ClientResponse;
import com.emak.crm.entity.Client;
import com.emak.crm.enums.OrigineClient;
import com.emak.crm.enums.StatutClient;
import com.emak.crm.enums.TypeClient;

public class ClientMapper {

    public static Client toEntity(ClientRequest request) {
        return Client.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .entreprise(request.entreprise())
                .email(request.email())
                .telephone(request.telephone())
                .adresse(request.adresse())
                .ville(request.ville())
                .codePostal(request.codePostal())
                .pays(request.pays() != null ? request.pays() : "France")
                .typeClient(TypeClient.valueOf(request.typeClient()))
                .origine(request.origine() != null ? 
                    OrigineClient.valueOf(request.origine()) : null)
                .scoreProspect(1)
                .statut(StatutClient.ACTIF)
                .build();
    }

    public static ClientResponse toResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .entreprise(client.getEntreprise())
                .email(client.getEmail())
                .telephone(client.getTelephone())
                .adresse(client.getAdresse())
                .ville(client.getVille())
                .codePostal(client.getCodePostal())
                .pays(client.getPays())
                .typeClient(client.getTypeClient().name())
                .statut(client.getStatut().name())
                .scoreProspect(client.getScoreProspect())
                .origine(client.getOrigine() != null ? client.getOrigine().name() : null)
                .idUtilisateurResponsable(client.getUtilisateurResponsable() != null ? 
                    client.getUtilisateurResponsable().getId() : null)
                .nomUtilisateurResponsable(client.getUtilisateurResponsable() != null ? 
                    client.getUtilisateurResponsable().getPrenom() + " " + client.getUtilisateurResponsable().getNom() : null)
                .dateCreation(client.getDateCreation())
                .build();
    }
}