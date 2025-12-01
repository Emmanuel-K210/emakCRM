package com.emak.crm.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emak.crm.dto.ClientResponse;
import com.emak.crm.dto.DevisResponse;
import com.emak.crm.service.ClientService;
import com.emak.crm.service.DevisService;

import lombok.AllArgsConstructor;


@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class RefApiGeneral {
	
	private final ClientService clientService;
	private final DevisService devisService;
	
	@GetMapping("/clients/all")
	public ResponseEntity<List<ClientResponse>> clients() {
		
		return ResponseEntity.ok(clientService.findAll());
	}

	
	@GetMapping("/devis/convertibles")
	public ResponseEntity<List<DevisResponse>> devisConvertibles() {
		return ResponseEntity.ok(devisService.findByDevisConvertible());
	}
	
	
}


