package com.emak.crm.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emak.crm.dto.AlerteResponse;
import com.emak.crm.dto.DashboardData;
import com.emak.crm.dto.InteractionResponse;
import com.emak.crm.dto.KPIMetrics;
import com.emak.crm.dto.OpportuniteResponse;
import com.emak.crm.enums.EtapeVente;
import com.emak.crm.enums.PrioriteAlerte;
import com.emak.crm.enums.StatutFacture;
import com.emak.crm.enums.StatutOpportunite;
import com.emak.crm.mapper.InteractionMapper;
import com.emak.crm.mapper.OpportuniteMapper;
import com.emak.crm.repository.ClientRepository;
import com.emak.crm.repository.FactureRepository;
import com.emak.crm.repository.InteractionRepository;
import com.emak.crm.repository.OpportuniteRepository;
import com.emak.crm.service.DashboardService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ClientRepository clientRepository;
    private final FactureRepository factureRepository;
    private final OpportuniteRepository opportuniteRepository;
    private final InteractionRepository interactionRepository;

    /**
     * DONNÉES COMPLÈTES DASHBOARD
     * Métier : Agrégation de toutes les données pour la vue d'ensemble
     * Contenu : CA, objectifs, pipeline, activités récentes, alertes
     * Usage : Prise de décision rapide, suivi performance
     */
    @Override
    public DashboardData getDashboardData() {
        log.info("Génération des données complètes du dashboard");
        
        KPIMetrics kpis = getKPIMetrics();
        List<OpportuniteResponse> opportunitesRecentes = getOpportunitesRecentes();
        List<InteractionResponse> activitesRecentes = getActivitesRecentes();
        Map<String, Long> pipelineStats = getPipelineStats();
        Map<String, BigDecimal> performanceCommerciale = getPerformanceCommerciale();
        List<AlerteResponse> alertes = getAlertes();
        Map<String, BigDecimal> ventesParMois = ventesParMois();

        return new DashboardData(
            kpis,
            opportunitesRecentes,
            activitesRecentes,
            pipelineStats,
            performanceCommerciale,
            alertes,
            ventesParMois,
            prepareGraphiquePerformance(kpis, pipelineStats, ventesParMois)
        );
    }

    /**
     * INDICATEURS CLÉS DE PERFORMANCE (KPI)
     * Métier : Métriques essentielles pour mesurer la santé commerciale
     * KPI : CA mensuel, taux conversion, objectifs, satisfaction
     */
    @Override
    public Map<String, Object> getMetrics() {
        log.debug("Calcul des indicateurs clés de performance");
        
        KPIMetrics kpis = getKPIMetrics();
        Map<String, Object> metrics = new LinkedHashMap<>();
        
        metrics.put("chiffreAffairesMensuel", kpis.chiffreAffairesMensuel());
        metrics.put("objectifMensuel", kpis.objectifMensuel());
        metrics.put("tauxObjectifAtteint", kpis.tauxObjectifAtteint());
        metrics.put("tauxConversion", kpis.tauxConversion());
        metrics.put("nouveauxClients", kpis.nouveauxClients());
       // metrics.put("satisfactionClient", kpis.satisfactionClient());
        metrics.put("opportunitesEnCours", kpis.opportunitesEnCours());
        metrics.put("valeurPipeline", kpis.valeurPipeline());
        metrics.put("activitesCeMois", kpis.activitesCeMois());
        metrics.put("croissanceCA", kpis.croissanceCA());
        
        return metrics;
    }

    /**
     * OPPORTUNITÉS RÉCENTES
     * Métier : Dernières opportunités créées ou modifiées
     * Usage : Suivi en temps réel, actions immédiates
     */
    @Override
    public List<OpportuniteResponse> getOpportunitesRecentes() {
        log.debug("Récupération des opportunités récentes");
        
        LocalDateTime dateLimite = LocalDateTime.now().minusDays(7);
        
        return opportuniteRepository
                .findTop10ByDateModificationAfterOrderByDateModificationDesc(dateLimite)
                .stream()
                .map(OpportuniteMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ACTIVITÉS RÉCENTES
     * Métier : Dernières interactions avec les clients
     * Usage : Suivi relation client, coordination équipe
     */
    @Override
    public List<InteractionResponse> getActivitesRecentes() {
        log.debug("Récupération des activités récentes");
        
        LocalDateTime dateLimite = LocalDateTime.now().minusDays(14);
        
        return interactionRepository
                .findTop15ByDateInteractionAfterOrderByDateInteractionDesc(dateLimite)
                .stream()
                .map(InteractionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * STATISTIQUES PIPELINE
     * Métier : Répartition des opportunités par étape de vente
     * Usage : Identification goulots, prévision chiffre d'affaires
     */
    @Override
    public Map<String, Long> getPipelineStats() {
        log.debug("Calcul des statistiques du pipeline");
        
        Map<String, Long> stats = new LinkedHashMap<>();
        
        // Récupération des counts par étape
        for (EtapeVente etape : EtapeVente.values()) {
            Long count = opportuniteRepository.countByEtapeVenteAndStatut(etape, StatutOpportunite.EN_COURS);
            stats.put(etape.name(), count != null ? count : 0L);
        }
        
        return stats;
    }

    /**
     * VENTES PAR MOIS
     * Métier : Évolution du chiffre d'affaires sur les 12 derniers mois
     * Usage : Analyse des tendances, prévisions
     */
    @Override
    public Map<String, BigDecimal> ventesParMois() {
        log.debug("Calcul des ventes par mois");
        
        Map<String, BigDecimal> ventes = new LinkedHashMap<>();
        LocalDate debutPeriode = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        
        // Générer les 12 derniers mois
        for (int i = 0; i < 12; i++) {
            YearMonth mois = YearMonth.from(debutPeriode.plusMonths(i));
            String moisFormate = mois.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH));
            ventes.put(moisFormate, BigDecimal.ZERO);
        }
        
        // Récupérer les données réelles
        List<Object[]> results = opportuniteRepository.findVentesParMois(debutPeriode.atStartOfDay());
        for (Object[] result : results) {
            Integer anneeMois = (Integer) result[0];
            BigDecimal montant = (BigDecimal) result[1];
            
            // Convertir Integer (YYYYMM) en YearMonth
            int annee = anneeMois / 100;
            int mois = anneeMois % 100;
            YearMonth yearMonth = YearMonth.of(annee, mois);
            String moisFormate = yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH));
            
            if (ventes.containsKey(moisFormate)) {
                ventes.put(moisFormate, montant != null ? montant : BigDecimal.ZERO);
            }
        }
        
        return ventes;
    }

    // Méthodes existantes maintenues pour la compatibilité
    
    @Override
    public long totalClients() {
        return clientRepository.count();
    }

    @Override
    public BigDecimal montantFactures() {
        BigDecimal total = factureRepository.sumMontantTtcByStatut(StatutFacture.PAYEE);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public long facturesNonPayees() {
        return factureRepository.countByStatut(StatutFacture.EN_RETARD);
    }

    @Override
    public long totalOpportunites() {
        return opportuniteRepository.countByStatut(StatutOpportunite.EN_COURS);
    }

    @Override
    public Map<String, Long> opportunitesParEtape() {
        return getPipelineStats();
    }

    @Override
    public long totalVentes() {
        return opportuniteRepository.countByStatut(StatutOpportunite.GAGNEE);
    }

    // Méthodes helper privées

    private KPIMetrics getKPIMetrics() {
        // Chiffre d'affaires mensuel
        BigDecimal caMensuel = calculerChiffreAffairesMensuel();
        
        // Objectif mensuel (exemple - à adapter selon votre logique métier)
        BigDecimal objectifMensuel = BigDecimal.valueOf(100000);
        
        // Taux d'objectif atteint
        BigDecimal tauxObjectifAtteint = objectifMensuel.compareTo(BigDecimal.ZERO) > 0 ?
                caMensuel.divide(objectifMensuel, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
        
        // Taux de conversion
        BigDecimal tauxConversion = calculerTauxConversion();
        
        // Nouveaux clients ce mois
        Integer nouveauxClients = calculerNouveauxClientsMensuels();
        
        // Satisfaction client (moyenne)
        //BigDecimal satisfactionClient = calculerSatisfactionClient();
        
        // Opportunités en cours
        Integer opportunitesEnCours = Math.toIntExact(totalOpportunites());
        
        // Valeur du pipeline
        BigDecimal valeurPipeline = calculerValeurPipeline();
        
        // Activités ce mois
        Integer activitesCeMois = calculerActivitesMensuelles();
        
        // Croissance du CA
        BigDecimal croissanceCA = calculerCroissanceCA();

        return new KPIMetrics(
            caMensuel,
            objectifMensuel,
            tauxObjectifAtteint,
            tauxConversion,
            nouveauxClients,
            opportunitesEnCours,
            valeurPipeline,
            activitesCeMois,
            croissanceCA
        );
    }

    private Map<String, BigDecimal> getPerformanceCommerciale() {
        log.debug("Calcul de la performance par commercial");
        
        Map<String, BigDecimal> performance = new LinkedHashMap<>();
        
        List<Object[]> results = opportuniteRepository.findPerformanceCommerciale();
        
        for (Object[] result : results) {
            String commercial = (String) result[0];
            BigDecimal ca = (BigDecimal) result[1];
            performance.put(commercial, ca != null ? ca : BigDecimal.ZERO);
        }
        
        return performance;
    }

    private List<AlerteResponse> getAlertes() {
        log.debug("Génération des alertes du dashboard");
        
        List<AlerteResponse> alertes = new ArrayList<>();
        
        // Alertes opportunités en retard
       
        long oppsEnRetard = opportuniteRepository.countByDateCloturePrevueBeforeAndStatut(
            LocalDate.now(), StatutOpportunite.EN_COURS);
        if (oppsEnRetard > 0) {
            alertes.add(new AlerteResponse(
                "OPPORTUNITE_RETARD",
                oppsEnRetard + " opportunité(s) en retard de traitement",
                PrioriteAlerte.URGENTE.name(),
                LocalDateTime.now(),
                null,
                "OPPORTUNITE"
            ));
        }
        
        // Alertes factures impayées
        long facturesImpayees = factureRepository.countByStatut(StatutFacture.EN_RETARD);
        if (facturesImpayees > 0) {
            alertes.add(new AlerteResponse(
                "FACTURE_IMPAYEE",
                facturesImpayees + " facture(s) en retard de paiement",
                PrioriteAlerte.URGENTE.name(),
                LocalDateTime.now(),
                null,
                "FACTURE"
            ));
        }
        
        // Alertes objectif mensuel en danger
        BigDecimal caMensuel = calculerChiffreAffairesMensuel();
        BigDecimal objectifMensuel = BigDecimal.valueOf(100000);
        BigDecimal progression = caMensuel.divide(objectifMensuel, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        LocalDate aujourdhui = LocalDate.now();
        int joursDansMois = aujourdhui.lengthOfMonth();
        int joursEcoules = aujourdhui.getDayOfMonth();
        BigDecimal progressionAttendue = BigDecimal.valueOf(joursEcoules)
                .divide(BigDecimal.valueOf(joursDansMois), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        if (progression.compareTo(progressionAttendue.subtract(BigDecimal.valueOf(20))) < 0) {
            alertes.add(new AlerteResponse(
                "OBJECTIF_EN_DANGER",
                "Progression du CA en retard par rapport à l'objectif mensuel",
                PrioriteAlerte.MOYENNE.name(),
                LocalDateTime.now(),
                null,
                "PERFORMANCE"
            ));
        }
        
        return alertes;
    }

    private BigDecimal calculerChiffreAffairesMensuel() {
        LocalDateTime debutMois = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
        
        // ✅ CORRIGÉ : Utiliser LocalDateTime
        BigDecimal ca = factureRepository.sumMontantTtcByDateFactureBetweenAndStatut(
            debutMois, finMois, StatutFacture.PAYEE);
        
        return ca != null ? ca : BigDecimal.ZERO;
    }

    private BigDecimal calculerTauxConversion() {
        long totalOpportunites = opportuniteRepository.count();
        long opportunitesGagnees = opportuniteRepository.countByStatut(StatutOpportunite.GAGNEE);
        
        if (totalOpportunites == 0) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(opportunitesGagnees)
                .divide(BigDecimal.valueOf(totalOpportunites), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private Integer calculerNouveauxClientsMensuels() {
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        return Math.toIntExact(clientRepository.countByDateCreationBetween(
            debutMois.atStartOfDay(), finMois.atTime(23, 59, 59)));
    }


    private BigDecimal calculerValeurPipeline() {
        BigDecimal valeur = opportuniteRepository.calculateValeurPipeline();
        return valeur != null ? valeur : BigDecimal.ZERO;
    }

    private Integer calculerActivitesMensuelles() {
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        return Math.toIntExact(interactionRepository.countByDateInteractionBetween(
            debutMois.atStartOfDay(), finMois.atTime(23, 59, 59)));
    }

    private BigDecimal calculerCroissanceCA() {
        LocalDate moisActuel = LocalDate.now().withDayOfMonth(1);
        LocalDate moisPrecedent = moisActuel.minusMonths(1);
        
        BigDecimal caMoisActuel = calculerChiffreAffairesMensuel();
        
        // ✅ CORRIGÉ : Convertir LocalDate en LocalDateTime
        LocalDateTime debutMoisPrecedent = moisPrecedent.withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMoisPrecedent = moisPrecedent.withDayOfMonth(moisPrecedent.lengthOfMonth()).atTime(23, 59, 59);
        
        // ✅ Utiliser LocalDateTime au lieu de LocalDate
        BigDecimal caMoisPrecedent = factureRepository.sumMontantTtcByDateFactureBetweenAndStatut(
            debutMoisPrecedent, 
            finMoisPrecedent,
            StatutFacture.PAYEE
        );
        
        if (caMoisPrecedent == null || caMoisPrecedent.compareTo(BigDecimal.ZERO) == 0) {
            return caMoisActuel.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        
        // Calcul du pourcentage de croissance
        return caMoisActuel.subtract(caMoisPrecedent)
                          .divide(caMoisPrecedent, 4, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100));
    }

    private Object prepareGraphiquePerformance(KPIMetrics kpis, Map<String, Long> pipelineStats, 
                                             Map<String, BigDecimal> ventesParMois) {
        // Préparation des données pour les graphiques frontend
        Map<String, Object> graphiques = new HashMap<>();
        
        // Données pour le graphique de pipeline
        graphiques.put("pipeline", pipelineStats);
        
        // Données pour le graphique d'évolution des ventes
        graphiques.put("evolutionVentes", ventesParMois);
        
        // Données pour le graphique de performance des KPI
        Map<String, BigDecimal> kpiChart = new HashMap<>();
        kpiChart.put("CA Mensuel", kpis.chiffreAffairesMensuel());
        kpiChart.put("Objectif", kpis.objectifMensuel());
        kpiChart.put("Taux Conversion", kpis.tauxConversion());
        graphiques.put("kpis", kpiChart);
        
        return graphiques;
    }
}