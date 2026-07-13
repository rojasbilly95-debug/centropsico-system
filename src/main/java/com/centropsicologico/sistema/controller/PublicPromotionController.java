package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.entity.Promotion;
import com.centropsicologico.sistema.service.PromotionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/promotions")
public class PublicPromotionController {

    private final PromotionService promotionService;

    public PublicPromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public List<Promotion> findPublicPromotions() {
        return promotionService.findPublicActivePromotions();
    }
}