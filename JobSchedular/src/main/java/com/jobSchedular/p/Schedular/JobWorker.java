package com.jobSchedular.p.Schedular;

import com.jobSchedular.p.Entities.Job;
import com.jobSchedular.p.Repo.JobRepository;
import com.jobSchedular.p.SSE.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.jobSchedular.p.Enums.JobSchedulerEnum.*;

@Component
@RequiredArgsConstructor
public class JobWorker {

    @Autowired
    private  JobRepository jobRepository;

    @Autowired
    private SseEmitterService sseEmitterService;

    private static final long BASE_DELAY_SECONDS = 2;

    @KafkaListener(topics = "job-events", groupId = "job-workers")
    public void consumeJob(String jobId) {
        Optional<Job> doesJobExists = jobRepository.findById(jobId);
        if(doesJobExists.isEmpty()){
            System.out.println("This job does not exists");
            return;
        }
        Job job=doesJobExists.get();
        if (job.getStatus().equals(DONE) || job.getStatus().equals(DEAD_LETTERED)){
            System.out.println("Job has been resolved");
            return;
        }

        job.setStatus(RUNNING);
        jobRepository.save(job);


        try{
            //execute Job
            executeJob(job);
            job.setStatus(DONE);
        }catch (Exception e){
            System.out.println("Job "+jobId+" got failed");
            job.setFailureCount(job.getFailureCount()+1);
            if(job.getFailureCount()<job.getMaxAttempts()) {
                job.setStatus(RETRYING);
                long delaySeconds = BASE_DELAY_SECONDS * (long) Math.pow(2, job.getFailureCount());
                job.setNextAttemptAt(Instant.now().plusSeconds(delaySeconds));
            }
            else {
                job.setStatus(DEAD_LETTERED);
            }


            job.setLastError(e.getMessage());
        }
        jobRepository.save(job);

        sseEmitterService.broadcast(job);
        // 1. look up the job by jobId
        // 2. idempotency check — if already DONE, just return, do nothing
        // 3. mark status = RUNNING, save
        // 4. try to "execute" the job (fake logic — sometimes throws, sometimes succeeds)
        // 5. on success: status = DONE, save
        // 6. on failure: failureCount++, decide RETRYING vs DEAD_LETTERED, save
    }

    private void executeJob(Job job) throws Exception {
        // fake "sometimes fails" logic for demo purposes
        if (ThreadLocalRandom.current().nextInt(100) < 50) {
            throw new Exception("Simulated random failure for job " + job.getId());
        }
        System.out.println("Job " + job.getId() + " executed successfully");
    }

}
