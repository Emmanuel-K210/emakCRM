class DevisManager {
    constructor() {
        this.devis = [];
        this.clients = [];
        this.produits = [];
        this.dataTable = null;
        this.init();
    }

    async init() {
        await this.loadClients();
        await this.loadProduits();
        this.initializeDataTable();
        await this.loadDevis();
        this.setupEventListeners();
    }

    initializeDataTable() {
        this.dataTable = $('#devisTable').DataTable({
            language: {
                url: '//cdn.datatables.net/plug-ins/1.13.6/i18n/fr-FR.json'
            },
            pageLength: 25,
            order: [[2, 'desc']],
            columns: [
                { title: "N° Devis" },
                { title: "Client" },
                { title: "Date" },
                { title: "Montant HT" },
                { title: "Montant TTC" },
                { title: "Statut" },
                { title: "Validité" },
                { title: "Actions", orderable: false }
            ]
        });
    }

    async loadDevis() {
        try {
            const response = await fetch('/api/devis/api');
            if (!response.ok) throw new Error('Erreur chargement devis');
            
            const data = await response.json();
            this.devis = data.content;
            this.renderTable();
            this.updateStats();
            
        } catch (error) {
            console.error('Erreur:', error);
            this.showAlert('Erreur lors du chargement des devis', 'danger');
        }
    }

    async loadClients() {
        try {
            const response = await fetch('/api/clients/all');
            if (response.ok) {
                this.clients = await response.json();
                this.populateClientSelect();
            }
        } catch (error) {
            console.error('Erreur chargement clients:', error);
        }
    }

    async loadProduits() {
        try {
            const response = await fetch('/api/produits/all');
            if (response.ok) {
                this.produits = await response.json();
            }
        } catch (error) {
            console.error('Erreur chargement produits:', error);
        }
    }

    renderTable() {
        this.dataTable.clear();
        
        this.devis.forEach(devis => {
            const actions = this.getActionButtons(devis);
            
            this.dataTable.row.add([
                `<strong>${devis.numeroDevis}</strong>`,
                devis.nomClient || 'N/A',
                new Date(devis.dateEmission).toLocaleDateString('fr-FR'),
                `${devis.montantHt ? devis.montantHt.toFixed(2) + ' €' : '0.00 €'}`,
                `<span class="montant">${devis.montantTtc ? devis.montantTtc.toFixed(2) + ' €' : '0.00 €'}</span>`,
                `<span class="badge statut-${devis.statut.toLowerCase()}">${this.formatStatut(devis.statut)}</span>`,
                devis.dateValidite ? new Date(devis.dateValidite).toLocaleDateString('fr-FR') : 'N/A',
                actions
            ]);
        });
        
        this.dataTable.draw();
        this.attachEventListeners();
    }

    getActionButtons(devis) {
        let buttons = `
            <div class="btn-group btn-group-sm">
                <button class="btn btn-outline-primary view-devis" data-id="${devis.idDevis}" title="Voir">
                    <i class="ti ti-eye"></i>
                </button>
        `;

        if (devis.statut === 'BROUILLON' || devis.statut === 'MODIFIE') {
            buttons += `
                <button class="btn btn-outline-warning edit-devis" data-id="${devis.idDevis}" title="Modifier">
                    <i class="ti ti-edit"></i>
                </button>
                <button class="btn btn-outline-success send-devis" data-id="${devis.idDevis}" title="Envoyer">
                    <i class="ti ti-send"></i>
                </button>
            `;
        }

        if (devis.statut === 'ACCEPTE' && !devis.dejaFacture) {
            buttons += `
                <button class="btn btn-outline-info convert-devis" data-id="${devis.idDevis}" title="Convertir en facture">
                    <i class="ti ti-file-invoice"></i>
                </button>
            `;
        }

        buttons += `
                <button class="btn btn-outline-danger delete-devis" data-id="${devis.idDevis}" title="Supprimer">
                    <i class="ti ti-trash"></i>
                </button>
            </div>
        `;

        return buttons;
    }

	// Dans DevisManager
	setupEventListeners() {
	    // Formulaire devis
	    document.getElementById('devisForm').addEventListener('submit', (e) => {
	        this.handleFormSubmit(e);
	    });

	    // Ajout de lignes
	    document.getElementById('addLigne').addEventListener('click', () => {
	        this.addLigneDevis();
	    });

	    // Calcul automatique des totaux
	    document.addEventListener('input', (e) => {
	        if (e.target.classList.contains('quantite') || 
	            e.target.classList.contains('prix-unitaire') ||
	            e.target.classList.contains('taux-tva')) {
	            this.calculerTotaux();
	        }
	    });

	    // Chargement produit
	    document.addEventListener('change', (e) => {
	        if (e.target.classList.contains('produit-select')) {
	            this.onProduitSelectChange(e.target);
	        }
	    });

	    // Filtres
	    document.getElementById('statutFilter').addEventListener('change', () => this.applyFilters());
	    document.getElementById('resetFilters').addEventListener('click', () => this.resetFilters());
	}

	addLigneDevis() {
	    const template = document.getElementById('ligneTemplate');
	    const clone = template.content.cloneNode(true);
	    const ligneContainer = document.getElementById('lignesDevis');
	    
	    ligneContainer.appendChild(clone);
	    this.populateProduitSelect(ligneContainer.lastElementChild);
	    this.attachLigneEventListeners(ligneContainer.lastElementChild);
	}

	populateProduitSelect(ligneElement) {
	    const select = ligneElement.querySelector('.produit-select');
	    select.innerHTML = '<option value="">Choisir un produit...</option>';
	    
	    this.produits.forEach(produit => {
	        const option = document.createElement('option');
	        option.value = produit.id;
	        option.textContent = `${produit.referenceSku} - ${produit.nomProduit} (${produit.prixUnitaireHt} €)`;
	        option.dataset.prix = produit.prixUnitaireHt;
	        select.appendChild(option);
	    });
	}

	onProduitSelectChange(select) {
	    const selectedOption = select.options[select.selectedIndex];
	    if (selectedOption.dataset.prix) {
	        const ligne = select.closest('.ligne-devis');
	        ligne.querySelector('.prix-unitaire').value = selectedOption.dataset.prix;
	        this.calculerTotaux();
	    }
	}

	calculerTotaux() {
	    let totalHT = 0;
	    const lignes = document.querySelectorAll('.ligne-devis');
	    
	    lignes.forEach(ligne => {
	        const quantite = parseFloat(ligne.querySelector('.quantite').value) || 0;
	        const prixUnitaire = parseFloat(ligne.querySelector('.prix-unitaire').value) || 0;
	        const tauxTVA = parseFloat(ligne.querySelector('.taux-tva').value) || 20;
	        
	        const montantLigneHT = quantite * prixUnitaire;
	        totalHT += montantLigneHT;
	    });
	    
	    const totalTVA = totalHT * 0.20; // TVA 20% par défaut
	    const totalTTC = totalHT + totalTVA;
	    
	    document.getElementById('totalHT').textContent = totalHT.toFixed(2) + ' €';
	    document.getElementById('totalTVA').textContent = totalTVA.toFixed(2) + ' €';
	    document.getElementById('totalTTC').textContent = totalTTC.toFixed(2) + ' €';
	}

	attachLigneEventListeners(ligneElement) {
	    // Suppression ligne
	    ligneElement.querySelector('.remove-ligne').addEventListener('click', () => {
	        ligneElement.remove();
	        this.calculerTotaux();
	    });
	}
	
	async handleFormSubmit(e) {
	    e.preventDefault();
	    
	    const form = e.target;
	    const formData = new FormData(form);
	    
	    // Récupérer les lignes
	    const lignes = this.getLignesFromForm();
	    
	    if (lignes.length === 0) {
	        this.showAlert('Le devis doit contenir au moins une ligne', 'warning');
	        return;
	    }
	    
	    const devisData = {
	        idClient: parseInt(formData.get('idClient')),
	        dateValidite: formData.get('dateValidite') || null,
	        conditions: formData.get('conditions') || '',
	        lignes: lignes
	    };
	    
	    try {
	        const response = await fetch('/api/devis/api', {
	            method: 'POST',
	            headers: {
	                'Content-Type': 'application/json',
	            },
	            body: JSON.stringify(devisData)
	        });
	        
	        if (response.ok) {
	            const result = await response.json();
	            this.showAlert('Devis créé avec succès', 'success');
	            bootstrap.Modal.getInstance(document.getElementById('devisModal')).hide();
	            await this.loadDevis();
	            this.resetForm();
	        } else {
	            const error = await response.text();
	            throw new Error(error);
	        }
	    } catch (error) {
	        this.showAlert('Erreur: ' + error.message, 'danger');
	    }
	}

	getLignesFromForm() {
	    const lignes = [];
	    const ligneElements = document.querySelectorAll('.ligne-devis');
	    
	    ligneElements.forEach(ligne => {
	        const idProduit = ligne.querySelector('.produit-select').value;
	        const quantite = parseInt(ligne.querySelector('.quantite').value);
	        const prixUnitaireHt = parseFloat(ligne.querySelector('.prix-unitaire').value);
	        const description = ligne.querySelector('.description').value;
	        const tauxTva = parseFloat(ligne.querySelector('.taux-tva').value);
	        
	        if (idProduit && quantite && prixUnitaireHt) {
	            lignes.push({
	                idProduit: parseInt(idProduit),
	                quantite: quantite,
	                prixUnitaireHt: prixUnitaireHt,
	                description: description,
	                tauxTva: tauxTva
	            });
	        }
	    });
	    
	    return lignes;
	}
	
	// Dans attachEventListeners()
	attachEventListeners() {
	    $('#devisTable').on('click', '.view-devis', (e) => {
	        const devisId = $(e.currentTarget).data('id');
	        this.viewDevis(devisId);
	    });
	    
	    $('#devisTable').on('click', '.edit-devis', (e) => {
	        const devisId = $(e.currentTarget).data('id');
	        this.editDevis(devisId);
	    });
	    
	    $('#devisTable').on('click', '.send-devis', (e) => {
	        const devisId = $(e.currentTarget).data('id');
	        this.sendDevis(devisId);
	    });
	    
	    $('#devisTable').on('click', '.convert-devis', (e) => {
	        const devisId = $(e.currentTarget).data('id');
	        this.convertToFacture(devisId);
	    });
	    
	    $('#devisTable').on('click', '.delete-devis', (e) => {
	        const devisId = $(e.currentTarget).data('id');
	        this.deleteDevis(devisId);
	    });
	}

	async viewDevis(devisId) {
	    try {
	        const response = await fetch(`/api/devis/api/${devisId}`);
	        if (response.ok) {
	            const devis = await response.json();
	            this.showDevisDetail(devis);
	        }
	    } catch (error) {
	        this.showAlert('Erreur lors du chargement du devis', 'danger');
	    }
	}

	async sendDevis(devisId) {
	    if (!confirm('Envoyer ce devis au client ?')) return;
	    
	    try {
	        const response = await fetch(`/api/devis/api/${devisId}/envoyer`, {
	            method: 'POST'
	        });
	        
	        if (response.ok) {
	            this.showAlert('Devis envoyé avec succès', 'success');
	            await this.loadDevis();
	        } else {
	            const error = await response.text();
	            throw new Error(error);
	        }
	    } catch (error) {
	        this.showAlert('Erreur: ' + error.message, 'danger');
	    }
	}

	async convertToFacture(devisId) {
	    if (!confirm('Convertir ce devis en facture ?')) return;
	    
	    try {
	        const response = await fetch(`/api/devis/api/${devisId}/convertir-facture`, {
	            method: 'POST'
	        });
	        
	        if (response.ok) {
	            this.showAlert('Devis converti en facture avec succès', 'success');
	            await this.loadDevis();
	        } else {
	            const error = await response.text();
	            throw new Error(error);
	        }
	    } catch (error) {
	        this.showAlert('Erreur: ' + error.message, 'danger');
	    }
	}

	async deleteDevis(devisId) {
	    if (!confirm('Supprimer ce devis ?')) return;
	    
	    try {
	        const response = await fetch(`/api/devis/api/${devisId}`, {
	            method: 'DELETE'
	        });
	        
	        if (response.ok) {
	            this.showAlert('Devis supprimé avec succès', 'success');
	            await this.loadDevis();
	        } else {
	            const error = await response.text();
	            throw new Error(error);
	        }
	    } catch (error) {
	        this.showAlert('Erreur: ' + error.message, 'danger');
	    }
	}
	
	populateClientSelect() {
	    const select = document.querySelector('select[name="idClient"]');
	    select.innerHTML = '<option value="">Choisir un client...</option>';
	    
	    this.clients.forEach(client => {
	        const option = document.createElement('option');
	        option.value = client.id;
	        option.textContent = `${client.nom} - ${client.email || ''}`;
	        select.appendChild(option);
	    });
	}

	formatStatut(statut) {
	    const statuts = {
	        'BROUILLON': 'Brouillon',
	        'ENVOYE': 'Envoyé',
	        'ACCEPTE': 'Accepté',
	        'REFUSE': 'Refusé',
	        'CONVERTI_EN_FACTURE': 'Converti en facture'
	    };
	    return statuts[statut] || statut;
	}

	updateStats() {
	    const enAttente = this.devis.filter(d => d.statut === 'ENVOYE').length;
	    const acceptes = this.devis.filter(d => d.statut === 'ACCEPTE').length;
	    const chiffreAffaires = this.devis
	        .filter(d => d.statut === 'ACCEPTE')
	        .reduce((total, devis) => total + (devis.montantTtc || 0), 0);
	    
	    document.getElementById('countEnAttente').textContent = enAttente;
	    document.getElementById('countAcceptes').textContent = acceptes;
	    document.getElementById('chiffreAffaires').textContent = chiffreAffaires.toFixed(2) + ' €';
	}

	applyFilters() {
	    const statutFilter = document.getElementById('statutFilter').value;
	    const dateFrom = document.getElementById('dateFromFilter').value;
	    const dateTo = document.getElementById('dateToFilter').value;
	    
	    let filteredDevis = this.devis;
	    
	    if (statutFilter) {
	        filteredDevis = filteredDevis.filter(d => d.statut === statutFilter);
	    }
	    
	    if (dateFrom) {
	        filteredDevis = filteredDevis.filter(d => new Date(d.dateEmission) >= new Date(dateFrom));
	    }
	    
	    if (dateTo) {
	        filteredDevis = filteredDevis.filter(d => new Date(d.dateEmission) <= new Date(dateTo));
	    }
	    
	    this.renderFilteredTable(filteredDevis);
	}

	resetFilters() {
	    document.getElementById('statutFilter').value = '';
	    document.getElementById('dateFromFilter').value = '';
	    document.getElementById('dateToFilter').value = '';
	    this.renderTable();
	}

	resetForm() {
	    document.getElementById('devisForm').reset();
	    document.getElementById('lignesDevis').innerHTML = '';
	    document.getElementById('totalHT').textContent = '0.00 €';
	    document.getElementById('totalTVA').textContent = '0.00 €';
	    document.getElementById('totalTTC').textContent = '0.00 €';
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
            setTimeout(() => alertDiv.remove(), 5000);
        }
    }
	
	async editDevis(devisId) {
	    try {
	        const response = await fetch(`/api/devis/api/${devisId}`);
	        if (!response.ok) throw new Error('Devis non trouvé');
	        
	        const devis = await response.json();
	        this.showEditModal(devis);
	        
	    } catch (error) {
	        this.showAlert('Erreur lors du chargement du devis', 'danger');
	    }
	}

	showEditModal(devis) {
	    // Remplir le formulaire avec les données du devis
	    const form = document.getElementById('devisForm');
	    form.querySelector('select[name="idClient"]').value = devis.idClient;
	    form.querySelector('input[name="dateValidite"]').value = devis.dateValidite;
	    form.querySelector('textarea[name="conditions"]').value = devis.conditions || '';
	    
	    // Vider les lignes existantes
	    document.getElementById('lignesDevis').innerHTML = '';
	    
	    // Ajouter les lignes du devis
	    if (devis.lignes) {
	        devis.lignes.forEach(ligne => {
	            this.addLigneDevis();
	            const lastLigne = document.querySelector('.ligne-devis:last-child');
	            // Remplir les champs de la ligne...
	        });
	    }
	    
	    // Changer le modal pour l'édition
	    document.querySelector('#devisModal .modal-title').textContent = 'Modifier le Devis';
	    document.querySelector('#devisModal .btn-primary').textContent = 'Mettre à jour';
	    form.dataset.editId = devis.idDevis;
	    
	    // Ouvrir le modal
	    new bootstrap.Modal(document.getElementById('devisModal')).show();
	}
}

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    window.devisManager = new DevisManager();
});