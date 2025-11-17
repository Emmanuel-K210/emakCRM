CREATE TABLE ligne_devis (
    id_ligne INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Quantité et prix
    quantite INT NOT NULL DEFAULT 1,
    prix_unitaire_ht DECIMAL(10,2) NOT NULL,
    taux_tva DECIMAL(5,2) DEFAULT 20.0,
    remise DECIMAL(5,2) DEFAULT 0,
    
    -- Relations
    id_devis INT NOT NULL,
    id_produit INT NOT NULL,
    
    -- Contraintes
    FOREIGN KEY (id_devis) REFERENCES devis(id) ON DELETE CASCADE,
    FOREIGN KEY (id_produit) REFERENCES produits(id),
    CONSTRAINT chk_quantite_positive CHECK (quantite > 0),
    CONSTRAINT chk_remise CHECK (remise BETWEEN 0 AND 100)
);

-- Index
CREATE INDEX idx_ligne_devis ON ligne_devis(id_devis);
CREATE INDEX idx_ligne_produit ON ligne_devis(id_produit);