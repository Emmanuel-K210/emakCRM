CREATE TABLE taches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Description
    titre VARCHAR(100) NOT NULL,
    tache_description TEXT,
    
    -- Planning
    date_debut DATETIME,
    date_echeance DATETIME,
    
    -- Priorité et statut
    priorite ENUM('BASSE', 'NORMALE', 'HAUTE', 'URGENTE') DEFAULT 'NORMALE',
    statut ENUM('A_FAIRE', 'EN_COURS', 'TERMINE', 'ANNULE') DEFAULT 'A_FAIRE',
    
    -- Relations
    id_utilisateur INT NOT NULL,
    id_client INT,
    id_opportunite INT,
    
    -- Audit
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Contraintes
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs(id),
    FOREIGN KEY (id_client) REFERENCES clients(id),
    FOREIGN KEY (id_opportunite) REFERENCES opportunitees(id)
);

-- Index
CREATE INDEX idx_tache_utilisateur ON taches(id_utilisateur);
CREATE INDEX idx_tache_client ON taches(id_client);
CREATE INDEX idx_tache_echeance ON taches(date_echeance);
CREATE INDEX idx_tache_statut ON taches(statut);