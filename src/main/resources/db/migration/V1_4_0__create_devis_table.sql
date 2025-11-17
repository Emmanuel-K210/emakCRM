CREATE TABLE devis (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_devis VARCHAR(50) UNIQUE NOT NULL,
    
    -- Dates
    date_emission DATE NOT NULL,
    date_validite DATE,
    
    -- Montants
    montant_ht DECIMAL(10,2),
    montant_tva DECIMAL(10,2),
    montant_ttc DECIMAL(10,2),
    taux_tva DECIMAL(5,2) DEFAULT 20.0,
    
    -- Statut
    statut ENUM('BROUILLON', 'ENVOYE', 'ACCEPTE', 'REFUSE', 'EXPIRE','ANNULE','EN_ATTENTE','MODIFIE','CONVERTI_EN_FACTURE') DEFAULT 'BROUILLON',
    
    -- Autres états devis
    conditions_generales TEXT,
    notes TEXT,
    deja_facture BOOLEAN DEFAULT FALSE,
    
    -- Relations
    id_opportunite INT NOT NULL,
    id_utilisateur INT NOT NULL,
    
    -- Audit
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Contraintes
    FOREIGN KEY (id_opportunite) REFERENCES opportunitees(id),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs(id)  -- ✅ Corrigé le nom de la table
);

-- Index
CREATE INDEX idx_devis_numero ON devis(numero_devis);
CREATE INDEX idx_devis_opportunite ON devis(id_opportunite);
CREATE INDEX idx_devis_statut ON devis(statut);
CREATE INDEX idx_devis_date_emission ON devis(date_emission);