/* =========================================
   Ventes par mois (bar chart)
   ========================================= */
var ventesParMoisCtx = document.getElementById('ventesParMoisChart').getContext('2d');

// Récupération correcte des données Thymeleaf
var ventesParMoisData = /*[[${ventesParMois}]]*/ {};
var ventesLabels = Object.keys(ventesParMoisData);
var ventesData = Object.values(ventesParMoisData);

// Formater les mois pour affichage lisible
var monthNames = ["Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"];
var ventesLabelsFormatted = ventesLabels.map(function(mois) {
    var parts = mois.split("-"); // "2025-01"
    return monthNames[parseInt(parts[1], 10) - 1] + " " + parts[0];
});

var ventesParMoisChart = new Chart(ventesParMoisCtx, {
    type: 'bar',
    data: {
        labels: ventesLabelsFormatted,
        datasets: [{
            label: 'Montant ventes',
            data: ventesData,
            backgroundColor: '#0d6efd'
        }]
    },
    options: {
        responsive: true,
        plugins: {
            legend: { display: false },
            tooltip: { 
                mode: 'index', 
                intersect: false,
                callbacks: {
                    label: function(context) {
                        return context.dataset.label + ': ' + context.parsed.y + ' FCFA';
                    }
                }
            }
        },
        scales: {
            y: { 
                beginAtZero: true,
                title: {
                    display: true,
                    text: 'Montant (CFA)'
                }
            },
            x: { 
                ticks: { autoSkip: false },
                title: {
                    display: true,
                    text: 'Mois'
                }
            }
        }
    }
});

/* =========================================
   Opportunités par étape (pie chart)
   ========================================= */
var oppCtx = document.getElementById('opportunitesParEtapeChart').getContext('2d');

// Récupération correcte des données Thymeleaf
var opportunitesParEtapeData = /*[[${opportunitesParEtape}]]*/ {};
var oppLabels = Object.keys(opportunitesParEtapeData);
var oppData = Object.values(opportunitesParEtapeData);

var opportunitesChart = new Chart(oppCtx, {
    type: 'pie',
    data: {
        labels: oppLabels,
        datasets: [{
            label: 'Nombre d\'opportunités',
            data: oppData,
            backgroundColor: ['#0d6efd', '#198754', '#ffc107', '#dc3545', '#6c757d', '#0dcaf0']
        }]
    },
    options: {
        responsive: true,
        plugins: {
            legend: { 
                position: 'bottom',
                labels: {
                    padding: 20,
                    usePointStyle: true
                }
            },
            tooltip: { 
                callbacks: { 
                    label: function(context) {
                        var total = context.dataset.data.reduce((a, b) => a + b, 0);
                        var percentage = Math.round((context.raw / total) * 100);
                        return context.label + ': ' + context.raw + ' (' + percentage + '%)';
                    }
                }
            }
        }
    }
});