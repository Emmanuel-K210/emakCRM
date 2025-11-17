package com.emak.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.emak.crm.entity.Envoi;

@Repository
public interface EnvoiRepository extends JpaRepository<Envoi, Long> {

}
