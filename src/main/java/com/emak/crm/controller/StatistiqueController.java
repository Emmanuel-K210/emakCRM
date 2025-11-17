package com.emak.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import groovy.util.logging.Slf4j;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@Slf4j
public class StatistiqueController {

	
	@GetMapping(path="/statistiques")
	public String statistiques(Model model) {
		model.addAttribute("pageActive","statistiques");
		
		return "pages/statistiques/statistiques.html";
	}
}
