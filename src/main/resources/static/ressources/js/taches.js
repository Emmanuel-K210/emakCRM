class TacheManager {
    constructor() {
        this.taches = [];
        this.draggedTask = null;
        this.calendar = null;
        this.currentView = 'kanban';
        this.init();
    }

    async init() {
        await this.loadTaches();
        this.setupEventListeners();
        await this.loadSelectOptions();
        this.setupDragAndDrop();
        this.setupViewSwitcher();
        this.initCalendar();
        this.initExports();
        
        // Diagnostic après initialisation
        setTimeout(() => this.diagnosticComplet(), 1000);
    }

    initExports() {
        document.getElementById('exportPdf')?.addEventListener('click', () => this.exportToPDF());
        document.getElementById('exportExcel')?.addEventListener('click', () => this.exportToExcel());
        document.getElementById('exportCsv')?.addEventListener('click', () => this.exportToCSV());
    }

    // === INITIALISATION DES VUES ===
    initCalendar() {
        const calendarEl = document.getElementById('calendar');
        if (!calendarEl) return;

        this.calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            locale: 'fr',
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
            },
            events: this.formatTachesForCalendar.bind(this),
            eventClick: this.handleCalendarEventClick.bind(this),
            eventDrop: this.handleCalendarEventDrop.bind(this),
            eventResize: this.handleCalendarEventResize.bind(this),
            editable: true,
            droppable: true,
            eventTimeFormat: {
                hour: '2-digit',
                minute: '2-digit',
                meridiem: false
            }
        });

        this.calendar.render();
    }

    // === EXPORTS ===
    exportToPDF() {
        try {
            if (typeof jspdf === 'undefined') {
                this.showAlert('Bibliothèque PDF non chargée', 'warning');
                return;
            }

            const { jsPDF } = window.jspdf;
            const doc = new jsPDF();
            
            doc.text('Liste des Tâches - eMakCRM', 20, 20);
            doc.text(`Généré le ${new Date().toLocaleDateString('fr-FR')}`, 20, 30);
            
            let yPosition = 50;
            this.taches.forEach((tache, index) => {
                if (yPosition > 270) {
                    doc.addPage();
                    yPosition = 20;
                }
                
                doc.text(`${index + 1}. ${tache.titre}`, 20, yPosition);
                doc.text(`   Statut: ${tache.statut} | Priorité: ${tache.priorite}`, 20, yPosition + 7);
                yPosition += 20;
            });
            
            doc.save(`taches-${new Date().toISOString().split('T')[0]}.pdf`);
            this.showAlert('PDF exporté avec succès!', 'success');
            
        } catch (error) {
            console.error('Erreur export PDF:', error);
            this.showAlert('Erreur lors de l\'export PDF', 'danger');
        }
    }

    exportToExcel() {
        try {
            if (typeof XLSX === 'undefined') {
                this.showAlert('Bibliothèque Excel non chargée', 'warning');
                return;
            }

            const data = this.taches.map(tache => ({
                'ID': tache.id,
                'Titre': tache.titre,
                'Description': tache.description || '',
                'Statut': tache.statut,
                'Priorité': tache.priorite,
                'Date Échéance': tache.dateEcheance ? new Date(tache.dateEcheance).toLocaleDateString('fr-FR') : '',
                'Assigné à': tache.nomUtilisateur || 'Non assigné'
            }));
            
            const ws = XLSX.utils.json_to_sheet(data);
            const wb = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(wb, ws, 'Tâches');
            XLSX.writeFile(wb, `taches-${new Date().toISOString().split('T')[0]}.xlsx`);
            
            this.showAlert('Excel exporté avec succès!', 'success');
            
        } catch (error) {
            console.error('Erreur export Excel:', error);
            this.showAlert('Erreur lors de l\'export Excel', 'danger');
        }
    }

    exportToCSV() {
        try {
            const headers = ['ID', 'Titre', 'Statut', 'Priorité', 'Échéance', 'Assigné à'];
            const csvData = this.taches.map(tache => [
                tache.id,
                `"${tache.titre.replace(/"/g, '""')}"`,
                tache.statut,
                tache.priorite,
                tache.dateEcheance ? new Date(tache.dateEcheance).toLocaleDateString('fr-FR') : '',
                `"${(tache.nomUtilisateur || '').replace(/"/g, '""')}"`
            ]);
            
            const csvContent = [
                headers.join(','),
                ...csvData.map(row => row.join(','))
            ].join('\n');
            
            const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
            const link = document.createElement('a');
            const url = URL.createObjectURL(blob);
            
            link.setAttribute('href', url);
            link.setAttribute('download', `taches-${new Date().toISOString().split('T')[0]}.csv`);
            link.style.visibility = 'hidden';
            
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            this.showAlert('CSV exporté avec succès!', 'success');
            
        } catch (error) {
            console.error('Erreur export CSV:', error);
            this.showAlert('Erreur lors de l\'export CSV', 'danger');
        }
    }

    setupViewSwitcher() {
        const viewSelect = document.getElementById('viewSelect');
        if (viewSelect) {
            viewSelect.addEventListener('change', (e) => {
                this.switchView(e.target.value);
            });
        }
    }

    // === GESTION DES DONNÉES ===
    async loadTaches() {
        try {
            console.log('Chargement des tâches...');
            
            const response = await fetch('/api/taches');
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            this.taches = await response.json();
            console.log('Tâches chargées:', this.taches.length);
            
            this.refreshAllViews();
            
        } catch (error) {
            console.error('Erreur détaillée:', error);
            this.showAlert('Erreur lors du chargement des tâches: ' + error.message, 'danger');
        }
    }

    async loadSelectOptions() {
        try {
            // Charger les utilisateurs
            const usersResponse = await fetch('/api/references/utilisateurs');
            const users = await usersResponse.json();
            this.populateSelect('idUtilisateur', users);

            // Charger les clients
            const clientsResponse = await fetch('/api/references/clients');
            const clients = await clientsResponse.json();
            this.populateSelect('idClient', clients);

            // Charger les opportunités
            const oppsResponse = await fetch('/api/references/opportunites');
            const opps = await oppsResponse.json();
            this.populateSelect('idOpportunite', opps);

        } catch (error) {
            console.error('Erreur chargement options:', error);
        }
    }

    populateSelect(selectName, options) {
        const select = document.querySelector(`select[name="${selectName}"]`);
        if (!select) return;

        // Garder la première option
        const firstOption = select.options[0];
        select.innerHTML = '';
        select.appendChild(firstOption);

        options.forEach(option => {
            const optElement = document.createElement('option');
            optElement.value = option.id;
            optElement.textContent = option.nom || option.entreprise || option.email;
            select.appendChild(optElement);
        });
    }

    // === RENDU DES VUES ===
    refreshAllViews() {
        // Vue Kanban
        this.renderKanbanView();
        
        // Vue Calendrier
        if (this.calendar) {
            this.calendar.refetchEvents();
        }
        
        // Vue Liste
        if (this.currentView === 'list') {
            this.renderListView();
        }
    }

    renderKanbanView() {
        // Vider toutes les colonnes
        document.querySelectorAll('.kanban-column').forEach(col => {
            col.innerHTML = '';
        });

        // Compter les tâches par statut
        const counts = { 'A_FAIRE': 0, 'EN_COURS': 0, 'TERMINE': 0, 'ANNULE': 0 };

        this.taches.forEach(tache => {
            this.createTaskCard(tache);
            counts[tache.statut] = (counts[tache.statut] || 0) + 1;
        });
		

        // Mettre à jour les compteurs
        Object.keys(counts).forEach(statut => {
            const element = document.getElementById(statut.toLowerCase() + 'Count');
            if (element) {
                element.textContent = counts[statut];
            }
        });
		
    }

    createTaskCard(tache) {
        const column = document.querySelector(`[data-status="${tache.statut}"]`);
        if (!column) return;

        const card = document.createElement('div');
        card.className = 'card task-card mb-3';
        card.draggable = true;
        card.dataset.taskId = tache.idTache;

        const priorityClass = this.getPriorityClass(tache.priorite);
        const statusClass = this.getStatusClass(tache.statut);
        const isLate = this.isTaskLate(tache);

        card.innerHTML = `
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <h6 class="card-title mb-0">${this.escapeHtml(tache.titre)}</h6>
                   <span class="badge ${priorityClass}">${tache.priorite}</span>
                </div>
                ${tache.description ? `<p class="card-text small">${this.escapeHtml(tache.description)}</p>` : ''}
                
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <small class="text-muted">
                        <i class="fas fa-user me-1"></i>${tache.nomUtilisateur || 'Non assigné'}
                    </small>
                    <span class="badge ${statusClass}">${tache.statut}</span>
                </div>
                
                ${tache.dateEcheance ? `
                <div class="d-flex justify-content-between align-items-center">
                    <small class="${isLate ? 'text-danger' : 'text-muted'}">
                        <i class="fas fa-clock me-1"></i>${this.formatDate(tache.dateEcheance)}
                        ${isLate ? '<i class="fas fa-exclamation-triangle ms-1"></i>' : ''}
                    </small>
                </div>
                ` : ''}
                
                <div class="mt-2 d-flex gap-1">
                    <button class="btn btn-sm btn-outline-primary edit-task" data-id="${tache.idTache}">
                        <i class="ti ti-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-success change-status" data-id="${tache.idTache}">
                      <i class="ti ti-refresh"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger delete-task" data-id="${tache.idTache}">
                        <i class="ti ti-trash"></i>
                    </button>
                </div>
            </div>
        `;

        // Ajouter les écouteurs d'événements
        card.querySelector('.edit-task').addEventListener('click', () => {
            this.editTache(tache.idTache);
        });
        
        card.querySelector('.change-status').addEventListener('click', () => {
            this.showStatusModal(tache.idTache);
        });
        
        card.querySelector('.delete-task').addEventListener('click', () => {
            this.deleteTache(tache.idTache);
        });

        // Configurer le drag & drop
        card.addEventListener('dragstart', this.handleDragStart.bind(this));
        card.addEventListener('dragend', this.handleDragEnd.bind(this));

        column.appendChild(card);
    }

    renderListView() {
        const tbody = document.querySelector('#tasksTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        this.taches.forEach(tache => {
            const row = document.createElement('tr');
            const isLate = this.isTaskLate(tache);
            
            row.innerHTML = `
                <td>
                    <strong>${this.escapeHtml(tache.titre)}</strong>
                    ${tache.description ? `<br><small class="text-muted">${this.escapeHtml(tache.description)}</small>` : ''}
                </td>
                <td>
                    <span class="badge ${this.getPriorityClass(tache.priorite)}">
                        ${tache.priorite}
                    </span>
                </td>
                <td>
                    <span class="badge ${this.getStatusClass(tache.statut)}">
                        ${tache.statut}
                    </span>
                </td>
                <td>${tache.nomUtilisateur || 'Non assigné'}</td>
                <td>
                    ${tache.dateEcheance ? `
                        <span class="${isLate ? 'text-danger' : ''}">
                            <i class="fas fa-clock me-1"></i>
                            ${this.formatDate(tache.dateEcheance)}
                            ${isLate ? '<i class="fas fa-exclamation-triangle ms-1 text-danger"></i>' : ''}
                        </span>
                    ` : 'Non définie'}
                </td>
                <td>${tache.nomClient || '-'}</td>
                <td>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-primary edit-task" data-id="${tache.idTache}">
                            <i class="ti ti-edit"></i>
                        </button>
                        <button class="btn btn-outline-success change-status" data-id="${tache.idTache}">
                            <i class="ti ti-refresh"></i>
                        </button>
                        <button class="btn btn-outline-danger delete-task" data-id="${tache.idTache}">
                            <i class="ti ti-trash"></i>
                        </button>
                    </div>
                </td>
            `;

            // Ajouter les écouteurs d'événements
            row.querySelector('.edit-task').addEventListener('click', () => this.editTache(tache.idTache));
            row.querySelector('.change-status').addEventListener('click', () => this.showStatusModal(tache.idTache));
            row.querySelector('.delete-task').addEventListener('click', () => this.deleteTache(tache.idTache));

            tbody.appendChild(row);
        });

        // Initialiser DataTables si disponible
        if (window.$ && $.fn.DataTable) {
            $('#tasksTable').DataTable({
                language: {
                    url: '//cdn.datatables.net/plug-ins/1.13.6/i18n/fr-FR.json'
                },
                pageLength: 25,
                order: [[4, 'asc']]
            });
        }
    }

    // === GESTION DU DRAG & DROP ===
    setupDragAndDrop() {
        const columns = document.querySelectorAll('.kanban-column');
        
        columns.forEach(column => {
            column.addEventListener('dragover', this.handleDragOver.bind(this));
            column.addEventListener('dragenter', this.handleDragEnter.bind(this));
            column.addEventListener('dragleave', this.handleDragLeave.bind(this));
            column.addEventListener('drop', this.handleDrop.bind(this));
        });
    }

    handleDragStart(e) {
        this.draggedTask = e.target;
        e.target.classList.add('dragging');
        e.dataTransfer.effectAllowed = 'move';
    }

    handleDragEnd(e) {
        e.target.classList.remove('dragging');
        document.querySelectorAll('.kanban-column').forEach(col => {
            col.classList.remove('drag-over');
        });
    }

    handleDragOver(e) {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
    }

    handleDragEnter(e) {
        e.preventDefault();
        e.target.closest('.kanban-column').classList.add('drag-over');
    }

    handleDragLeave(e) {
        e.target.closest('.kanban-column').classList.remove('drag-over');
    }

	async handleDrop(e) {
	    e.preventDefault();
	    const column = e.target.closest('.kanban-column');
	    if (!column) return;
	    
	    column.classList.remove('drag-over');

	    if (this.draggedTask) {
	        // ✅ RÉCUPÉRATION ET VALIDATION
	        const taskId = this.draggedTask.dataset.taskId;
	        const newStatus = column.dataset.status;

	        console.log("🔍 Drag&Drop - ID:", taskId, "Nouveau statut:", newStatus);

	        if (!taskId || taskId === 'undefined') {
	            console.error('❌ ID invalide dans drag&drop');
	            this.showAlert('Erreur: impossible de déplacer cette tâche', 'danger');
	            return;
	        }

	        try {
	            await this.updateTaskStatus(taskId, newStatus);
	        } catch (error) {
	            console.error('❌ Erreur déplacement:', error);
	            this.loadTaches(); // Restaurer l'interface
	        }
	    }
	}

    // === GESTION DU CALENDRIER ===
    formatTachesForCalendar(info, successCallback, failureCallback) {
        const events = this.taches.map(tache => {
            const isLate = this.isTaskLate(tache);
            
            return {
                id: tache.idTache.toString(),
                title: tache.titre,
                start: tache.dateDebut || tache.dateEcheance || new Date(),
                end: tache.dateEcheance,
                extendedProps: {
                    tache: tache
                },
                color: this.getCalendarEventColor(tache),
                textColor: 'white',
                borderColor: this.getCalendarEventColor(tache),
                classNames: isLate ? ['fc-event-late'] : []
            };
        });

        successCallback(events);
    }

    getCalendarEventColor(tache) {
        const colors = {
            'URGENTE': '#ff001a',
            'HAUTE': '#dc3545',
            'NORMALE': '#ffc107',
            'BASSE': '#198754'
        };

        const statusColors = {
            'A_FAIRE': '#0d6efd',
            'EN_COURS': '#fd7e14',
            'TERMINE': '#198754',
            'ANNULE': '#6c757d'
        };

        return statusColors[tache.statut] || colors[tache.priorite] || '#6c757d';
    }

    handleCalendarEventClick(info) {
        const tache = info.event.extendedProps.tache;
        this.showEditModal(tache);
    }

    async handleCalendarEventDrop(info) {
        const tacheId = info.event.id;
        const newStart = info.event.start;
        const newEnd = info.event.end;

        try {
            await this.updateTaskDates(tacheId, newStart, newEnd);
            this.showAlert('Dates de la tâche mises à jour', 'success');
        } catch (error) {
            this.showAlert('Erreur mise à jour dates', 'danger');
            info.revert();
        }
    }

    async handleCalendarEventResize(info) {
        const tacheId = info.event.id;
        const newEnd = info.event.end;

        try {
            await this.updateTaskEndDate(tacheId, newEnd);
            this.showAlert('Date de fin mise à jour', 'success');
        } catch (error) {
            this.showAlert('Erreur mise à jour date fin', 'danger');
            info.revert();
        }
    }

    // === ACTIONS SUR LES TÂCHES ===
	
	async updateTaskStatus(taskId, newStatus) {
	    // ✅ VALIDATION OBLIGATOIRE
	    console.log("🔍 Debug updateTaskStatus - ID reçu:", taskId, "Type:", typeof taskId);
	    
	    if (!taskId || taskId === 'undefined' || taskId === 'null') {
	        console.error('❌ ID invalide:', taskId);
	        this.showAlert('Erreur: ID de tâche invalide', 'danger');
	        return;
	    }

	    // ✅ CONVERSION SÉCURISÉE
	    const id = Number(taskId);
	    if (isNaN(id) || id <= 0) {
	        console.error('❌ ID non numérique:', taskId);
	        this.showAlert('Erreur: ID de tâche non valide', 'danger');
	        return;
	    }

	    try {
	        console.log(`🔄 PUT /api/taches/${id}/statut - ${newStatus}`);
	        
	        // ✅ UTILISER VOTRE ENDPOINT PATCH EXISTANT
	        const response = await fetch(`/api/taches/${id}/statut`, {
	            method: 'PATCH',
	            headers: {
	                'Content-Type': 'application/json',
	            },
	            body: JSON.stringify({ 
	                statut: newStatus 
	            })
	        });

	        if (!response.ok) {
	            const errorText = await response.text();
	            throw new Error(`HTTP ${response.status}: ${errorText}`);
	        }

	        const result = await response.json();
	        console.log('✅ Statut mis à jour:', result);
	        
	        this.showAlert('Statut mis à jour avec succès', 'success');
	        await this.loadTaches(); // Recharger les données
	        
	    } catch (error) {
	        console.error('❌ Erreur mise à jour statut:', error);
	        this.showAlert('Erreur lors de la mise à jour: ' + error.message, 'danger');
	    }
	}

    async updateTaskStatusFallback(taskId, newStatus) {
        try {
            // Récupérer la tâche existante
            const getResponse = await fetch(`/api/taches/${taskId}`);
            if (!getResponse.ok) throw new Error('Tâche non trouvée');
            
            const tache = await getResponse.json();
            
            // Mettre à jour seulement le statut
            const updatedTache = {
                ...tache,
                statut: newStatus
            };
            
            // Envoyer la mise à jour complète
            const updateResponse = await fetch(`/api/taches/${taskId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(updatedTache)
            });

            if (!updateResponse.ok) {
                throw new Error('Erreur mise à jour');
            }
            
            this.showAlert('Statut mis à jour avec succès', 'success');
            await this.loadTaches();
            
        } catch (error) {
            throw new Error(`Fallback failed: ${error.message}`);
        }
    }

    async updateTaskDates(taskId, dateDebut, dateEcheance) {
        const response = await fetch(`/api/taches/${taskId}`);
        const tache = await response.json();

        const updatedTache = {
            ...tache,
            dateDebut: dateDebut ? dateDebut.toISOString() : null,
            dateEcheance: dateEcheance ? dateEcheance.toISOString() : null
        };

        const updateResponse = await fetch(`/api/taches/${taskId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updatedTache)
        });

        if (!updateResponse.ok) {
            throw new Error('Erreur mise à jour dates');
        }
    }

    async updateTaskEndDate(taskId, dateEcheance) {
        const response = await fetch(`/api/taches/${taskId}`);
        const tache = await response.json();

        const updatedTache = {
            ...tache,
            dateEcheance: dateEcheance ? dateEcheance.toISOString() : null
        };

        const updateResponse = await fetch(`/api/taches/${taskId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updatedTache)
        });

        if (!updateResponse.ok) {
            throw new Error('Erreur mise à jour date fin');
        }
    }

    async editTache(taskId) {
        try {
            const response = await fetch(`/api/taches/${taskId}`);
            const tache = await response.json();
            this.showEditModal(tache);
        } catch (error) {
            this.showAlert('Erreur lors du chargement de la tâche', 'danger');
        }
    }

    showEditModal(tache) {
        // Remplir le formulaire avec les données de la tâche
        const form = document.getElementById('taskForm');
        form.querySelector('input[name="titre"]').value = tache.titre || '';
        form.querySelector('textarea[name="description"]').value = tache.description || '';
        form.querySelector('select[name="priorite"]').value = tache.priorite || '';
        form.querySelector('input[name="dateDebut"]').value = this.formatDateTimeForInput(tache.dateDebut);
        form.querySelector('input[name="dateEcheance"]').value = this.formatDateTimeForInput(tache.dateEcheance);
        form.querySelector('select[name="idUtilisateur"]').value = tache.idUtilisateur || '';
        form.querySelector('select[name="idClient"]').value = tache.idClient || '';
        form.querySelector('select[name="idOpportunite"]').value = tache.idOpportunite || '';

        // Changer le formulaire pour la mise à jour
        form.method = 'PUT';
        form.action = `/api/taches/${tache.id}`;

        // Changer le texte du modal
        document.querySelector('#addTaskModal .modal-title').textContent = 'Modifier la Tâche';
        document.querySelector('#addTaskModal .btn-primary').textContent = 'Mettre à jour';

        // Ouvrir le modal
        new bootstrap.Modal(document.getElementById('addTaskModal')).show();
    }

    showStatusModal(taskId) {
        const modalHtml = `
            <div class="modal fade" id="statusModal" tabindex="-1">
                <div class="modal-dialog modal-sm">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">Changer le statut</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <select class="form-select" id="newStatusSelect">
                                <option value="A_FAIRE">À faire</option>
                                <option value="EN_COURS">En cours</option>
                                <option value="TERMINE">Terminé</option>
                                <option value="ANNULE">Annulé</option>
                            </select>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                            <button type="button" class="btn btn-primary" id="confirmStatusChange">Changer</button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHtml);
        
        const statusModal = new bootstrap.Modal(document.getElementById('statusModal'));
        statusModal.show();

        document.getElementById('confirmStatusChange').addEventListener('click', async () => {
            const newStatus = document.getElementById('newStatusSelect').value;
            await this.updateTaskStatus(taskId, newStatus);
            statusModal.hide();
            document.getElementById('statusModal').remove();
        });

        document.getElementById('statusModal').addEventListener('hidden.bs.modal', function () {
            this.remove();
        });
    }

    async deleteTache(taskId) {
        if (!confirm('Êtes-vous sûr de vouloir supprimer cette tâche ?')) {
            return;
        }

        try {
            const response = await fetch(`/api/taches/${taskId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                this.showAlert('Tâche supprimée avec succès', 'success');
                this.loadTaches();
            } else {
                throw new Error('Erreur suppression');
            }
        } catch (error) {
            this.showAlert('Erreur lors de la suppression de la tâche', 'danger');
        }
    }

    // === GESTION DES VUES ===
    switchView(viewName) {
        // Cacher toutes les vues
        document.getElementById('kanbanView').classList.add('d-none');
        document.getElementById('calendarView').classList.add('d-none');
        document.getElementById('listView').classList.add('d-none');

        // Afficher la vue sélectionnée
        this.currentView = viewName;
        
        switch(viewName) {
            case 'kanban':
                document.getElementById('kanbanView').classList.remove('d-none');
                break;
            case 'calendar':
                document.getElementById('calendarView').classList.remove('d-none');
                if (this.calendar) {
                    this.calendar.render();
                }
                break;
            case 'list':
                document.getElementById('listView').classList.remove('d-none');
                this.renderListView();
                break;
        }
    }

    // === GESTION DES ÉVÉNEMENTS ===
    setupEventListeners() {
        // Soumission du formulaire
        document.getElementById('taskForm').addEventListener('submit', this.handleFormSubmit.bind(this));

        // Filtres
        document.getElementById('statusFilter')?.addEventListener('change', () => this.applyFilters());
        document.getElementById('priorityFilter')?.addEventListener('change', () => this.applyFilters());
        document.getElementById('dateFilter')?.addEventListener('change', () => this.applyFilters());
        
        // Réinitialisation filtres
        document.getElementById('resetFilters')?.addEventListener('click', () => this.resetFilters());

        // Réinitialiser le formulaire quand le modal est fermé
        document.getElementById('addTaskModal')?.addEventListener('hidden.bs.modal', () => {
            this.resetForm();
        });
    }

    async handleFormSubmit(e) {
        e.preventDefault();
        
        const form = e.target;
        const formData = new FormData(form);
        
        const tacheData = {
            titre: formData.get('titre'),
            description: formData.get('description'),
            priorite: formData.get('priorite'),
            dateDebut: formData.get('dateDebut') || null,
            dateEcheance: formData.get('dateEcheance') || null,
            idUtilisateur: parseInt(formData.get('idUtilisateur')),
            idClient: formData.get('idClient') ? parseInt(formData.get('idClient')) : null,
            idOpportunite: formData.get('idOpportunite') ? parseInt(formData.get('idOpportunite')) : null
        };

        try {
            const url = form.action || '/api/taches';
            const method = form.method || 'POST';

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(tacheData)
            });

            if (response.ok) {
                const result = await response.json();
                this.showAlert(method === 'POST' ? 'Tâche créée avec succès' : 'Tâche mise à jour avec succès', 'success');
                
                // Fermer le modal et recharger les tâches
                bootstrap.Modal.getInstance(document.getElementById('addTaskModal')).hide();
                this.loadTaches();
                this.resetForm();
            } else {
                const error = await response.json();
                throw new Error(error.message || 'Erreur inconnue');
            }
        } catch (error) {
            this.showAlert('Erreur: ' + error.message, 'danger');
        }
    }

    applyFilters() {
        const statusFilter = document.getElementById('statusFilter')?.value;
        const priorityFilter = document.getElementById('priorityFilter')?.value;
        const dateFilter = document.getElementById('dateFilter')?.value;

        let filteredTaches = this.taches;

        if (statusFilter) {
            filteredTaches = filteredTaches.filter(t => t.statut === statusFilter);
        }

        if (priorityFilter) {
            filteredTaches = filteredTaches.filter(t => t.priorite === priorityFilter);
        }

        if (dateFilter) {
            filteredTaches = filteredTaches.filter(t => {
                if (!t.dateEcheance) return false;
                const taskDate = new Date(t.dateEcheance).toISOString().split('T')[0];
                return taskDate === dateFilter;
            });
        }

        this.renderKanbanView(filteredTaches);
    }

    resetFilters() {
        document.getElementById('statusFilter').value = '';
        document.getElementById('priorityFilter').value = '';
        document.getElementById('dateFilter').value = '';
        this.renderKanbanView(this.taches);
    }

    resetForm() {
        const form = document.getElementById('taskForm');
        form.reset();
        form.method = 'POST';
        form.action = '/api/taches';
        document.querySelector('#addTaskModal .modal-title').textContent = 'Nouvelle Tâche';
        document.querySelector('#addTaskModal .btn-primary').textContent = 'Créer la tâche';
    }

    // === UTILITAIRES ===
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    formatDate(dateString) {
        if (!dateString) return 'Non définie';
        return new Date(dateString).toLocaleDateString('fr-FR');
    }

    formatDateTimeForInput(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toISOString().slice(0, 16);
    }

    isTaskLate(tache) {
        if (!tache.dateEcheance || tache.statut === 'TERMINE') return false;
        return new Date(tache.dateEcheance) < new Date();
    }

    getPriorityClass(priorite) {
        const classes = {
            'URGENTE': 'bg-danger',
            'HAUTE': 'bg-danger',
            'NORMALE': 'bg-warning',
            'BASSE': 'bg-success'
        };
        return classes[priorite] || 'bg-secondary';
    }

    getStatusClass(statut) {
        const classes = {
            'A_FAIRE': 'bg-primary',
            'EN_COURS': 'bg-warning text-dark',
            'TERMINE': 'bg-success',
            'ANNULE': 'bg-secondary'
        };
        return classes[statut] || 'bg-secondary';
    }

    showAlert(message, type) {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        const container = document.querySelector('.container-fluid');
        if (container) {
            container.insertBefore(alertDiv, container.firstChild);

            // Auto-supprimer après 5 secondes
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.remove();
                }
            }, 5000);
        }
    }

    // === DIAGNOSTIC ===
    async diagnosticComplet() {
        console.log('=== DIAGNOSTIC TACHEMANAGER ===');
        
        // Test des endpoints
        const endpoints = [
            '/api/taches',
            '/api/references/utilisateurs',
            '/api/references/clients',
            '/api/references/opportunites'
        ];
        
        for (const endpoint of endpoints) {
            try {
                const response = await fetch(endpoint);
                console.log(`✅ ${endpoint}: ${response.status}`);
            } catch (error) {
                console.log(`❌ ${endpoint}: ${error.message}`);
            }
        }
        
        // Vérification des données
        console.log(`Tâches chargées: ${this.taches.length}`);
        console.log(`Vue active: ${this.currentView}`);
        console.log(`Calendar initialisé: ${!!this.calendar}`);
        
        // Vérification des bibliothèques
        const libs = {
            'FullCalendar': typeof FullCalendar,
            'SockJS': typeof SockJS, 
            'Stomp': typeof Stomp,
            'jspdf': typeof jspdf,
            'XLSX': typeof XLSX
        };
        
        Object.entries(libs).forEach(([lib, status]) => {
            console.log(`${status !== 'undefined' ? '✅' : '❌'} ${lib}`);
        });
    }
}

class NotificationManager {
    constructor(tacheManager) {
        this.tacheManager = tacheManager;
        this.stompClient = null;
        this.connected = false;
        this.init();
    }

    init() {
        this.connectWebSocket();
        this.setupNotificationUI();
    }

    connectWebSocket() {
        try {
            const socket = new SockJS('/ws-taches');
            this.stompClient = Stomp.over(socket);

            this.stompClient.connect({}, (frame) => {
                this.connected = true;
                console.log('✅ Connecté aux notifications WebSocket');
                this.showConnectionStatus(true);

                // S'abonner aux notifications de tâches
                this.stompClient.subscribe('/topic/taches', (message) => {
                    const notification = JSON.parse(message.body);
                    this.handleNotification(notification);
                });
            }, (error) => {
                console.error('❌ Erreur WebSocket:', error);
                this.connected = false;
                this.showConnectionStatus(false);
                
                // Tentative de reconnexion après 5 secondes
                setTimeout(() => this.connectWebSocket(), 5000);
            });
        } catch (error) {
            console.error('Erreur initialisation WebSocket:', error);
        }
    }

    handleNotification(notification) {
        console.log('📨 Notification reçue:', notification);
        
        // Afficher la notification
        this.showBrowserNotification(notification);
        
        // Recharger les tâches
        this.tacheManager.loadTaches();
        
        // Jouer un son (optionnel)
        this.playNotificationSound();
    }

    showBrowserNotification(notification) {
        // Notification native du navigateur
        if ('Notification' in window && Notification.permission === 'granted') {
            new Notification('eMakCRM - Tâches', {
                body: notification.message,
                icon: '/images/logo.png'
            });
        }
        
        // Notification dans l'interface
        this.tacheManager.showAlert(notification.message, 'info');
    }

    async requestNotificationPermission() {
        if ('Notification' in window && Notification.permission === 'default') {
            const permission = await Notification.requestPermission();
            if (permission === 'granted') {
                this.showBrowserNotification({
                    message: '🔔 Notifications activées pour les tâches'
                });
            }
        }
    }

    playNotificationSound() {
        try {
            // Créer un son simple (optionnel)
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();
            
            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);
            
            oscillator.frequency.value = 800;
            gainNode.gain.value = 0.1;
            
            oscillator.start();
            setTimeout(() => oscillator.stop(), 100);
        } catch (error) {
            console.log('Son notification ignoré');
        }
    }

    showConnectionStatus(connected) {
        // Créer ou mettre à jour l'indicateur de statut
        let indicator = document.getElementById('websocketIndicator');
        
        if (!indicator) {
            indicator = document.createElement('div');
            indicator.id = 'websocketIndicator';
            indicator.className = 'position-fixed bottom-0 end-0 m-3';
            indicator.style.zIndex = '1050';
            document.body.appendChild(indicator);
        }

        if (connected) {
            indicator.innerHTML = `
                <div class="toast show">
                    <div class="toast-header bg-success text-white">
                        <i class="fas fa-wifi me-2"></i>
                        <strong class="me-auto">Connecté</strong>
                        <small>en temps réel</small>
                    </div>
                </div>
            `;
        } else {
            indicator.innerHTML = `
                <div class="toast show">
                    <div class="toast-header bg-warning text-dark">
                        <i class="fas fa-wifi-slash me-2"></i>
                        <strong class="me-auto">Hors ligne</strong>
                        <small>reconnexion...</small>
                    </div>
                </div>
            `;
        }
    }

    setupNotificationUI() {
        document.getElementById('enableNotifications')?.addEventListener('click', () => {
            this.requestNotificationPermission();
        });
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    // Initialiser le gestionnaire de tâches
    window.tacheManager = new TacheManager();
    
    // Initialiser les notifications WebSocket
    window.notificationManager = new NotificationManager(window.tacheManager);
});