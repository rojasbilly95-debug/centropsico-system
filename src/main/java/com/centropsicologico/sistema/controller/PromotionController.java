package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.entity.Promotion;
import com.centropsicologico.sistema.service.PromotionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    public Promotion save(@RequestBody Promotion promotion) {
        return promotionService.save(promotion);
    }

    @PutMapping("/{id}")
    public Promotion update(
            @PathVariable Long id,
            @RequestBody Promotion promotion
    ) {
        return promotionService.update(id, promotion);
    }

    @PutMapping("/{id}/toggle")
    public Promotion toggleActive(@PathVariable Long id) {
        return promotionService.toggleActive(id);
    }

    @GetMapping
    public List<Promotion> findAll() {
        return promotionService.findAll();
    }
}