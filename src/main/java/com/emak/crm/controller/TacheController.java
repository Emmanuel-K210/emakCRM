package com.emak.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emak.crm.service.TacheService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/taches")
public class TacheController {
	private final TacheService tacheService;

	@GetMapping
	public String taches() {
		
		return "pages/taches/taches.html";
	}
	
	@GetMapping
	public String taches(@RequestParam(name ="clientId") Long clientId) {
		
		return "pages/taches/taches.html";
	}
}
