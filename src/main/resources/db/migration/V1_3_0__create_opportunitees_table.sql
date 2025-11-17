CREATE TABLE opportunitees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_opportunite VARCHAR(100) NOT NULL,
    
    -- Workflow vente
    etape_vente ENUM('PROSPECTION', 'QUALIFICATION', 'PROPOSITION', 'NEGOCIATION', 'SIGNATURE','GAGNE','PERDU') DEFAULT 'PROSPECTION',
    probabilite TINYINT NOT NULL DEFAULT 10,
    statut ENUM('EN_COURS', 'GAGNEE', 'PERDUE', 'ABANDONNEE') DEFAULT 'EN_COURS',
    
    -- Données financières
    montant_estime DECIMAL(10,2),
    date_cloture_prevue DATE,
    
    -- Description
    opportunite_description TEXT,
    source ENUM('SITE_WEB', 'SALON', 'RECOMMANDATION', 'CAMPAGNE_MARKETING', 'APPEL_ENTRANT', 'AUTRE'),
    
    -- Relations
    id_client INT NOT NULL,
    id_utilisateur INT NOT NULL,
    
    -- Audit
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Contraintes
    FOREIGN KEY (id_client) REFERENCES clients(id),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs(id),
    CONSTRAINT chk_probabilite CHECK (probabilite BETWEEN 10 AND 100)
);

-- Index
CREATE INDEX idx_opp_client ON opportunitees(id_client);
CREATE INDEX idx_opp_utilisateur ON opportunitees(id_utilisateur);
CREATE INDEX idx_opp_statut ON opportunitees(statut);
CREATE INDEX idx_opp_etape ON opportunitees(etape_vente);
CREATE INDEX idx_opp_date_cloture ON opportunitees(date_cloture_prevue);