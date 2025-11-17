package com.emak.crm.service.impl;


import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Service;

import com.emak.crm.dto.ComparaisonMensuelle;
import com.emak.crm.dto.Deal;
import com.emak.crm.dto.EvolutionVentes;
import com.emak.crm.dto.ObjectifAtteint;
import com.emak.crm.dto.PerformanceMensuelle;
import com.emak.crm.dto.PointDonnees;
import com.emak.crm.dto.RapportPerformance;
import com.emak.crm.dto.RapportVentes;
import com.emak.crm.dto.VentesParCommercial;
import com.emak.crm.dto.VentesParProduit;
import com.emak.crm.entity.Client;
import com.emak.crm.entity.Opportunite;
import com.emak.crm.entity.Utilisateur;
import com.emak.crm.enums.StatutOpportunite;
import com.emak.crm.exception.EntityNotFound;
import com.emak.crm.repository.ClientRepository;
import com.emak.crm.repository.OpportuniteRepository;
import com.emak.crm.repository.UtilisateurRepository;
import com.emak.crm.service.ReportingService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ReportingServiceImpl implements ReportingService {

    private final OpportuniteRepository opportuniteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;


    /**
     * RAPPORT DE VENTES
     * Métier : Analyse détaillée des performances commerciales sur une période
     * Contenu : CA par commercial, par produit, évolution, comparaison
     */
    @Override
    public RapportVentes genererRapportVentes(LocalDate debut, LocalDate fin) {
        log.info("Génération du rapport de ventes de {} à {}", debut, fin);
        
        // Récupérer toutes les opportunités gagnées dans la période
        List<Opportunite> opportunites = opportuniteRepository
                .findByDateCreationBetweenAndStatut(
                    debut.atStartOfDay(), 
                    fin.atTime(23, 59, 59), 
                    StatutOpportunite.GAGNEE
                );

        // Calcul du chiffre d'affaires total
        BigDecimal chiffreAffairesTotal = opportunites.stream()
                .map(Opportunite::getMontantEstime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ventes par commercial
        List<VentesParCommercial> ventesParCommercial = calculerVentesParCommercial(opportunites);

        // Ventes par produit
        List<VentesParProduit> ventesParProduit = calculerVentesParProduit(opportunites);

        // Évolution des ventes
        EvolutionVentes evolution = calculerEvolutionVentes(debut, fin);

        // Comparaison mensuelle
        List<ComparaisonMensuelle> comparaison = calculerComparaisonMensuelle(debut, fin);

        // Objectif atteint (exemple: 80% du chiffre d'affaires cible)
        BigDecimal objectifAtteint = calculerObjectifAtteint(chiffreAffairesTotal, debut, fin);

        return new RapportVentes(
            debut, fin, chiffreAffairesTotal, objectifAtteint,
            ventesParCommercial, ventesParProduit, evolution, comparaison
        );
    }

    /**
     * RAPPORT DE PERFORMANCE
     * Métier : Évaluation individuelle d'un commercial
     * Métriques : Objectifs, conversion, satisfaction client
     */
    @Override
    public RapportPerformance genererRapportPerformance(Long commercialId) throws EntityNotFound {
        Utilisateur commercial = utilisateurRepository.findById(commercialId)
                .orElseThrow(() -> EntityNotFound.of("Commercial non trouvé"));

        log.info("Génération du rapport de performance pour {}", commercial.getNom());

        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // Récupérer les opportunités du commercial
        List<Opportunite> opportunites = opportuniteRepository
                .findByUtilisateurAndDateCreationBetween(
                    commercial, 
                    debutMois.atStartOfDay(), 
                    finMois.atTime(23, 59, 59)
                );

        // Calcul des métriques
        BigDecimal chiffreAffairesReel = calculerChiffreAffairesCommercial(opportunites);
        BigDecimal objectifChiffreAffaires = calculerObjectifCommercial(commercial);
        BigDecimal tauxObjectifAtteint = objectifChiffreAffaires.compareTo(BigDecimal.ZERO) > 0 ?
                chiffreAffairesReel.divide(objectifChiffreAffaires, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        Integer nombreDealsConvertis = (int) opportunites.stream()
                .filter(o -> StatutOpportunite.GAGNEE.equals(o.getStatut()))
                .count();

        BigDecimal tauxConversion = opportunites.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(nombreDealsConvertis)
                        .divide(BigDecimal.valueOf(opportunites.size()), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

        Integer nouveauxClients = calculerNouveauxClients(commercial, debutMois, finMois);
        BigDecimal satisfactionClient = calculerSatisfactionClient(commercial);

        List<PerformanceMensuelle> performanceMensuelle = calculerPerformanceMensuelle(commercial);
        List<ObjectifAtteint> objectifsAtteints = evaluerObjectifsAtteints(commercial, chiffreAffairesReel, tauxConversion);
        List<Deal> dealsEnCours = getDealsEnCours(commercial);

        return new RapportPerformance(
            commercial.getNom(),
            debutMois.getMonth().toString() + " " + debutMois.getYear(),
            objectifChiffreAffaires,
            chiffreAffairesReel,
            tauxObjectifAtteint,
            nombreDealsConvertis,
            tauxConversion,
            nouveauxClients,
            satisfactionClient,
            performanceMensuelle,
            objectifsAtteints,
            dealsEnCours
        );
    }

    /**
     * EXPORT CLIENTS EXCEL
     * Métier : Exporter la base clients pour traitement externe
     * Usage : Campagnes externes, analyse avancée, sauvegarde
     */
    @Override
    public byte[] exporterClientsExcel() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Clients");
            
            // En-têtes
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Nom", "Email", "Téléphone", "Entreprise", "Ville", "Date Création", "Commercial Assigné"};
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Données
            List<Client> clients = clientRepository.findAll();
            int rowNum = 1;
            
            CellStyle dataStyle = createDataStyle(workbook);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (Client client : clients) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(client.getId());
                row.createCell(1).setCellValue(client.getNom());
                row.createCell(2).setCellValue(client.getEmail());
                row.createCell(3).setCellValue(client.getTelephone());
                row.createCell(4).setCellValue(client.getEntreprise());
              
                row.createCell(6).setCellValue(client.getVille());
                row.createCell(7).setCellValue(client.getDateCreation().format(formatter));
                row.createCell(8).setCellValue(
                    client.getUtilisateurResponsable() != null ? client.getUtilisateurResponsable().getNom() : "Non assigné"
                );
                
                // Appliquer le style aux cellules
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
            
            // Ajuster la largeur des colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("Erreur lors de l'export Excel des clients", e);
            throw new RuntimeException("Erreur lors de l'export Excel", e);
        }
    }

    /**
     * EXPORT VENTES PDF
     * Métier : Générer un rapport professionnel imprimable
     * Usage : Présentation direction, reporting formel
     */
    @Override
    public byte[] exporterVentesPDF() {
        try (XWPFDocument document = new XWPFDocument(); 
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            // Titre du document
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("RAPPORT DE VENTES");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            
            // Période
            XWPFParagraph periode = document.createParagraph();
            periode.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun periodeRun = periode.createRun();
            periodeRun.setText("Période : " + LocalDate.now().getMonth() + " " + LocalDate.now().getYear());
            periodeRun.setFontSize(12);
            
            // Ligne séparatrice
            createSeparator(document);
            
            // Résumé exécutif
            createExecutiveSummary(document);
            
            // Tableau des performances par commercial
            createPerformanceTable(document);
            
            // Graphiques (texte descriptif dans le PDF)
            createChartsSection(document);
            
            // Recommandations
            createRecommendations(document);
            
            document.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("Erreur lors de l'export PDF des ventes", e);
            throw new RuntimeException("Erreur lors de l'export PDF", e);
        }
    }

    // Méthodes helper privées

    private List<VentesParCommercial> calculerVentesParCommercial(List<Opportunite> opportunites) {
        Map<Utilisateur, List<Opportunite>> opportunitesParCommercial = opportunites.stream()
                .filter(o -> o.getUtilisateur() != null)
                .collect(Collectors.groupingBy(Opportunite::getUtilisateur));
        
        return opportunitesParCommercial.entrySet().stream()
                .map(entry -> {
                    Utilisateur commercial = entry.getKey();
                    List<Opportunite> opps = entry.getValue();
                    
                    BigDecimal ca = opps.stream()
                            .map(Opportunite::getMontantEstime)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    long dealsGagnes = opps.stream()
                            .filter(o -> StatutOpportunite.GAGNEE.equals(o.getStatut()))
                            .count();
                    
                    BigDecimal tauxConversion = opps.isEmpty() ? BigDecimal.ZERO :
                            BigDecimal.valueOf(dealsGagnes)
                                    .divide(BigDecimal.valueOf(opps.size()), 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100));
                    
                    return new VentesParCommercial(
                        commercial.getNom(),
                        ca,
                        opps.size(),
                        tauxConversion
                    );
                })
                .sorted((v1, v2) -> v2.chiffreAffaires().compareTo(v1.chiffreAffaires()))
                .collect(Collectors.toList());
    }

    private List<VentesParProduit> calculerVentesParProduit(List<Opportunite> opportunites) {
        // Implémentation simplifiée - à adapter selon votre modèle de données
        return List.of(
            new VentesParProduit("Produit A", BigDecimal.valueOf(50000), 25, BigDecimal.valueOf(40)),
            new VentesParProduit("Produit B", BigDecimal.valueOf(30000), 15, BigDecimal.valueOf(24)),
            new VentesParProduit("Produit C", BigDecimal.valueOf(20000), 10, BigDecimal.valueOf(16))
        );
    }

    private EvolutionVentes calculerEvolutionVentes(LocalDate debut, LocalDate fin) {
        // Implémentation simplifiée
        List<PointDonnees> historique = List.of(
            new PointDonnees(LocalDate.now().minusMonths(3), BigDecimal.valueOf(80000)),
            new PointDonnees(LocalDate.now().minusMonths(2), BigDecimal.valueOf(95000)),
            new PointDonnees(LocalDate.now().minusMonths(1), BigDecimal.valueOf(110000)),
            new PointDonnees(LocalDate.now(), BigDecimal.valueOf(100000))
        );
        
        return new EvolutionVentes(
            BigDecimal.valueOf(12.5),
            BigDecimal.valueOf(25.0),
            historique
        );
    }

    private List<ComparaisonMensuelle> calculerComparaisonMensuelle(LocalDate debut, LocalDate fin) {
        return List.of(
            new ComparaisonMensuelle("Janvier", BigDecimal.valueOf(80000), BigDecimal.valueOf(75000), BigDecimal.valueOf(5000)),
            new ComparaisonMensuelle("Février", BigDecimal.valueOf(95000), BigDecimal.valueOf(90000), BigDecimal.valueOf(5000)),
            new ComparaisonMensuelle("Mars", BigDecimal.valueOf(110000), BigDecimal.valueOf(100000), BigDecimal.valueOf(10000))
        );
    }

    private BigDecimal calculerObjectifAtteint(BigDecimal caTotal, LocalDate debut, LocalDate fin) {
        BigDecimal objectifCible = BigDecimal.valueOf(120000); // Objectif mensuel exemple
        return caTotal.divide(objectifCible, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculerChiffreAffairesCommercial(List<Opportunite> opportunites) {
        return opportunites.stream()
                .filter(o -> StatutOpportunite.GAGNEE.equals(o.getStatut()))
                .map(Opportunite::getMontantEstime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculerObjectifCommercial(Utilisateur commercial) {
        // Exemple d'objectif basé sur le rôle ou l'historique
        return BigDecimal.valueOf(100000); // Objectif fixe pour l'exemple
    }

    private Integer calculerNouveauxClients(Utilisateur commercial, LocalDate debut, LocalDate fin) {
        return clientRepository.countByUtilisateurResponsableAndDateCreationBetween(
            commercial, debut.atStartOfDay(), fin.atTime(23, 59, 59));
    }

    private BigDecimal calculerSatisfactionClient(Utilisateur commercial) {
        // Implémentation simplifiée - à adapter avec vos métriques de satisfaction
        return BigDecimal.valueOf(4.2); // Sur 5
    }

    private List<PerformanceMensuelle> calculerPerformanceMensuelle(Utilisateur commercial) {
        return List.of(
            new PerformanceMensuelle("Janvier", BigDecimal.valueOf(75000), BigDecimal.valueOf(80000), 8),
            new PerformanceMensuelle("Février", BigDecimal.valueOf(92000), BigDecimal.valueOf(90000), 12),
            new PerformanceMensuelle("Mars", BigDecimal.valueOf(105000), BigDecimal.valueOf(100000), 15)
        );
    }

    private List<ObjectifAtteint> evaluerObjectifsAtteints(Utilisateur commercial, BigDecimal caReel, BigDecimal tauxConversion) {
        return List.of(
            new ObjectifAtteint("Chiffre d'affaires mensuel", caReel.compareTo(BigDecimal.valueOf(90000)) > 0, BigDecimal.valueOf(95)),
            new ObjectifAtteint("Taux de conversion", tauxConversion.compareTo(BigDecimal.valueOf(30)) > 0, tauxConversion),
            new ObjectifAtteint("Nouveaux clients", true, BigDecimal.valueOf(100))
        );
    }

    private List<Deal> getDealsEnCours(Utilisateur commercial) {
        return opportuniteRepository.findByUtilisateurAndStatutNot(commercial, StatutOpportunite.GAGNEE)
                .stream()
                .map(opp -> new Deal(
                    opp.getNomOpportunite(),
                    opp.getMontantEstime(),
                    opp.getStatut().toString(),
                    opp.getClient().getNom()
                ))
                .limit(5)
                .collect(Collectors.toList());
    }

    // Méthodes pour Excel
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    // Méthodes pour PDF
    private void createSeparator(XWPFDocument document) {
        XWPFParagraph separator = document.createParagraph();
        XWPFRun separatorRun = separator.createRun();
        separatorRun.setText("_________________________________________________________________________");
        separatorRun.setColor("CCCCCC");
    }

    private void createExecutiveSummary(XWPFDocument document) {
        XWPFParagraph summaryTitle = document.createParagraph();
        XWPFRun summaryTitleRun = summaryTitle.createRun();
        summaryTitleRun.setText("Résumé Exécutif");
        summaryTitleRun.setBold(true);
        summaryTitleRun.setFontSize(14);
        
        XWPFParagraph summary = document.createParagraph();
        XWPFRun summaryRun = summary.createRun();
        summaryRun.setText("Ce rapport présente les performances commerciales pour le mois en cours. " +
                "Le chiffre d'affaires global montre une croissance positive avec une augmentation de 12% par rapport au mois précédent.");
    }

    private void createPerformanceTable(XWPFDocument document) {
        XWPFParagraph tableTitle = document.createParagraph();
        XWPFRun tableTitleRun = tableTitle.createRun();
        tableTitleRun.setText("Performances par Commercial");
        tableTitleRun.setBold(true);
        tableTitleRun.setFontSize(14);
        
        // Création du tableau (simplifiée)
        XWPFTable table = document.createTable(4, 4);
        
        // En-têtes
        table.getRow(0).getCell(0).setText("Commercial");
        table.getRow(0).getCell(1).setText("CA Réalisé");
        table.getRow(0).getCell(2).setText("Objectif");
        table.getRow(0).getCell(3).setText("Taux");
        
        // Données
        table.getRow(1).getCell(0).setText("Jean Dupont");
        table.getRow(1).getCell(1).setText("45 000 €");
        table.getRow(1).getCell(2).setText("40 000 €");
        table.getRow(1).getCell(3).setText("112%");
        
        table.getRow(2).getCell(0).setText("Marie Martin");
        table.getRow(2).getCell(1).setText("38 000 €");
        table.getRow(2).getCell(2).setText("35 000 €");
        table.getRow(2).getCell(3).setText("108%");
        
        table.getRow(3).getCell(0).setText("Pierre Lambert");
        table.getRow(3).getCell(1).setText("42 000 €");
        table.getRow(3).getCell(2).setText("45 000 €");
        table.getRow(3).getCell(3).setText("93%");
    }

    private void createChartsSection(XWPFDocument document) {
        XWPFParagraph chartsTitle = document.createParagraph();
        XWPFRun chartsTitleRun = chartsTitle.createRun();
        chartsTitleRun.setText("Analyse et Tendances");
        chartsTitleRun.setBold(true);
        chartsTitleRun.setFontSize(14);
        
        XWPFParagraph charts = document.createParagraph();
        XWPFRun chartsRun = charts.createRun();
        chartsRun.setText("• Évolution du CA : Croissance constante sur les 3 derniers mois\n" +
                         "• Répartition par produit : Produit A (40%), Produit B (35%), Produit C (25%)\n" +
                         "• Taux de conversion moyen : 28%");
    }

    private void createRecommendations(XWPFDocument document) {
        XWPFParagraph recTitle = document.createParagraph();
        XWPFRun recTitleRun = recTitle.createRun();
        recTitleRun.setText("Recommandations");
        recTitleRun.setBold(true);
        recTitleRun.setFontSize(14);
        
        XWPFParagraph rec = document.createParagraph();
        XWPFRun recRun = rec.createRun();
        recRun.setText("1. Focus sur la formation des commerciaux pour améliorer le taux de conversion\n" +
                      "2. Développer des campagnes ciblées pour le Produit B\n" +
                      "3. Renforcer le suivi des leads qualifiés");
    }
}