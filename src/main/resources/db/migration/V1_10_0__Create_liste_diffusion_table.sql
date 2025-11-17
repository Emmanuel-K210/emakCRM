CREATE TABLE liste_diffusions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_liste VARCHAR(100) NOT NULL,
    ld_description TEXT,
    nombre_contacts INT DEFAULT 0,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Index
CREATE INDEX idx_liste_nom ON liste_diffusions(nom_liste);