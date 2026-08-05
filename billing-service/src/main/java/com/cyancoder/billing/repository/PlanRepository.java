package com.cyancoder.billing.repository;
import com.cyancoder.billing.model.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PlanRepository extends JpaRepository<PlanEntity, String> { List<PlanEntity> findByActiveTrueOrderByDisplayNameAsc(); }
