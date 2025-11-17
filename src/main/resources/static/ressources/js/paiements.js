// ressources/js/paiements.js

document.addEventListener("DOMContentLoaded", () => {

    // Elements du modal
    const paiementModal = new bootstrap.Modal(document.getElementById('paiementModal'));
    const modalTitle = document.getElementById('modalTitle');
    const paiementIdInput = document.getElementById('paiementId');
    const factureSelect = document.getElementById('factureId');
    const montantInput = document.getElementById('montant');
    const modePaiementSelect = document.getElementById('modePaiement');
    const statutSelect = document.getElementById('statut');

    // Fonction pour ouvrir le modal en mode création
    window.openCreateModal = function () {
        modalTitle.textContent = 'Nouveau paiement';
        paiementIdInput.value = '';
        factureSelect.selectedIndex = 0;
        montantInput.value = '';
        modePaiementSelect.selectedIndex = 0;
        statutSelect.selectedIndex = 0;
        paiementModal.show();
    };

    // Fonction pour ouvrir le modal en mode édition
    window.openEditModal = function (id) {
        const row = document.querySelector(`tr td:first-child[text="${id}"]`)?.parentElement;
        if (!row) return;

        modalTitle.textContent = 'Modifier paiement';
        paiementIdInput.value = id;
        factureSelect.value = row.querySelector('td:nth-child(1)').textContent.trim();
        montantInput.value = row.querySelector('td:nth-child(3)').textContent.trim();
        modePaiementSelect.value = row.querySelector('td:nth-child(5)').textContent.trim();
        statutSelect.value = row.querySelector('td:nth-child(6) span').textContent.trim();

        paiementModal.show();
    };

    // Confirmation de suppression
    window.confirmDelete = function (url) {
        if (confirm('Êtes-vous sûr de vouloir supprimer ce paiement ?')) {
            window.location.href = url;
        }
    };

});
