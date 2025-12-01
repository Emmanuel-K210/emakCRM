class PaiementManager {
    constructor() {
        this.paiements = [];
        this.factures = [];
        this.dataTable = null;
        this.init();
    }

    async init() {
        await this.loadFacturesImpayees();
        this.initializeDataTable();
        await this.loadPaiements();
        this.setupEventListeners();
    }

    initializeDataTable() {
        this.dataTable = $('#paiementsTable').DataTable({
            language: {
                url: '//cdn.datatables.net/plug-ins/1.13.6/i18n/fr-FR.json'
            },
            pageLength: 25,
            order: [[3, 'desc']],
            columns: [
                { title: "Référence" },
                { title: "Facture" },
                { title: "Client" },
                { title: "Date" },
                { title: "Montant" },
                { title: "Mode" },
                { title: "Statut" },
                { title: "Actions", orderable: false }
            ]
        });
    }

    async loadPaiements() {
        try {
            const response = await fetch('/api/paiements');
            if (!response.ok) throw new Error('Erreur chargement paiements');
            
            const data = await response.json();
            this.paiements = data.content;
            this.renderTable();
            this.updateStats();
            
        } catch (error) {
            console.error('Erreur:', error);
            this.showAlert('Erreur lors du chargement des paiements', 'danger');
        }
    }

    async loadFacturesImpayees() {
        try {
            const response = await fetch('/api/factures?statut=EMISE');
            if (response.ok) {
                const data = await response.json();
                this.factures = data.content.filter(f => f.montantRestant > 0);
                this.populateFactureSelect();
            }
        } catch (error) {
            console.error('Erreur chargement factures:', error);
        }
    }

    renderTable() {
        this.dataTable.clear();
        
        this.paiements.forEach(paiement => {
            const actions = this.getActionButtons(paiement);
            
            this.dataTable.row.add([
                paiement.referenceTransaction || `<span class="text-muted">N/A</span>`,
                `<strong>${paiement.numeroFacture}</strong>`,
                paiement.nomClient || 'N/A',
                new Date(paiement.datePaiement).toLocaleDateString('fr-FR'),
                `<span class="montant fw-bold">${paiement.montant ? paiement.montant.toFixed(2) + ' €' : '0.00 €'}</span>`,
                this.formatModePaiement(paiement.modePaiement),
                `<span class="badge statut-${paiement.statut.toLowerCase()}">${this.formatStatut(paiement.statut)}</span>`,
                actions
            ]);
        });
        
        this.dataTable.draw();
        this.attachEventListeners();
    }

    getActionButtons(paiement) {
        let buttons = `
            <div class="btn-group btn-group-sm">
                <button class="btn btn-outline-primary view-paiement" data-id="${paiement.id}" title="Voir">
                    <i class="ti ti-eye"></i>
                </button>
        `;

        if (paiement.statut === 'EN_ATTENTE') {
            buttons += `
                <button class="btn btn-outline-success valider-paiement" data-id="${paiement.id}" title="Valider">
                    <i class="ti ti-check"></i>
                </button>
                <button class="btn btn-outline-danger refuser-paiement" data-id="${paiement.id}" title="Refuser">
                    <i class="ti ti-x"></i>
                </button>
            `;
        }

        if (paiement.statut === 'VALIDE') {
            buttons += `
                <button class="btn btn-outline-info edit-paiement" data-id="${paiement.id}" title="Modifier">
                    <i class="ti ti-edit"></i>
                </button>
            `;
        }

        buttons += `
                <button class="btn btn-outline-danger delete-paiement" data-id="${paiement.id}" title="Supprimer">
                    <i class="ti ti-trash"></i>
                </button>
            </div>
        `;

        return buttons;
    }

    formatModePaiement(mode) {
        const modes = {
            'CARTE': 'Carte',
            'VIREMENT': 'Virement',
            'CHEQUE': 'Chèque',
            'ESPECES': 'Espèces',
            'PRELEVEMENT': 'Prélèvement'
        };
        return modes[mode] || mode;
    }

    formatStatut(statut) {
        const statuts = {
            'VALIDE': 'Validé',
            'EN_ATTENTE': 'En attente',
            'REFUSE': 'Refusé'
        };
        return statuts[statut] || statut;
    }

    updateStats() {
        const valides = this.paiements.filter(p => p.statut === 'VALIDE').length;
        const enAttente = this.paiements.filter(p => p.statut === 'EN_ATTENTE').length;
        const refuses = this.paiements.filter(p => p.statut === 'REFUSE').length;
        const totalEncaisse = this.paiements
            .filter(p => p.statut === 'VALIDE')
            .reduce((total, paiement) => total + (paiement.montant || 0), 0);
        
        document.getElementById('countValides').textContent = valides;
        document.getElementById('countAttente').textContent = enAttente;
        document.getElementById('countRefuses').textContent = refuses;
        document.getElementById('totalEncaisse').textContent = totalEncaisse.toFixed(2) + ' €';
    }

    // === MÉTHODES DE GESTION DES FORMULAIRES ===

    setupEventListeners() {
        // Formulaire paiement
        document.getElementById('paiementForm').addEventListener('submit', (e) => {
            this.handleFormSubmit(e);
        });

        // Auto-remplissage du montant restant
        document.querySelector('select[name="idFacture"]').addEventListener('change', (e) => {
            this.onFactureSelectChange(e.target);
        });

        // Filtres
        document.getElementById('applyFilters').addEventListener('click', () => this.applyFilters());
        document.getElementById('resetFilters').addEventListener('click', () => this.resetFilters());

        // Initialisation modal
        document.getElementById('paiementModal').addEventListener('show.bs.modal', () => {
            this.resetForm();
            this.setDefaultDate();
        });
    }

    populateFactureSelect() {
        const select = document.querySelector('select[name="idFacture"]');
        select.innerHTML = '<option value="">Choisir une facture...</option>';
        
        this.factures.forEach(facture => {
            const option = document.createElement('option');
            option.value = facture.idFacture;
            option.textContent = `${facture.numeroFacture} - ${facture.nomClient} (Reste: ${facture.montantRestant.toFixed(2)} €)`;
            option.dataset.montantRestant = facture.montantRestant;
            select.appendChild(option);
        });
    }

    onFactureSelectChange(select) {
        const selectedOption = select.options[select.selectedIndex];
        if (selectedOption.dataset.montantRestant) {
            const montantRestant = parseFloat(selectedOption.dataset.montantRestant);
            const montantInput = document.querySelector('input[name="montant"]');
            montantInput.value = montantRestant.toFixed(2);
            montantInput.max = montantRestant;
        }
    }

    setDefaultDate() {
        const today = new Date().toISOString().split('T')[0];
        document.querySelector('input[name="datePaiement"]').value = today;
    }

    async handleFormSubmit(e) {
        e.preventDefault();
        
        const form = e.target;
        const formData = new FormData(form);
        
        const paiementData = {
            datePaiement: formData.get('datePaiement'),
            montant: parseFloat(formData.get('montant')),
            modePaiement: formData.get('modePaiement'),
            libelle: formData.get('libelle'),
            referenceTransaction: formData.get('referenceTransaction'),
            idFacture: parseInt(formData.get('idFacture')),
            statut: 'VALIDE' // Par défaut validé
        };

        // Validation
        if (!this.validatePaiement(paiementData)) {
            return;
        }
        
        try {
            const response = await fetch('/api/paiements', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(paiementData)
            });
            
            if (response.ok) {
                const result = await response.json();
                this.showAlert('Paiement enregistré avec succès', 'success');
                bootstrap.Modal.getInstance(document.getElementById('paiementModal')).hide();
                await this.loadPaiements();
                await this.loadFacturesImpayees(); // Recharger les factures
                this.resetForm();
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            this.showAlert('Erreur: ' + error.message, 'danger');
        }
    }

    validatePaiement(paiementData) {
        if (paiementData.montant <= 0) {
            this.showAlert('Le montant doit être positif', 'warning');
            return false;
        }

        const factureSelectionnee = this.factures.find(f => f.idFacture === paiementData.idFacture);
        if (factureSelectionnee && paiementData.montant > factureSelectionnee.montantRestant) {
            this.showAlert(`Le montant ne peut pas dépasser le reste à payer (${factureSelectionnee.montantRestant.toFixed(2)} €)`, 'warning');
            return false;
        }

        if (!paiementData.libelle || paiementData.libelle.trim() === '') {
            this.showAlert('Le libellé est obligatoire', 'warning');
            return false;
        }

        return true;
    }

    // === MÉTHODES D'ACTIONS SUR LES PAIEMENTS ===

    attachEventListeners() {
        $('#paiementsTable').on('click', '.view-paiement', (e) => {
            const paiementId = $(e.currentTarget).data('id');
            this.viewPaiement(paiementId);
        });
        
        $('#paiementsTable').on('click', '.valider-paiement', (e) => {
            const paiementId = $(e.currentTarget).data('id');
            this.validerPaiement(paiementId);
        });
        
        $('#paiementsTable').on('click', '.refuser-paiement', (e) => {
            const paiementId = $(e.currentTarget).data('id');
            this.refuserPaiement(paiementId);
        });
        
        $('#paiementsTable').on('click', '.edit-paiement', (e) => {
            const paiementId = $(e.currentTarget).data('id');
            this.editPaiement(paiementId);
        });
        
        $('#paiementsTable').on('click', '.delete-paiement', (e) => {
            const paiementId = $(e.currentTarget).data('id');
            this.deletePaiement(paiementId);
        });
    }

    async viewPaiement(paiementId) {
        try {
            const response = await fetch(`/api/paiements/${paiementId}`);
            if (response.ok) {
                const paiement = await response.json();
                this.showPaiementDetail(paiement);
            }
        } catch (error) {
            this.showAlert('Erreur lors du chargement du paiement', 'danger');
        }
    }

    async validerPaiement(paiementId) {
        if (!confirm('Valider ce paiement ?')) return;
        
        try {
            const paiement = await this.getPaiement(paiementId);
            const updateData = {
                ...paiement,
                statut: 'VALIDE'
            };

            const response = await fetch(`/api/paiements/${paiementId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(updateData)
            });
            
            if (response.ok) {
                this.showAlert('Paiement validé avec succès', 'success');
                await this.loadPaiements();
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            this.showAlert('Erreur: ' + error.message, 'danger');
        }
    }

    async refuserPaiement(paiementId) {
        const raison = prompt('Veuillez saisir la raison du refus:');
        if (!raison) return;
        
        try {
            const paiement = await this.getPaiement(paiementId);
            const updateData = {
                ...paiement,
                statut: 'REFUSE',
                libelle: `${paiement.libelle} - REFUSÉ: ${raison}`
            };

            const response = await fetch(`/api/paiements/${paiementId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(updateData)
            });
            
            if (response.ok) {
                this.showAlert('Paiement refusé', 'success');
                await this.loadPaiements();
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            this.showAlert('Erreur: ' + error.message, 'danger');
        }
    }

    async editPaiement(paiementId) {
        try {
            const paiement = await this.getPaiement(paiementId);
            this.showEditModal(paiement);
        } catch (error) {
            this.showAlert('Erreur lors du chargement du paiement', 'danger');
        }
    }

    async deletePaiement(paiementId) {
        if (!confirm('Supprimer ce paiement ? Cette action est irréversible.')) return;
        
        try {
            const response = await fetch(`/api/paiements/${paiementId}`, {
                method: 'DELETE'
            });
            
            if (response.ok) {
                this.showAlert('Paiement supprimé avec succès', 'success');
                await this.loadPaiements();
                await this.loadFacturesImpayees(); // Recharger les factures
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            this.showAlert('Erreur: ' + error.message, 'danger');
        }
    }

    async getPaiement(paiementId) {
        const response = await fetch(`/api/paiements/${paiementId}`);
        if (!response.ok) throw new Error('Paiement non trouvé');
        return await response.json();
    }

    showPaiementDetail(paiement) {
        const detailHtml = `
            <div class="card">
                <div class="card-header">
                    <h5 class="card-title mb-0">Détail du Paiement</h5>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <p><strong>Facture:</strong> ${paiement.numeroFacture}</p>
                            <p><strong>Client:</strong> ${paiement.nomClient}</p>
                            <p><strong>Date:</strong> ${new Date(paiement.datePaiement).toLocaleDateString('fr-FR')}</p>
                            <p><strong>Montant:</strong> ${paiement.montant.toFixed(2)} €</p>
                        </div>
                        <div class="col-md-6">
                            <p><strong>Mode:</strong> ${this.formatModePaiement(paiement.modePaiement)}</p>
                            <p><strong>Statut:</strong> <span class="badge statut-${paiement.statut.toLowerCase()}">${this.formatStatut(paiement.statut)}</span></p>
                            <p><strong>Référence:</strong> ${paiement.referenceTransaction || 'N/A'}</p>
                            <p><strong>Libellé:</strong> ${paiement.libelle}</p>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Afficher dans un modal ou remplacer le contenu
        const modal = new bootstrap.Modal(document.getElementById('paiementModal'));
        document.querySelector('#paiementModal .modal-body').innerHTML = detailHtml;
        document.querySelector('#paiementModal .modal-footer').style.display = 'none';
        modal.show();
    }

    showEditModal(paiement) {
        const form = document.getElementById('paiementForm');
        form.querySelector('select[name="idFacture"]').value = paiement.idFacture;
        form.querySelector('input[name="datePaiement"]').value = paiement.datePaiement;
        form.querySelector('input[name="montant"]').value = paiement.montant;
        form.querySelector('select[name="modePaiement"]').value = paiement.modePaiement;
        form.querySelector('input[name="referenceTransaction"]').value = paiement.referenceTransaction || '';
        form.querySelector('input[name="libelle"]').value = paiement.libelle;
        
        form.dataset.editId = paiement.id;
        
        const modal = new bootstrap.Modal(document.getElementById('paiementModal'));
        document.querySelector('#paiementModal .modal-title').textContent = 'Modifier le Paiement';
        document.querySelector('#paiementModal .btn-primary').textContent = 'Mettre à jour';
        modal.show();
    }

    // === MÉTHODES DE FILTRES ===

    applyFilters() {
        const statutFilter = document.getElementById('statutFilter').value;
        const modeFilter = document.getElementById('modeFilter').value;
        const dateFrom = document.getElementById('dateFromFilter').value;
        const dateTo = document.getElementById('dateToFilter').value;
        
        let filteredPaiements = this.paiements;
        
        if (statutFilter) {
            filteredPaiements = filteredPaiements.filter(p => p.statut === statutFilter);
        }
        
        if (modeFilter) {
            filteredPaiements = filteredPaiements.filter(p => p.modePaiement === modeFilter);
        }
        
        if (dateFrom) {
            filteredPaiements = filteredPaiements.filter(p => new Date(p.datePaiement) >= new Date(dateFrom));
        }
        
        if (dateTo) {
            filteredPaiements = filteredPaiements.filter(p => new Date(p.datePaiement) <= new Date(dateTo));
        }
        
        this.renderFilteredTable(filteredPaiements);
    }

    renderFilteredTable(filteredPaiements) {
        this.dataTable.clear();
        
        filteredPaiements.forEach(paiement => {
            const actions = this.getActionButtons(paiement);
            
            this.dataTable.row.add([
                paiement.referenceTransaction || `<span class="text-muted">N/A</span>`,
                `<strong>${paiement.numeroFacture}</strong>`,
                paiement.nomClient || 'N/A',
                new Date(paiement.datePaiement).toLocaleDateString('fr-FR'),
                `<span class="montant fw-bold">${paiement.montant ? paiement.montant.toFixed(2) + ' €' : '0.00 €'}</span>`,
                this.formatModePaiement(paiement.modePaiement),
                `<span class="badge statut-${paiement.statut.toLowerCase()}">${this.formatStatut(paiement.statut)}</span>`,
                actions
            ]);
        });
        
        this.dataTable.draw();
    }

    resetFilters() {
        document.getElementById('statutFilter').value = '';
        document.getElementById('modeFilter').value = '';
        document.getElementById('dateFromFilter').value = '';
        document.getElementById('dateToFilter').value = '';
        this.renderTable();
    }

    resetForm() {
        document.getElementById('paiementForm').reset();
        delete document.getElementById('paiementForm').dataset.editId;
        document.querySelector('#paiementModal .modal-title').textContent = 'Nouveau Paiement';
        document.querySelector('#paiementModal .btn-primary').textContent = 'Enregistrer';
        document.querySelector('#paiementModal .modal-footer').style.display = 'flex';
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
    window.paiementManager = new PaiementManager();
});