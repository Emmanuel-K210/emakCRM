package com.emak.crm.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.emak.crm.enums.EtapeVente;
import com.emak.crm.enums.SourceOpportunite;
import com.emak.crm.enums.StatutOpportunite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "opportunitees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunite {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name="nom_opportunite")
	private String nomOpportunite;

	@Enumerated(EnumType.STRING)
    @Column(name="etape_vente")
	@Builder.Default
	private EtapeVente etapeVente = EtapeVente.PROSPECTION;
	@Builder.Default
	private Integer probabilite = 10;
	@Builder.Default
	@Enumerated(EnumType.STRING)
	private StatutOpportunite statut = StatutOpportunite.EN_COURS;
	 @Column(name="montant_estime")
	private BigDecimal montantEstime;
	 @Column(name="date_cloture_prevue")
	private LocalDate dateCloturePrevue;
	 @Column(name="opportunite_description")
	private String description;

	@Enumerated(EnumType.STRING)
	private SourceOpportunite source;

	@ManyToOne
	@JoinColumn(name = "id_client")
	private Client client;

	@ManyToOne
	@JoinColumn(name = "id_utilisateur")
	private Utilisateur utilisateur;
	@Builder.Default
	@Column(name="date_creation")
	private LocalDateTime dateCreation = LocalDateTime.now();
	@Builder.Default
	@Column(name="date_modification")
	private LocalDateTime dateModification = LocalDateTime.now();

	@PreUpdate
	public void preUpdate() {
		this.dateModification = LocalDateTime.now();
	}
}
