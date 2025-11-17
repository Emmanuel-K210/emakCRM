-- Table de liaison Campagne ↔ ListeDiffusion
CREATE TABLE campagne_listes (
    id_campagne INT,
    id_liste INT,
    date_liaison DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_campagne, id_liste),
    FOREIGN KEY (id_campagne) REFERENCES campagnes(id) ON DELETE CASCADE,
    FOREIGN KEY (id_liste) REFERENCES liste_diffusions(id) ON DELETE CASCADE
);

-- Table de liaison Client ↔ ListeDiffusion
CREATE TABLE liste_clients (
    id_liste INT,
    id_client INT,
    date_ajout DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_liste, id_client),
    FOREIGN KEY (id_liste) REFERENCES liste_diffusions(id) ON DELETE CASCADE,
    FOREIGN KEY (id_client) REFERENCES clients(id) ON DELETE CASCADE
);

-- Index pour les tables de liaison
CREATE INDEX idx_campagne_liste_campagne ON campagne_listes(id_campagne);
CREATE INDEX idx_campagne_liste_liste ON campagne_listes(id_liste);
CREATE INDEX idx_liste_client_liste ON liste_clients(id_liste);
CREATE INDEX idx_liste_client_client ON liste_clients(id_client);