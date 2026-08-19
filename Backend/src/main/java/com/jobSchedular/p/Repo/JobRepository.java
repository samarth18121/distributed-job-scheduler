package com.jobSchedular.p.Repo;

import com.jobSchedular.p.Entities.Job;
import com.jobSchedular.p.Enums.JobSchedulerEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;


public interface JobRepository extends JpaRepository<Job,String> {

    List<Job> findByStatusAndNextAttemptAtBefore(JobSchedulerEnum status, Instant time);
    List<Job> findByStatusAndCreatedAtBefore(JobSchedulerEnum status, Instant time);
}
