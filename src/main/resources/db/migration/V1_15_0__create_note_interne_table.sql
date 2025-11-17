CREATE TABLE note_internes(
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    contenu TEXT NOT NULL,
    note_interne_type ENUM('NOTE_INTERNE', 'RAPPEL', 'IDEE', 'SUIVI', 'DECISION', 'FEEDBACK') NOT NULL,
    privee BOOLEAN DEFAULT FALSE,
    id_auteur INT NOT NULL,
    id_client INT,
    id_opportunite INT,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (id_auteur) REFERENCES utilisateurs(id),
    FOREIGN KEY (id_client) REFERENCES clients(id),
    FOREIGN KEY (id_opportunite) REFERENCES opportunitees(id),
    
    INDEX idx_note_auteur (id_auteur),
    INDEX idx_note_client (id_client),
    INDEX idx_note_opportunite (id_opportunite),
    INDEX idx_note_type (note_interne_type),
    INDEX idx_note_privee (privee)
);