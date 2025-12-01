class FactureManager {
    constructor() {
        this.factures = [];
        this.clients = [];
        this.produits = [];
        this.devis = [];
        this.dataTable = null;
        this.init();
    }

    async init() {
        await this.loadClients();
        await this.loadProduits();
        await this.loadDevisConvertibles();
        this.initializeDataTable();
        await this.loadFactures();
        this.setupEventListeners();
    }

    initializeDataTable() {
        this.dataTable = $('#facturesTable').DataTable({
            language: {
                url: '//cdn.datatables.net/plug-ins/1.13.6/i18n/fr-FR.json'
            },
            pageLength: 25,
            order: [[2, 'desc']],
            columns: [
                { title: "N° Facture" },
                { title: "Client" },
                { title: "Date" },
                { title: "Échéance" },
                { title: "Montant TTC" },
                { title: "Reste à payer" },
                { title: "Statut" },
                { title: "Actions", orderable: false }
            ]
        });
    }

    async loadFactures() {
        try {
            const response = await fetch('/api/factures');
            if (!response.ok) throw new Error('Erreur chargement factures');
            
            const data = await response.json();
            this.factures = data.content;
            this.renderTable();
            this.updateStats();
            
        } catch (error) {
            console.error('Erreur:', error);
            this.showAlert('Erreur lors du chargement des factures', 'danger');
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

    async loadDevisConvertibles() {
        try {
            const response = await fetch('/api/devis/convertibles');
            if (response.ok) {
                this.devis = await response.json();
                this.populateDevisSelect();
            }
        } catch (error) {
            console.error('Erreur chargement devis:', error);
        }
    }

    renderTable() {
        this.dataTable.clear();
        
        this.factures.forEach(facture => {
            const actions = this.getActionButtons(facture);
            const isEnRetard = this.isFactureEnRetard(facture);
            
            this.dataTable.row.add([
                `<strong>${facture.numeroFacture}</strong>`,
                facture.nomClient || 'N/A',
                new Date(facture.dateFacture).toLocaleDateString('fr-FR'),
                facture.dateEcheance ? new Date(facture.dateEcheance).toLocaleDateString('fr-FR') : 'N/A',
                `<span class="montant">${facture.montantTtc ? facture.montantTtc.toFixed(2) + ' €' : '0.00 €'}</span>`,
                `<span class="${facture.montantRestant > 0 ? 'text-danger' : 'text-success'}">${facture.montantRestant ? facture.montantRestant.toFixed(2) + ' €' : '0.00 €'}</span>`,
                `<span class="badge statut-${facture.statut.toLowerCase()} ${isEnRetard ? 'bg-danger' : ''}">${this.formatStatut(facture.statut)}${isEnRetard ? ' ⚠️' : ''}</span>`,
                actions
            ]);
        });
        
        this.dataTable.draw();
        this.attachEventListeners();
    }

    getActionButtons(facture) {
        let buttons = `
            <div class="btn-group btn-group-sm">
                <button class="btn btn-outline-primary view-facture" data-id="${facture.idFacture}" title="Voir">
                    <i class="ti ti-eye"></i>
                </button>
        `;

        if (facture.statut === 'BROUILLON') {
            buttons += `
                <button class="btn btn-outline-warning edit-facture" data-id="${facture.idFacture}" title="Modifier">
                    <i class="ti ti-edit"></i>
                </button>
                <button class="btn btn-outline-success send-facture" data-id="${facture.idFacture}" title="Envoyer">
                    <i class="ti ti-send"></i>
                </button>
            `;
        }

        if (facture.montantRestant > 0 && facture.statut !== 'ANNULEE') {
            buttons += `
                <button class="btn btn-outline-info add-paiement" data-id="${facture.idFacture}" title="Ajouter paiement">
                    <i class="ti ti-credit-card"></i>
                </button>
            `;
        }

        if (facture.statut === 'BROUILLON' || facture.statut === 'EMISE') {
            buttons += `
                <button class="btn btn-outline-danger cancel-facture" data-id="${facture.idFacture}" title="Annuler">
                    <i class="ti ti-x"></i>
                </button>
            `;
        }

        buttons += `</div>`;
        return buttons;
    }

    isFactureEnRetard(facture) {
        if (!facture.dateEcheance || facture.statut === 'PAYEE' || facture.statut === 'ANNULEE') {
            return false;
        }
        return new Date(facture.dateEcheance) < new Date() && facture.montantRestant > 0;
    }

    formatStatut(statut) {
        const statuts = {
            'BROUILLON': 'Brouillon',
            'EMISE': 'Emise',
            'EN_RETARD': 'En retard',
            'PAYEE_PARTIELLEMENT': 'Payée partiellement',
            'PAYEE': 'Payée',
            'ANNULEE': 'Annulée'
        };
        return statuts[statut] || statut;
    }

    updateStats() {
        const emises = this.factures.filter(f => f.statut === 'EMISE').length;
        const enRetard = this.factures.filter(f => this.isFactureEnRetard(f)).length;
        const payees = this.factures.filter(f => f.statut === 'PAYEE').length;
        const totalEncaissements = this.factures
            .filter(f => f.statut === 'PAYEE')
            .reduce((total, facture) => total + (facture.montantTtc || 0), 0);
        
        document.getElementById('countEmises').textContent = emises;
        document.getElementById('countRetard').textContent = enRetard;
        document.getElementById('countPayees').textContent = payees;
        document.getElementById('totalEncaissements').textContent = totalEncaissements.toFixed(2) + ' €';
    }

    // === MÉTHODES DE GESTION DES FORMULAIRES ===

    setupEventListeners() {
        // Formulaire facture
        document.getElementById('factureForm').addEventListener('submit', (e) => {
            this.handleFormSubmit(e);
        });

        // Ajout de lignes
        document.getElementById('addLigne').addEventListener('click', () => {
            this.addLigneFacture();
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
        document.getElementById('applyFilters').addEventListener('click', () => this.applyFilters());
        document.getElementById('resetFilters').addEventListener('click', () => this.resetFilters());

        // Initialisation modal
        document.getElementById('factureModal').addEventListener('show.bs.modal', () => {
            this.resetForm();
            this.addLigneFacture(); // Ajouter une ligne par défaut
        });
    }

    addLigneFacture() {
        const template = document.getElementById('ligneTemplate');
        const clone = template.content.cloneNode(true);
        const ligneContainer = document.getElementById('lignesFacture');
        
        ligneContainer.appendChild(clone);
        this.populateProduitSelect(ligneContainer.lastElementChild);
        this.attachLigneEventListeners(ligneContainer.lastElementChild);
        this.calculerTotaux();
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

    populateDevisSelect() {
        const select = document.querySelector('select[name="idDevis"]');
        select.innerHTML = '<option value="">Choisir un devis...</option>';
        
        this.devis.forEach(devis => {
            const option = document.createElement('option');
            option.value = devis.idDevis;
            option.textContent = `${devis.numeroDevis} - ${devis.nomClient} (${devis.montantTtc} €)`;
            select.appendChild(option);
        });
    }

    onProduitSelectChange(select) {
        const selectedOption = select.options[select.selectedIndex];
        if (selectedOption.dataset.prix) {
            const ligne = select.closest('.ligne-facture');
            ligne.querySelector('.prix-unitaire').value = selectedOption.dataset.prix;
            this.calculerTotaux();
        }
    }

    calculerTotaux() {
        let totalHT = 0;
        let totalTVA = 0;
        const lignes = document.querySelectorAll('.ligne-facture');
        
        lignes.forEach(ligne => {
            const quantite = parseFloat(ligne.querySelector('.quantite').value) || 0;
            const prixUnitaire = parseFloat(ligne.querySelector('.prix-unitaire').value) || 0;
            const tauxTVA = parseFloat(ligne.querySelector('.taux-tva').value) || 20;
            
            const montantLigneHT = quantite * prixUnitaire;
            const montantLigneTVA = montantLigneHT * (tauxTVA / 100);
            
            totalHT += montantLigneHT;
            totalTVA += montantLigneTVA;
        });
        
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
            this.showAlert('La facture doit contenir au moins une ligne', 'warning');
            return;
        }
        
        // Calculer les totaux finaux
        this.calculerTotaux();
        
        const factureData = {
            idClient: parseInt(formData.get('idClient')),
            idDevis: formData.get('idDevis') ? parseInt(formData.get('idDevis')) : null,
            dateFacture: formData.get('dateFacture') || new Date().toISOString().split('T')[0],
            dateEcheance: formData.get('dateEcheance') || null,
            tauxTva: parseFloat(formData.get('tauxTva')) || 20.0,
            notes: formData.get('notes') || '',
            montantHt: this.calculerTotalHT(),
            lignes: lignes
        };
        
        try {
            const response = await fetch('/api/factures', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(factureData)
            });
            
            if (response.ok) {
                const result = await response.json();
                this.showAlert('Facture créée avec succès', 'success');
                bootstrap.Modal.getInstance(document.getElementById('factureModal')).hide();
                await this.loadFactures();
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
        const ligneElements = document.querySelectorAll('.ligne-facture');
        
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

    calculerTotalHT() {
        let totalHT = 0;
        const lignes = document.querySelectorAll('.ligne-facture');
        
        lignes.forEach(ligne => {
            const quantite = parseFloat(ligne.querySelector('.quantite').value) || 0;
            const prixUnitaire = parseFloat(ligne.querySelector('.prix-unitaire').value) || 0;
            totalHT += quantite * prixUnitaire;
        });
        
        return totalHT;
    }

    // === MÉTHODES D'ACTIONS SUR LES FACTURES ===

    attachEventListeners() {
        $('#facturesTable').on('click', '.view-facture', (e) => {
            const factureId = $(e.currentTarget).data('id');
            this.viewFacture(factureId);
        });
        
        $('#facturesTable').on('click', '.edit-facture', (e) => {
            const factureId = $(e.currentTarget).data('id');
            this.editFacture(factureId);
        });
        
        $('#facturesTable').on('click', '.send-facture', (e) => {
            const factureId = $(e.currentTarget).data('id');
            this.sendFacture(factureId);
        });
        
        $('#facturesTable').on('click', '.add-paiement', (e) => {
            const factureId = $(e.currentTarget).data('id');
            this.addPaiement(factureId);
        });
        
        $('#facturesTable').on('click', '.cancel-facture', (e) => {
            const factureId = $(e.currentTarget).data('id');
            this.cancelFacture(factureId);
        });
    }

    async viewFacture(factureId) {
        window.location.href = `/factures/${factureId}`;
    }

    async editFacture(factureId) {
        try {
            const response = await fetch(`/api/factures/${factureId}`);
            if (response.ok) {
                const facture = await response.json();
                this.showEditModal(facture);
            }
        } catch (error) {
            this.showAlert('Erreur lors du chargement de la facture', 'danger');
        }
    }

    async sendFacture(factureId) {
        if (!confirm('Envoyer cette facture au client ?')) return;
        
        try {
            // Implémenter l'envoi de facture
            this.showAlert('Envoi de facture à implémenter', 'info');
        } catch (error) {
            this.showAlert('Erreur: ' + error.message, 'danger');
        }
    }

    async addPaiement(factureId) {
        // Ouvrir modal de paiement
        const modal = new bootstrap.Modal(document.getElementById('paiementModal'));
        document.getElementById('factureIdPaiement').value = factureId;
        modal.show();
    }

    async cancelFacture(factureId) {
        if (!confirm('Annuler cette facture ? Cette action est irréversible.')) return;
        
        const raison = prompt('Veuillez saisir la raison de l\'annulation:');
        if (!raison) return;
        
        try {
            const response = await fetch(`/api/factures/${factureId}/annuler?raison=${encodeURIComponent(raison)}`, {
                method: 'PUT'
            });
            
            if (response.ok) {
                this.showAlert('Facture annulée avec succès', 'success');
                await this.loadFactures();
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            this.showAlert('Erreur: ' + error.message, 'danger');
        }
    }

    // === MÉTHODES DE FILTRES ===

    applyFilters() {
        const statutFilter = document.getElementById('statutFilter').value;
        const dateFrom = document.getElementById('dateFromFilter').value;
        const dateTo = document.getElementById('dateToFilter').value;
        
        let filteredFactures = this.factures;
        
        if (statutFilter) {
            filteredFactures = filteredFactures.filter(f => f.statut === statutFilter);
        }
        
        if (dateFrom) {
            filteredFactures = filteredFactures.filter(f => new Date(f.dateFacture) >= new Date(dateFrom));
        }
        
        if (dateTo) {
            filteredFactures = filteredFactures.filter(f => new Date(f.dateFacture) <= new Date(dateTo));
        }
        
        this.renderFilteredTable(filteredFactures);
    }

    renderFilteredTable(filteredFactures) {
        this.dataTable.clear();
        
        filteredFactures.forEach(facture => {
            const actions = this.getActionButtons(facture);
            const isEnRetard = this.isFactureEnRetard(facture);
            
            this.dataTable.row.add([
                `<strong>${facture.numeroFacture}</strong>`,
                facture.nomClient || 'N/A',
                new Date(facture.dateFacture).toLocaleDateString('fr-FR'),
                facture.dateEcheance ? new Date(facture.dateEcheance).toLocaleDateString('fr-FR') : 'N/A',
                `<span class="montant">${facture.montantTtc ? facture.montantTtc.toFixed(2) + ' €' : '0.00 €'}</span>`,
                `<span class="${facture.montantRestant > 0 ? 'text-danger' : 'text-success'}">${facture.montantRestant ? facture.montantRestant.toFixed(2) + ' €' : '0.00 €'}</span>`,
                `<span class="badge statut-${facture.statut.toLowerCase()} ${isEnRetard ? 'bg-danger' : ''}">${this.formatStatut(facture.statut)}${isEnRetard ? ' ⚠️' : ''}</span>`,
                actions
            ]);
        });
        
        this.dataTable.draw();
    }

    resetFilters() {
        document.getElementById('statutFilter').value = '';
        document.getElementById('dateFromFilter').value = '';
        document.getElementById('dateToFilter').value = '';
        this.renderTable();
    }

    resetForm() {
        document.getElementById('factureForm').reset();
        document.getElementById('lignesFacture').innerHTML = '';
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
}

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    window.factureManager = new FactureManager();
});