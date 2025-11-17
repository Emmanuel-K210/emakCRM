package com.emak.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "liste_diffusions")
public class ListeDiffusion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nomListe;
    @Column(name="ld_description")
    private String description;
    @Builder.Default
    @Column(name="nombre_contacts")
    private Integer nombreContacts = 0;
    
    @ManyToMany(mappedBy = "listes")
    @Builder.Default
    private List<Campagne> campagnes = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "liste_client",
        joinColumns = @JoinColumn(name = "id_liste"),
        inverseJoinColumns = @JoinColumn(name = "id_client")
    )
    @Builder.Default
    private List<Client> clients = new ArrayList<>();
    @Builder.Default
    @Column(name="date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();
    
    @PreUpdate
    @PrePersist
    public void updateNombreContacts() {
        this.nombreContacts = clients != null ? clients.size() : 0;
    }
}