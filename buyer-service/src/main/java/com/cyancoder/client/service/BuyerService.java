package com.cyancoder.client.service;

import com.cyancoder.client.entity.BuyerEntity;
import com.cyancoder.client.model.BuyerModel;
import com.cyancoder.client.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Buyer persistence without the command/event indirection. The aggregate path
 * this replaces never actually populated a command — CreateBuyerCommand was
 * built with every field commented out — so callers got an empty buyer back.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BuyerService {
    private final BuyerRepository buyerRepository;

    public List<BuyerModel> findAll() {
        return buyerRepository.findAll().stream().map(BuyerModel::new).toList();
    }

    public BuyerModel findById(String buyerId) {
        return buyerRepository.findById(buyerId).map(BuyerModel::new).orElse(null);
    }

    /**
     * Upserts by buyerId and returns the id, which is what the caller needs in
     * order to reference the buyer afterwards.
     */
    public String addOrEdit(BuyerModel request) {
        String buyerId = request.getBuyerId() == null || request.getBuyerId().isBlank()
                ? UUID.randomUUID().toString()
                : request.getBuyerId();
        BuyerEntity entity = buyerRepository.findById(buyerId).orElseGet(BuyerEntity::new);
        BeanUtils.copyProperties(request, entity);
        entity.setBuyerId(buyerId);
        buyerRepository.save(entity);
        log.info("buyer saved: {}", buyerId);
        return buyerId;
    }
}
