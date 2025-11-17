-- Insertion des utilisateurs par défaut
INSERT INTO utilisateurs (nom, prenom, email, user_role, equipe, date_embauche) VALUES
('Admin', 'System', 'admin@entreprise.com', 'ADMIN', 'Direction', '2024-01-01'),
('Dupont', 'Marie', 'marie.dupont@entreprise.com', 'COMMERCIAL', 'Ventes', '2024-01-15'),
('Martin', 'Pierre', 'pierre.martin@entreprise.com', 'COMMERCIAL', 'Ventes', '2024-02-01'),
('Bernard', 'Sophie', 'sophie.bernard@entreprise.com', 'MARKETING', 'Marketing', '2024-01-20');

-- Insertion de produits par défaut
INSERT INTO produits (reference_sku, nom_produit, produit_description, categorie, prix_unitaire_ht, stock) VALUES
('PROD-001', 'Formation CRM', 'Formation complète sur l utilisation du CRM', 'Formation', 1500.00, 100),
('PROD-002', 'Accompagnement stratégique', 'Accompagnement personnalisé pour votre entreprise', 'Consulting', 5000.00, 50),
('PROD-003', 'Audit commercial', 'Audit complet de vos processus commerciaux', 'Audit', 3000.00, 25),
('PROD-004', 'Support premium', 'Support technique prioritaire', 'Service', 500.00, 1000);

-- Insertion d'une liste de diffusion par défaut
INSERT INTO liste_diffusions (nom_liste, ld_description, nombre_contacts) VALUES
('Prospects actifs', 'Liste des prospects ayant manifesté un intérêt récent', 0),
('Clients fidèles', 'Clients avec plus de 2 achats', 0),
('Anciens clients', 'Clients inactifs depuis plus de 6 mois', 0);