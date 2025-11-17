function openEditModal(id) {
	fetch('/clients/get/' + id)
		.then(response => response.json())
		.then(data => {
			// remplir les champs
			document.getElementById('clientId').value = data.id;
			document.getElementById('nom').value = data.nom;
			document.getElementById('entreprise').value = data.entreprise;
			document.getElementById('email').value = data.email;
			document.getElementById('telephone').value = data.telephone;
			document.getElementById('statut').value = data.statut;
			document.getElementById('adresse').value = data.adresse;
			document.getElementById('modalTitle').innerText = "Modifier le client";
			document.getElementById('submitBtn').innerText = "Mettre à jour";

			const modal = new bootstrap.Modal(document.getElementById('clientModal'));
			modal.show();
		});
}

function confirmDelete(url) {
	const btn = document.getElementById('deleteConfirmBtn');
	btn.setAttribute('href', url);
	const modal = new bootstrap.Modal(document.getElementById('confirmDeleteModal'));
	modal.show();
}

setTimeout(() => {
	const alerts = document.querySelectorAll('.alert');
	alerts.forEach(alert => {
		const bsAlert = new bootstrap.Alert(alert);
		bsAlert.close();
	});
}, 2000); // 3 secondes