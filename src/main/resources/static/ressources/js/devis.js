document.addEventListener("DOMContentLoaded", function() {

    // Ouvre le modal pour créer ou éditer un devis
    window.openEditModal = function(id, clientId, montantTotal, statut) {
        document.getElementById('devisId').value = id || '';
        document.getElementById('clientId').value = clientId || '';
        document.getElementById('montantTotal').value = montantTotal || '';
        document.getElementById('statut').value = statut || 'EN_ATTENTE';
        var modal = new bootstrap.Modal(document.getElementById('devisModal'));
        modal.show();
    }

    // Confirmation de suppression
    window.confirmDelete = function(url) {
        const deleteBtn = document.getElementById('deleteConfirmBtn');
        deleteBtn.setAttribute('href', url);
        var modal = new bootstrap.Modal(document.getElementById('confirmDeleteModal'));
        modal.show();
    }

});
