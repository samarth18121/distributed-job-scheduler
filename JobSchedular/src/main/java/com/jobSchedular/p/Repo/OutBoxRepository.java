package com.jobSchedular.p.Repo;

import com.jobSchedular.p.Entities.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutBoxRepository extends JpaRepository<Outbox,String> {

     List<Outbox> findByPublishedFalse();
}
