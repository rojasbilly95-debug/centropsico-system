package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Promotion;
import com.centropsicologico.sistema.exception.BusinessRuleException;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.PromotionRepository;
import com.centropsicologico.sistema.service.AuditLogService;
import com.centropsicologico.sistema.service.PromotionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final AuditLogService auditLogService;

    public PromotionServiceImpl(
            PromotionRepository promotionRepository,
            AuditLogService auditLogService
    ) {
        this.promotionRepository = promotionRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public Promotion save(Promotion promotion) {
        validatePromotion(promotion);

        promotion.setTitle(promotion.getTitle().trim());
        promotion.setDescription(promotion.getDescription().trim());

        if (promotion.getActive() == null) {
            promotion.setActive(true);
        }

        Promotion saved = promotionRepository.save(promotion);

        auditLogService.record(
                "PROMOCIONES",
                "REGISTRO DE PROMOCIÓN",
                "Promotion",
                saved.getId(),
                "Se registró la promoción: " + saved.getTitle()
        );

        return saved;
    }

    @Override
    public Promotion update(Long id, Promotion promotion) {
        Promotion existing = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));

        validatePromotion(promotion);

        existing.setTitle(promotion.getTitle().trim());
        existing.setDescription(promotion.getDescription().trim());
        existing.setDiscountPercent(promotion.getDiscountPercent());
        existing.setStartDate(promotion.getStartDate());
        existing.setEndDate(promotion.getEndDate());

        Promotion updated = promotionRepository.save(existing);

        auditLogService.record(
                "PROMOCIONES",
                "ACTUALIZACIÓN DE PROMOCIÓN",
                "Promotion",
                updated.getId(),
                "Se actualizó la promoción: " + updated.getTitle()
        );

        return updated;
    }

    @Override
    public Promotion toggleActive(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));

        promotion.setActive(!Boolean.TRUE.equals(promotion.getActive()));

        Promotion updated = promotionRepository.save(promotion);

        auditLogService.record(
                "PROMOCIONES",
                "CAMBIO DE ESTADO DE PROMOCIÓN",
                "Promotion",
                updated.getId(),
                "La promoción "
                        + updated.getTitle()
                        + " cambió a estado "
                        + (Boolean.TRUE.equals(updated.getActive()) ? "ACTIVO" : "INACTIVO")
        );

        return updated;
    }

    @Override
    public List<Promotion> findAll() {
        return promotionRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<Promotion> findPublicActivePromotions() {
        return promotionRepository.findPublicActivePromotions(LocalDate.now());
    }

    private void validatePromotion(Promotion promotion) {
        if (promotion == null) {
            throw new BusinessRuleException("Los datos de la promoción son obligatorios");
        }

        if (promotion.getTitle() == null || promotion.getTitle().trim().isEmpty()) {
            throw new BusinessRuleException("El título de la promoción es obligatorio");
        }

        if (promotion.getDescription() == null || promotion.getDescription().trim().isEmpty()) {
            throw new BusinessRuleException("La descripción de la promoción es obligatoria");
        }

        if (promotion.getDiscountPercent() != null
                && (promotion.getDiscountPercent() < 0 || promotion.getDiscountPercent() > 100)) {
            throw new BusinessRuleException("El porcentaje de descuento debe estar entre 0 y 100");
        }

        if (promotion.getStartDate() != null
                && promotion.getEndDate() != null
                && promotion.getEndDate().isBefore(promotion.getStartDate())) {
            throw new BusinessRuleException("La fecha fin no puede ser menor a la fecha inicio");
        }
    }
}