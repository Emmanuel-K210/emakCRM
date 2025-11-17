CREATE TABLE utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telephone VARCHAR(20),
    mot_passe VARCHAR(255),
    user_role ENUM('COMMERCIAL', 'MARKETING', 'ADMIN', 'SUPPORT','MANAGER_COMMERCIAL') NOT NULL DEFAULT 'COMMERCIAL',
    equipe VARCHAR(50),
    date_embauche DATE,
    actif BOOLEAN DEFAULT TRUE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Index pour les recherches courantes
CREATE INDEX idx_utilisateur_email ON utilisateurs(email);
CREATE INDEX idx_utilisateur_role ON utilisateurs(user_role);
CREATE INDEX idx_utilisateur_actif ON utilisateurs(actif);