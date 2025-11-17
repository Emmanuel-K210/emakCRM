CREATE TABLE interactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Type d'interaction
    type_interaction ENUM('APPEL', 'EMAIL', 'RENDEZ_VOUS', 'REUNION', 'NOTE') NOT NULL,
    objet VARCHAR(200) NOT NULL,
    
    -- Contenu
    compte_rendu TEXT,
    resultat ENUM('POSITIF', 'NEUTRE', 'NEGATIF', 'A_SUIVRE'),
    
    -- Dates
    date_interaction DATETIME NOT NULL,
    duree INT,
    
    -- Relations
    id_client INT NOT NULL,
    id_utilisateur INT NOT NULL,
    id_opportunite INT,
    
    -- Audit
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    -- Contraintes
    FOREIGN KEY (id_client) REFERENCES clients(id),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs(id),
    FOREIGN KEY (id_opportunite) REFERENCES opportunitees(id)
);

-- Index
CREATE INDEX idx_interaction_client ON interactions(id_client);
CREATE INDEX idx_interaction_utilisateur ON interactions(id_utilisateur);
CREATE INDEX idx_interaction_date ON interactions(date_interaction);
CREATE INDEX idx_interaction_opportunite ON interactions(id_opportunite);