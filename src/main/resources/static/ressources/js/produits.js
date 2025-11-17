function openEditProduit(id) {
    fetch('/produits/get/' + id)
      .then(res => res.json())
      .then(data => {
          document.getElementById('id').value = data.id;
          document.getElementById('libelle').value = data.libelle;
          document.getElementById('prix').value = data.prix;
          document.getElementById('stock').value = data.stock;
          document.getElementById('submitBtn').innerText = "Mettre à jour";
      });
}

function resetForm() {
    document.getElementById('produitId').value = '';
    document.getElementById('submitBtn').innerText = "Enregistrer";
}
