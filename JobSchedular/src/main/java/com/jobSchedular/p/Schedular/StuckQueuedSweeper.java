package com.jobSchedular.p.Schedular;

import com.jobSchedular.p.Entities.Job;
import com.jobSchedular.p.Enums.JobSchedulerEnum;
import com.jobSchedular.p.Repo.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StuckQueuedSweeper {

    private final JobRepository jobRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "job-events";
    private static final long STUCK_THRESHOLD_SECONDS = 30;

    @Scheduled(fixedRate = 30000)
    public void requeueStuckJobs() {
        Instant cutoff = Instant.now().minusSeconds(STUCK_THRESHOLD_SECONDS);

        List<Job> stuckJobs = jobRepository.findByStatusAndCreatedAtBefore(
                JobSchedulerEnum.QUEUED, cutoff);

        for (Job job : stuckJobs) {
            System.out.println("Re-publishing stuck QUEUED job: " + job.getId());
            kafkaTemplate.send(TOPIC, job.getId());
        }
    }
}