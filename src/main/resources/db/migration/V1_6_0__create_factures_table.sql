CREATE TABLE factures (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero_facture VARCHAR(50) UNIQUE NOT NULL,
    
    -- Dates
    date_facture DATE NOT NULL,
    date_echeance DATE,
    
     -- Montants
    montant_ht DECIMAL(15,2) NOT NULL,  -- ✅ Augmenté la précision
    montant_ttc DECIMAL(15,2) NOT NULL,
    montant_tva DECIMAL(15,2),
    taux_tva DECIMAL(5,2) DEFAULT 20.0,
    montant_restant DECIMAL(15,2) DEFAULT 0,
    notes VARCHAR(500) DEFAULT '', 
    
    -- Statut
    statut ENUM('BROUILLON', 'ENVOYEE', 'PAYEE', 'EN_RETARD', 'ANNULEE','EMISE','PAYEE_PARTIELLEMENT') DEFAULT 'BROUILLON',
    
    -- Relations
    id_devis INT,
    id_client INT NOT NULL,
    id_utilisateur INT NOT NULL,
    
    -- Audit
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    -- Contraintes
    FOREIGN KEY (id_devis) REFERENCES devis(id),
    FOREIGN KEY (id_client) REFERENCES clients(id),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs(id)
);

-- Index
CREATE INDEX idx_facture_numero ON factures(numero_facture);
CREATE INDEX idx_facture_client ON factures(id_client);
CREATE INDEX idx_facture_statut ON factures(statut);
CREATE INDEX idx_facture_date_echeance ON factures(date_echeance);