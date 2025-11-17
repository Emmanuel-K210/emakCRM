CREATE TABLE clients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50),
    entreprise VARCHAR(100),
    email VARCHAR(100),
    telephone VARCHAR(20),
    adresse TEXT,
    ville VARCHAR(50),
    code_postal VARCHAR(10),
    pays VARCHAR(50) DEFAULT "Cote d'ivoire",
    
    -- Champs métier
    type_client ENUM('PROSPECT', 'CLIENT', 'ANCIEN_CLIENT') DEFAULT 'PROSPECT',
    statut ENUM('ACTIF', 'INACTIF', 'SUSPENDU') DEFAULT 'ACTIF',
    score_prospect TINYINT DEFAULT 1,
    origine ENUM('SITE_WEB', 'SALON', 'RECOMMANDATION', 'RESEAUX_SOCIAUX', 'PUBLICITE', 'AUTRE'),
    
    -- Responsable commercial
    id_utilisateur_responsable INT,
    
    -- Audit
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Contraintes
    FOREIGN KEY (id_utilisateur_responsable) REFERENCES utilisateurs(id),
    CONSTRAINT chk_score_prospect CHECK (score_prospect BETWEEN 1 AND 10)
);

-- Index pour performances
CREATE INDEX idx_client_email ON clients(email);
CREATE INDEX idx_client_entreprise ON clients(entreprise);
CREATE INDEX idx_client_type ON clients(type_client);
CREATE INDEX idx_client_responsable ON clients(id_utilisateur_responsable);
CREATE INDEX idx_client_ville ON clients(ville);