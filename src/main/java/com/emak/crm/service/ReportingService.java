package com.emak.crm.service;

import com.emak.crm.dto.RapportPerformance;
import com.emak.crm.dto.RapportVentes;
import com.emak.crm.exception.EntityNotFound;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
@Service
public interface ReportingService {
    RapportVentes genererRapportVentes(LocalDate debut, LocalDate fin);
    RapportPerformance genererRapportPerformance(Long commercialId) throws EntityNotFound;
    byte[] exporterClientsExcel();
    byte[] exporterVentesPDF();
}