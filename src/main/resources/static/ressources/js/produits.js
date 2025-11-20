class ProduitManager {
    constructor() {
        this.produits = [];
        this.currentProduitId = null;
        this.init();
    }

    init() {
        this.loadProduits();
        this.setupEventListeners();
    }

    async loadProduits() {
        try {
            const response = await fetch('/api/produits/all');
            if (!response.ok) throw new Error('Erreur chargement produits');
            
            this.produits = await response.json();
            this.renderTable();
            
        } catch (error) {
            console.error('Erreur:', error);
            this.showAlert('Erreur lors du chargement des produits', 'danger');
        }
    }

    renderTable() {
		
		const table = $('#produitsTable');
		    
		    // ✅ Détruire DataTable si elle existe déjà
		    if ($.fn.DataTable.isDataTable('#produitsTable')) {
		        table.DataTable().destroy();
		    }
			
        const tbody = document.querySelector('#produitsTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        this.produits.forEach(produit => {
            const row = document.createElement('tr');
            const stockStatus = this.getStockStatus(produit.stock);
            
            row.innerHTML = `
                <td>
                    <strong>${this.escapeHtml(produit.referenceSku)}</strong>
                </td>
                <td>
                    <strong>${this.escapeHtml(produit.nomProduit)}</strong>
                    ${produit.description ? `<br><small class="text-muted">${this.escapeHtml(produit.description)}</small>` : ''}
                </td>
                <td>
                    <span class="badge bg-secondary">${produit.categorie || 'Non classé'}</span>
                </td>
                <td>
                    <strong>${produit.prixUnitaireHt ? produit.prixUnitaireHt.toFixed(2) + ' €' : '0.00 €'}</strong>
                </td>
                <td>
                    ${produit.coutUnitaire ? produit.coutUnitaire.toFixed(2) + ' €' : '-'}
                </td>
                <td>
                    <span class="badge ${stockStatus.class}">
                        ${produit.stock !== null ? produit.stock : 'N/A'}
                    </span>
                </td>
                <td>
                    <span class="badge ${produit.actif ? 'bg-success' : 'bg-secondary'}">
                        ${produit.actif ? 'Actif' : 'Inactif'}
                    </span>
                </td>
                <td>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-primary edit-produit" data-id="${produit.id}">
                            <i class="ti ti-edit"></i>
                        </button>
                        <button class="btn btn-outline-warning update-stock" data-id="${produit.id}">
                            <i class="ti ti-package"></i>
                        </button>
                        <button class="btn btn-outline-danger delete-produit" data-id="${produit.id}">
                            <i class="ti ti-trash"></i>
                        </button>
                    </div>
                </td>
            `;

            // Écouteurs d'événements
            row.querySelector('.edit-produit').addEventListener('click', () => {
                this.editProduit(produit.id);
            });
            
            row.querySelector('.update-stock').addEventListener('click', () => {
                this.showStockModal(produit.id, produit.nomProduit, produit.stock || 0);
            });
            
            row.querySelector('.delete-produit').addEventListener('click', () => {
                this.deleteProduit(produit.id);
            });

            tbody.appendChild(row);
        });

		// ✅ Recréer DataTable
		   table.DataTable({
		       language: {
		           url: '//cdn.datatables.net/plug-ins/1.13.6/i18n/fr-FR.json'
		       },
		       pageLength: 25,
		       order: [[0, 'asc']]
		   });
    }

    getStockStatus(stock) {
        if (stock === null || stock === undefined) return { class: 'bg-secondary', text: 'N/A' };
        if (stock === 0) return { class: 'bg-danger', text: 'Rupture' };
        if (stock <= 5) return { class: 'bg-warning text-dark', text: 'Faible' };
        return { class: 'bg-success', text: 'OK' };
    }

    setupEventListeners() {
        // Formulaire création
        document.getElementById('produitForm').addEventListener('submit', (e) => {
            this.handleFormSubmit(e);
        });

        // Filtres
        document.getElementById('searchInput').addEventListener('input', () => this.applyFilters());
        document.getElementById('categoryFilter').addEventListener('change', () => this.applyFilters());
        document.getElementById('stockFilter').addEventListener('change', () => this.applyFilters());
        document.getElementById('resetFilters').addEventListener('click', () => this.resetFilters());

        // Modal stock
        document.getElementById('confirmStockUpdate').addEventListener('click', () => {
            this.updateStock();
        });

        // Reset formulaire modal
        document.getElementById('addProduitModal').addEventListener('hidden.bs.modal', () => {
            this.resetForm();
        });
    }

	async handleFormSubmit(e) {
	    e.preventDefault();
	    
	    const form = e.target;
	    const formData = new FormData(form);
	    
	    const produitData = {
	        referenceSku: formData.get('referenceSku'),
	        nomProduit: formData.get('nomProduit'),
	        description: formData.get('description'),
	        categorie: formData.get('categorie'),
	        famille: formData.get('famille'),
	        prixUnitaireHt: parseFloat(formData.get('prixUnitaireHt')) || 0,
	        coutUnitaire: parseFloat(formData.get('coutUnitaire')) || 0,
	        stock: parseInt(formData.get('stock')) || 0
	    };

	    try {
	        // ✅ DÉTERMINER LA MÉTHODE CORRECTEMENT
	        const isUpdate = form.dataset.editId; // Si on a un ID d'édition
	        const url = isUpdate ? `/api/produits/${form.dataset.editId}` : '/api/produits';
	        const method = isUpdate ? 'PUT' : 'POST';

	        console.log(`🌐 ${method} ${url}`, produitData);

	        const response = await fetch(url, {
	            method: method,
	            headers: {
	                'Content-Type': 'application/json',
	            },
	            body: JSON.stringify(produitData)
	        });

	        if (!response.ok) {
	            const errorText = await response.text();
	            throw new Error(errorText || `Erreur ${response.status}`);
	        }

	        const result = await response.json();
	        this.showAlert(isUpdate ? 'Produit mis à jour avec succès' : 'Produit créé avec succès', 'success');
	        
	        // Fermer le modal et recharger
	        bootstrap.Modal.getInstance(document.getElementById('addProduitModal')).hide();
	        await this.loadProduits();
	        this.resetForm();
	        
	    } catch (error) {
	        console.error('💥 Erreur:', error);
	        this.showAlert('Erreur: ' + error.message, 'danger');
	    }
	}

    async editProduit(produitId) {
        try {
            const response = await fetch(`/api/produits/${produitId}`);
            if (!response.ok) throw new Error('Produit non trouvé');
            
            const produit = await response.json();
            this.showEditModal(produit);
            
        } catch (error) {
            this.showAlert('Erreur lors du chargement du produit', 'danger');
        }
    }

	showEditModal(produit) {
	    console.log('🔄 Chargement produit pour édition:', produit);
	    
	    const form = document.getElementById('produitForm');
	    
	    // Remplissage des champs
	    form.querySelector('input[name="referenceSku"]').value = produit.referenceSku || '';
	    form.querySelector('input[name="nomProduit"]').value = produit.nomProduit || '';
	    form.querySelector('textarea[name="description"]').value = produit.description || '';
	    form.querySelector('select[name="categorie"]').value = produit.categorie || '';
	    form.querySelector('input[name="famille"]').value = produit.famille || '';
	    form.querySelector('input[name="prixUnitaireHt"]').value = produit.prixUnitaireHt || '';
	    form.querySelector('input[name="coutUnitaire"]').value = produit.coutUnitaire || '';
	    form.querySelector('input[name="stock"]').value = produit.stock || 0;

	    // ✅ CORRECTION : Stocker l'ID pour déterminer la méthode plus tard
	    form.dataset.editId = produit.id;
	    form.action = `/api/produits/${produit.id}`;

	    // Changer le texte du modal
	    document.querySelector('#addProduitModal .modal-title').textContent = 'Modifier le Produit';
	    document.querySelector('#addProduitModal .btn-primary').textContent = 'Mettre à jour';

	    // Ouvrir le modal
	    new bootstrap.Modal(document.getElementById('addProduitModal')).show();
	}
    showStockModal(produitId, nomProduit, currentStock) {
        this.currentProduitId = produitId;
        
        const modal = document.getElementById('stockModal');
        modal.querySelector('.modal-title').textContent = `Stock - ${nomProduit}`;
        modal.querySelector('#newStockInput').value = currentStock;
        
        new bootstrap.Modal(modal).show();
    }

    async updateStock() {
        if (!this.currentProduitId) return;

        const newStock = parseInt(document.getElementById('newStockInput').value);
        if (isNaN(newStock) || newStock < 0) {
            this.showAlert('Valeur de stock invalide', 'danger');
            return;
        }

        try {
            const response = await fetch(`/api/produits/${this.currentProduitId}/stock?stock=${newStock}`, {
                method: 'PUT'
            });

            if (response.ok) {
                this.showAlert('Stock mis à jour avec succès', 'success');
                bootstrap.Modal.getInstance(document.getElementById('stockModal')).hide();
                this.loadProduits();
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            this.showAlert('Erreur mise à jour stock: ' + error.message, 'danger');
        }
    }

    async deleteProduit(produitId) {
        if (!confirm('Êtes-vous sûr de vouloir supprimer ce produit ?')) {
            return;
        }

        try {
            const response = await fetch(`/api/produits/${produitId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                this.showAlert('Produit supprimé avec succès', 'success');
                this.loadProduits();
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            this.showAlert('Erreur suppression: ' + error.message, 'danger');
        }
    }

    applyFilters() {
        // Implémentation des filtres
        console.log('Filtres appliqués');
    }

    resetFilters() {
        document.getElementById('searchInput').value = '';
        document.getElementById('categoryFilter').value = '';
        document.getElementById('stockFilter').value = '';
        this.renderTable();
    }

	resetForm() {
	    const form = document.getElementById('produitForm');
	    form.reset();
	    form.action = '/api/produits';
	    delete form.dataset.editId; // ✅ Important : supprimer l'ID d'édition
	    
	    document.querySelector('#addProduitModal .modal-title').textContent = 'Nouveau Produit';
	    document.querySelector('#addProduitModal .btn-primary').textContent = 'Créer le produit';
	}

    // Utilitaires
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    showAlert(message, type) {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        const container = document.getElementById('alertContainer');
        if (container) {
            container.appendChild(alertDiv);

            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.remove();
                }
            }, 5000);
        }
    }
}

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    window.produitManager = new ProduitManager();
});