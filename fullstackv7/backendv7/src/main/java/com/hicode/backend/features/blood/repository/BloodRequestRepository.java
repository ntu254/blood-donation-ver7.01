package com.hicode.backend.features.blood.repository;

import com.hicode.backend.model.entity.BloodRequest;
import com.hicode.backend.model.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    // Lấy tất cả yêu cầu với các thông tin liên quan (fetch join)
    @Query("SELECT br FROM BloodRequest br JOIN FETCH br.bloodType JOIN FETCH br.createdBy")
    List<BloodRequest> findAllWithDetails();

    // Tìm các yêu cầu đang hoạt động với các thông tin liên quan
    @Query("SELECT br FROM BloodRequest br JOIN FETCH br.bloodType JOIN FETCH br.createdBy WHERE br.status = :status")
    List<BloodRequest> findByStatusWithDetails(@Param("status") RequestStatus status);

    @Override
    @Query(value = "SELECT br FROM BloodRequest br JOIN FETCH br.bloodType JOIN FETCH br.createdBy",
            countQuery = "SELECT count(br) FROM BloodRequest br")
    Page<BloodRequest> findAll(Pageable pageable);
}