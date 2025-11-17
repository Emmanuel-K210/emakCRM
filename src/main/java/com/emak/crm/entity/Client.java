package com.emak.crm.entity;

import java.time.LocalDateTime;

import com.emak.crm.enums.OrigineClient;
import com.emak.crm.enums.StatutClient;
import com.emak.crm.enums.TypeClient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * @author KOUADIO EMMANUEL
 * SERVICE IMPL : {@link com.emak.crm.service.impl.ClientServiceImpl}
 */
public class Client {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nom;
	private String prenom;
	private String entreprise;
	private String email;
	private String telephone;
	private String adresse;
	private String ville;
	@Column(name="code_postal")
	private String codePostal;
	@Builder.Default
	private String pays = "Côte d'ivoire";

	@Enumerated(EnumType.STRING)
	@Builder.Default
	@Column(name="type_client")
	private TypeClient typeClient = TypeClient.PROSPECT;
	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(name="statut")
	private StatutClient statut = StatutClient.ACTIF;
	@Builder.Default
	@Column(name="score_prospect")
	private Integer scoreProspect = 1;

	@Enumerated(EnumType.STRING)
	private OrigineClient origine;

	@ManyToOne
	@JoinColumn(name = "id_utilisateur_responsable")
	private Utilisateur utilisateurResponsable;
	@Builder.Default
	@Column(name="date_creation")
	private LocalDateTime dateCreation = LocalDateTime.now();
	@Builder.Default
	@Column(name="date_modification")
	private LocalDateTime dateModification = LocalDateTime.now();

}
