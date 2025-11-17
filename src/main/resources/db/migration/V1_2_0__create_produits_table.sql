CREATE TABLE produits (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reference_sku VARCHAR(50) UNIQUE NOT NULL,
    nom_produit VARCHAR(100) NOT NULL,
    produit_description TEXT,
    categorie VARCHAR(50),
    famille VARCHAR(50),
    prix_unitaire_ht DECIMAL(10,2) NOT NULL,
    cout_unitaire DECIMAL(10,2),
    stock INT DEFAULT 0,
    actif BOOLEAN DEFAULT TRUE,
    gere_stock BOOLEAN DEFAULT TRUE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Index
CREATE INDEX idx_produit_sku ON produits(reference_sku);
CREATE INDEX idx_produit_categorie ON produits(categorie);
CREATE INDEX idx_produit_actif ON produits(actif);