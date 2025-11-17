function openEditModal(id, data) {
    document.getElementById('modalId').value = id; // exemple : factureId
    document.getElementById('clientId').value = data.clientId;
    document.getElementById('montant').value = data.montant;
    document.getElementById('statut').value = data.statut;
    var modal = new bootstrap.Modal(document.getElementById('modalIdModal'));
    modal.show();
}


function confirmDelete(url) {
    const deleteBtn = document.getElementById('deleteConfirmBtn');
    deleteBtn.setAttribute('href', url);
    var modal = new bootstrap.Modal(document.getElementById('confirmDeleteModal'));
    modal.show();
}
