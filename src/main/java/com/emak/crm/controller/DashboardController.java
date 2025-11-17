package com.emak.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.emak.crm.service.DashboardService;

import groovy.util.logging.Slf4j;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@Slf4j
public class DashboardController {
	private final DashboardService dashboardService;

	@GetMapping(path = "/index")
	public String home(Model model) {
		model.addAttribute("pageActive", "dashboard");

        model.addAttribute("totalClients", dashboardService.totalClients());
        model.addAttribute("totalVentes", dashboardService.totalVentes());
        model.addAttribute("montantFactures", dashboardService.montantFactures());
        model.addAttribute("facturesNonPayees", dashboardService.facturesNonPayees());
        model.addAttribute("totalOpportunites", dashboardService.totalOpportunites());

        // Pour les charts, tu peux passer des listes de valeurs
        model.addAttribute("ventesParMois", dashboardService.ventesParMois());
        model.addAttribute("opportunitesParEtape", dashboardService.opportunitesParEtape());
		return "pages/index.html";
	}

	@GetMapping("/")
	public String redirectHome() {

		return "redirect:/index";
	}
}
