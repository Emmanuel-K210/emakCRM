class FactureDetail {
    constructor() {
        this.facture = null;
        this.factureId = this.getFactureIdFromUrl();
        this.init();
    }

    getFactureIdFromUrl() {
        const path = window.location.pathname;
        const matches = path.match(/\/factures\/(\d+)/);
        return matches ? parseInt(matches[1]) : null;
    }

    async init() {
        if (!this.factureId) {
            this.showAlert('ID de facture non valide', 'danger');
            return;
        }

        await this.loadFactureDetail();
        this.setupEventListeners();
        this.updateUI();
    }

    async loadFactureDetail() {
        try {
            const response = await fetch(`/api/factures/${this.factureId}`);
            if (!response.ok) throw new Error('Facture non trouvée');
            
            this.facture = await response.json();
            this.renderFactureDetail();
            
        } catch (error) {
            console.error('Erreur:', error);
            this.showAlert('Erreur lors du chargement de la facture', 'danger');
        }
    }

    renderFactureDetail() {
        if (!this.facture) return;

        // En-tête de la facture
        document.getElementById('factureNumero').textContent = this.facture.numeroFacture;
        document.getElementById('factureClient').innerHTML = `
            <strong>Client:</strong> ${this.facture.nomClient || 'N/A'}<br>
            <strong>Email:</strong> ${this.facture.emailClient || 'N/A'}
        `;
        
        document.getElementById('factureDates').innerHTML = `
            <strong>Date facturation:</strong> ${new Date(this.facture.dateFacture).toLocaleDateString('fr-FR')}<br>
            <strong>Échéance:</strong> ${this.facture.dateEcheance ? new Date(this.facture.dateEcheance).toLocaleDateString('fr-FR') : 'N/A'}
        `;

        // Statut et totaux
        const isEnRetard = this.isFactureEnRetard();
        document.getElementById('factureStatut').innerHTML = `
            <span class="badge statut-${this.facture.statut.toLowerCase()} ${isEnRetard ? 'bg-danger' : ''}">
                ${this.formatStatut(this.facture.statut)}${isEnRetard ? ' ⚠️ En retard' : ''}
            </span>
        `;

        document.getElementById('factureTotaux').innerHTML = `
            <strong>Total TTC: ${this.facture.montantTtc ? this.facture.montantTtc.toFixed(2) + ' €' : '0.00 €'}</strong>
        `;

        document.getElementById('factureRestant').innerHTML = `
            <strong>Reste à payer: ${this.facture.montantRestant ? this.facture.montantRestant.toFixed(2) + ' €' : '0.00 €'}</strong>
        `;

        // Lignes de facture
        this.renderLignesFacture();

        // Notes
        document.getElementById('factureNotes').textContent = 
            this.facture.notes && this.facture.notes.trim() !== '' ? 
            this.facture.notes : 'Aucune note';

        // Paiements
        this.renderPaiements();

        // Devis associé
        if (this.facture.numeroDevis) {
            this.renderDevisAssocie();
        }
    }

    renderLignesFacture() {
        const tbody = document.getElementById('lignesFactureBody');
        const tfoot = document.getElementById('factureTotauxFoot');
        
        tbody.innerHTML = '';
        
        if (this.facture.lignes && this.facture.lignes.length > 0) {
            this.facture.lignes.forEach(ligne => {
                const montantLigneHT = ligne.quantite * ligne.prixUnitaireHt;
                const montantLigneTVA = montantLigneHT * (ligne.tauxTva / 100);
                const montantLigneTTC = montantLigneHT + montantLigneTVA;
                
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${ligne.nomProduit || 'N/A'}</td>
                    <td>${ligne.description || ''}</td>
                    <td class="text-end">${ligne.quantite}</td>
                    <td class="text-end">${ligne.prixUnitaireHt.toFixed(2)} €</td>
                    <td class="text-end">${montantLigneHT.toFixed(2)} €</td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-muted">Aucune ligne de facture</td>
                </tr>
            `;
        }

        // Totaux
        const totalHT = this.facture.montantHt || 0;
        const totalTVA = this.facture.montantTva || 0;
        const totalTTC = this.facture.montantTtc || 0;

        tfoot.innerHTML = `
            <tr>
                <td colspan="4" class="text-end"><strong>Total HT:</strong></td>
                <td class="text-end"><strong>${totalHT.toFixed(2)} €</strong></td>
            </tr>
            <tr>
                <td colspan="4" class="text-end"><strong>TVA:</strong></td>
                <td class="text-end"><strong>${totalTVA.toFixed(2)} €</strong></td>
            </tr>
            <tr class="table-active">
                <td colspan="4" class="text-end"><strong>Total TTC:</strong></td>
                <td class="text-end"><strong>${totalTTC.toFixed(2)} €</strong></td>
            </tr>
        `;
    }

    renderPaiements() {
        const container = document.getElementById('paiementsList');
        
        if (this.facture.paiements && this.facture.paiements.length > 0) {
            let html = '';
            this.facture.paiements.forEach(paiement => {
                html += `
                    <div class="border-bottom pb-2 mb-2">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <strong>${paiement.montant.toFixed(2)} €</strong><br>
                                <small class="text-muted">
                                    ${new Date(paiement.datePaiement).toLocaleDateString('fr-FR')} - 
                                    ${this.formatModePaiement(paiement.modePaiement)}
                                </small><br>
                                <small>${paiement.libelle}</small>
                            </div>
                            <span class="badge statut-${paiement.statut.toLowerCase()}">
                                ${this.formatStatutPaiement(paiement.statut)}
                            </span>
                        </div>
                        ${paiement.referenceTransaction ? `
                            <small class="text-muted">Ref: ${paiement.referenceTransaction}</small>
                        ` : ''}
                    </div>
                `;
            });
            
            // Total payé
            const totalPaye = this.facture.paiements
                .filter(p => p.statut === 'VALIDE')
                .reduce((total, p) => total + p.montant, 0);
            
            html += `
                <div class="mt-3 p-2 bg-light rounded">
                    <div class="d-flex justify-content-between">
                        <strong>Total payé:</strong>
                        <strong class="text-success">${totalPaye.toFixed(2)} €</strong>
                    </div>
                    <div class="d-flex justify-content-between">
                        <strong>Reste à payer:</strong>
                        <strong class="${this.facture.montantRestant > 0 ? 'text-danger' : 'text-success'}">
                            ${this.facture.montantRestant.toFixed(2)} €
                        </strong>
                    </div>
                </div>
            `;
            
            container.innerHTML = html;
        } else {
            container.innerHTML = `
                <div class="text-center text-muted">
                    <i class="ti ti-credit-card fs-1 mb-2"></i>
                    <p>Aucun paiement enregistré</p>
                </div>
            `;
        }
    }

    renderDevisAssocie() {
        // Ajouter une section pour le devis associé si nécessaire
        const headerSection = document.querySelector('.card-header:first-child');
        if (headerSection && this.facture.numeroDevis) {
            const devisInfo = document.createElement('div');
            devisInfo.className = 'mt-2';
            devisInfo.innerHTML = `
                <small class="text-muted">
                    <strong>Devis associé:</strong> ${this.facture.numeroDevis}
                </small>
            `;
            headerSection.appendChild(devisInfo);
        }
    }

    setupEventListeners() {
        // Impression
        document.getElementById('printFacture').addEventListener('click', () => {
            this.printFacture();
        });

        // Export PDF
        document.getElementById('exportPdf').addEventListener('click', () => {
            this.exportPdf();
        });

        // Ajout paiement
        document.getElementById('addPaiementBtn').addEventListener('click', () => {
            this.showAddPaiementModal();
        });

        // Envoi facture
        document.getElementById('sendFactureBtn').addEventListener('click', () => {
            this.sendFacture();
        });

        // Annulation facture
        document.getElementById('cancelFactureBtn').addEventListener('click', () => {
            this.cancelFacture();
        });

        // Formulaire paiement
        document.getElementById('paiementForm').addEventListener('submit', (e) => {
            this.handlePaiementSubmit(e);
        });

        // Auto-remplissage du montant maximum
        document.querySelector('input[name="montant"]').addEventListener('input', (e) => {
            this.validateMontantPaiement(e.target);
        });
    }

    updateUI() {
        // Masquer/activer les boutons selon le statut
        const sendBtn = document.getElementById('sendFactureBtn');
        const cancelBtn = document.getElementById('cancelFactureBtn');
        const addPaiementBtn = document.getElementById('addPaiementBtn');

        if (this.facture) {
            // Bouton envoyer
            if (this.facture.statut === 'BROUILLON') {
                sendBtn.style.display = 'block';
            } else {
                sendBtn.style.display = 'none';
            }

            // Bouton annuler
            if (this.facture.statut === 'BROUILLON' || this.facture.statut === 'EMISE') {
                cancelBtn.style.display = 'block';
            } else {
                cancelBtn.style.display = 'none';
            }

            // Bouton ajouter paiement
            if (this.facture.montantRestant > 0 && this.facture.statut !== 'ANNULEE') {
                addPaiementBtn.style.display = 'block';
            } else {
                addPaiementBtn.style.display = 'none';
            }
        }
    }

    showAddPaiementModal() {
        const modal = new bootstrap.Modal(document.getElementById('paiementModal'));
        
        // Pré-remplir le formulaire
        document.getElementById('factureIdPaiement').value = this.factureId;
        document.querySelector('input[name="montant"]').value = this.facture.montantRestant.toFixed(2);
        document.querySelector('input[name="montant"]').max = this.facture.montantRestant;
        document.querySelector('input[name="datePaiement"]').value = new Date().toISOString().split('T')[0];
        document.querySelector('input[name="libelle"]').value = `Paiement facture ${this.facture.numeroFacture}`;
        
        modal.show();
    }

    async handlePaiementSubmit(e) {
        e.preventDefault();
        
        const form = e.target;
        const formData = new FormData(form);
        
        const paiementData = {
            datePaiement: formData.get('datePaiement'),
            montant: parseFloat(formData.get('montant')),
            modePaiement: formData.get('modePaiement'),
            libelle: formData.get('libelle'),
            referenceTransaction: formData.get('referenceTransaction'),
            idFacture: this.factureId
        };

        // Validation
        if (paiementData.montant > this.facture.montantRestant) {
            this.showAlert(`Le montant ne peut pas dépasser le reste à payer (${this.facture.montantRestant.toFixed(2)} €)`, 'warning');
            return;
        }
        
        try {
            const response = await fetch(`/api/factures/${this.factureId}/paiements`, {
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
                
                // Recharger les données
                await this.loadFactureDetail();
                this.updateUI();
                
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            this.showAlert('Erreur: ' + error.message, 'danger');
        }
    }

    validateMontantPaiement(input) {
        const montant = parseFloat(input.value) || 0;
        const maxMontant = this.facture.montantRestant;
        
        if (montant > maxMontant) {
            input.classList.add('is-invalid');
            this.showAlert(`Le montant ne peut pas dépasser ${maxMontant.toFixed(2)} €`, 'warning');
        } else {
            input.classList.remove('is-invalid');
        }
    }

    async sendFacture() {
        if (!confirm('Envoyer cette facture au client ? Un email sera envoyé.')) return;
        
        try {
            // Implémenter l'envoi d'email
            const response = await fetch(`/api/factures/${this.factureId}/envoyer`, {
                method: 'POST'
            });
            
            if (response.ok) {
                this.showAlert('Facture envoyée avec succès', 'success');
                await this.loadFactureDetail();
                this.updateUI();
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            this.showAlert('Erreur: ' + error.message, 'danger');
        }
    }

    async cancelFacture() {
        if (!confirm('Annuler cette facture ? Cette action est irréversible.')) return;
        
        const raison = prompt('Veuillez saisir la raison de l\'annulation:');
        if (!raison) return;
        
        try {
            const response = await fetch(`/api/factures/${this.factureId}/annuler?raison=${encodeURIComponent(raison)}`, {
                method: 'PUT'
            });
            
            if (response.ok) {
                this.showAlert('Facture annulée avec succès', 'success');
                await this.loadFactureDetail();
                this.updateUI();
            } else {
                const error = await response.text();
                throw new Error(error);
            }
        } catch (error) {
            this.showAlert('Erreur: ' + error.message, 'danger');
        }
    }

    printFacture() {
        window.print();
    }

    exportPdf() {
        this.showAlert('Export PDF à implémenter', 'info');
        // Implémenter la génération PDF
        // window.open(`/api/factures/${this.factureId}/pdf`, '_blank');
    }

    // === MÉTHODES UTILITAIRES ===

    isFactureEnRetard() {
        if (!this.facture.dateEcheance || this.facture.statut === 'PAYEE' || this.facture.statut === 'ANNULEE') {
            return false;
        }
        return new Date(this.facture.dateEcheance) < new Date() && this.facture.montantRestant > 0;
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

    formatStatutPaiement(statut) {
        const statuts = {
            'VALIDE': 'Validé',
            'EN_ATTENTE': 'En attente',
            'REFUSE': 'Refusé'
        };
        return statuts[statut] || statut;
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

    // Méthode pour formater les nombres
    formatNumber(number) {
        return new Intl.NumberFormat('fr-FR', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(number);
    }
}

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    window.factureDetail = new FactureDetail();
});

// Style d'impression
const style = document.createElement('style');
style.textContent = `
    @media print {
        .btn, .modal, .alert, .card-header .btn {
            display: none !important;
        }
        .card {
            border: none !important;
            box-shadow: none !important;
        }
        .card-header {
            background: white !important;
            color: black !important;
            border-bottom: 2px solid #000 !important;
        }
        .table-active {
            background-color: #f8f9fa !important;
        }
    }
`;
document.head.appendChild(style);