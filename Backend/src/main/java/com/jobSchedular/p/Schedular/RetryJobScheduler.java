package com.jobSchedular.p.Schedular;

import com.jobSchedular.p.Entities.Job;
import com.jobSchedular.p.Enums.JobSchedulerEnum;
import com.jobSchedular.p.Repo.JobRepository;
import com.jobSchedular.p.SSE.SseEmitterService;
import com.jobSchedular.p.Service.ServiceImpl.JobServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class RetryJobScheduler {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SseEmitterService sseEmitterService;

    @Autowired
    private  KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "job-events";

    @Scheduled(fixedRate = 5000)
    public void retryFailedJobs(){
        List<Job> jobsToRetry = jobRepository.findByStatusAndNextAttemptAtBefore(JobSchedulerEnum.RETRYING, Instant.now());

        for(Job job:jobsToRetry){
            kafkaTemplate.send(TOPIC, job.getId());
            job.setNextAttemptAt(null);
            jobRepository.save(job);
            sseEmitterService.broadcast(job);
        }
    }

}
