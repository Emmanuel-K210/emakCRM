CREATE TABLE campagnes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Identification
    nom_campagne VARCHAR(100) NOT NULL,
    type_campagne ENUM('EMAILING', 'SMS', 'EVENEMENT', 'TELEPHONIQUE') NOT NULL,
    
    -- Planning
    date_debut DATE,
    date_fin DATE,
    
    -- Budget et objectifs
    budget DECIMAL(10,2),
    objectif VARCHAR(200),
    
    -- Résultats
    statut ENUM('PLANIFIEE', 'EN_COURS', 'TERMINEE', 'ANNULEE') DEFAULT 'PLANIFIEE',
    taux_conversion DECIMAL(5,2),
    
    -- Relations
    id_utilisateur_responsable INT NOT NULL,
    
    -- Audit
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    -- Contraintes
    FOREIGN KEY (id_utilisateur_responsable) REFERENCES utilisateurs(id)
);

-- Index
CREATE INDEX idx_campagne_responsable ON campagnes(id_utilisateur_responsable);
CREATE INDEX idx_campagne_statut ON campagnes(statut);
CREATE INDEX idx_campagne_dates ON campagnes(date_debut, date_fin);