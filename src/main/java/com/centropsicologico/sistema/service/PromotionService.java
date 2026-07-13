package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.Promotion;

import java.util.List;

public interface PromotionService {

    Promotion save(Promotion promotion);

    Promotion update(Long id, Promotion promotion);

    Promotion toggleActive(Long id);

    List<Promotion> findAll();

    List<Promotion> findPublicActivePromotions();
}