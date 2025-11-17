CREATE TABLE envois (
    id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Type et contenu
    type_envoi ENUM('EMAIL', 'SMS', 'NOTIFICATION') NOT NULL,
    objet VARCHAR(200),
    contenu TEXT,
    
    -- Statut
    statut ENUM('ENVOYE', 'OUVERT', 'CLIQUE', 'REPONDU', 'ECHOUE') DEFAULT 'ENVOYE',
    
    -- Dates
    date_envoi DATETIME NOT NULL,
    date_ouverture DATETIME,
    
    -- Relations
    id_campagne INT NOT NULL,
    id_client INT NOT NULL,
    
    -- Contraintes
    FOREIGN KEY (id_campagne) REFERENCES campagnes(id),
    FOREIGN KEY (id_client) REFERENCES clients(id)
);

-- Index
CREATE INDEX idx_envoi_campagne ON envois(id_campagne);
CREATE INDEX idx_envoi_client ON envois(id_client);
CREATE INDEX idx_envoi_date ON envois(date_envoi);
CREATE INDEX idx_envoi_statut ON envois(statut);