package com.jobSchedular.p.Service;

import com.jobSchedular.p.DTO.CreateJobRequest;
import com.jobSchedular.p.Entities.Job;
import com.jobSchedular.p.Repo.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


public interface JobService {

    public Job createJob(CreateJobRequest jobRequest);


}
