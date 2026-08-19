package com.jobSchedular.p.Service.ServiceImpl;


import com.jobSchedular.p.DTO.CreateJobRequest;
import com.jobSchedular.p.Entities.Job;
import com.jobSchedular.p.Entities.Outbox;
import com.jobSchedular.p.Enums.JobSchedulerEnum;
import com.jobSchedular.p.Repo.JobRepository;
import com.jobSchedular.p.Repo.OutBoxRepository;
import com.jobSchedular.p.SSE.SseEmitterService;
import com.jobSchedular.p.Service.JobService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobServiceImpl implements JobService {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private OutBoxRepository outBoxRepository;

    @Autowired
    private SseEmitterService sseEmitterService;

    @Transactional
    @Override
    public Job createJob(CreateJobRequest createJobRequest) {
        Job job = Job.builder()
                .jobType(createJobRequest.getJobType())
                .payload(createJobRequest.getPayload())
                .maxAttempts(createJobRequest.getMaxAttempts())
                .status(JobSchedulerEnum.QUEUED)
                .failureCount(0)
                .build();

        Job savedJob = jobRepository.save(job);

        Outbox outbox = Outbox.builder().
                jobId(savedJob.getId()).
                published(false)
                .build();

        outBoxRepository.save(outbox);

        sseEmitterService.broadcast(savedJob);
        return savedJob;
    }
}
