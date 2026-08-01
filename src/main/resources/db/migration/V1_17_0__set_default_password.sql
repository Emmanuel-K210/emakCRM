-- Mot de passe par défaut pour les comptes de démonstration créés en V1_14_0.
-- Valeur en clair : "password123" (hash BCrypt ci-dessous)
-- ⚠️ À changer immédiatement après la première connexion en production.
UPDATE utilisateurs
SET mot_passe = '$2b$10$BYqSBFHVLEJwStdwawxXM.yd6F9xZNtri8GVGPojs3FEy/QIZbXRK'
WHERE mot_passe IS NULL;
