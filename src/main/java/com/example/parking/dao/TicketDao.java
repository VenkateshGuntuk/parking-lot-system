package com.example.parking.dao;

import com.example.parking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketDao extends JpaRepository<Ticket, Long> {
	@Query("select t from Ticket t where t.vehicle.plateNo = :plate and t.status in ('ACTIVE','PAYMENT_PENDING')")
	List<Ticket> findActiveByPlate(@Param("plate") String plateNo);
}
