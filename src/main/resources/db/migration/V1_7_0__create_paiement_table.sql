CREATE TABLE paiements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL,
    -- Données paiement
    date_paiement DATE NOT NULL,
    montant DECIMAL(10,2) NOT NULL,
    mode_paiement ENUM('CARTE', 'VIREMENT', 'CHEQUE', 'ESPECES', 'PRELEVEMENT') NOT NULL,
    reference_transaction VARCHAR(100),
    statut ENUM('VALIDE', 'EN_ATTENTE', 'REFUSE') DEFAULT 'VALIDE',
    
    -- Relation
    id_facture INT NOT NULL,
    
    -- Audit
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    -- Contraintes
    FOREIGN KEY (id_facture) REFERENCES factures(id),
    CONSTRAINT chk_montant_positif CHECK (montant > 0)
);

-- Index
CREATE INDEX idx_paiement_facture ON paiements(id_facture);
CREATE INDEX idx_paiement_date ON paiements(date_paiement);
CREATE INDEX idx_paiement_statut ON paiements(statut);