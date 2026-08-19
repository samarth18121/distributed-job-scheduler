package com.jobSchedular.p.Controller;

import com.jobSchedular.p.DTO.CreateJobRequest;
import com.jobSchedular.p.Entities.Job;
import com.jobSchedular.p.Repo.JobRepository;
import com.jobSchedular.p.SSE.SseEmitterService;
import com.jobSchedular.p.Service.JobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class MainController {
    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SseEmitterService sseEmitterService;

    @PostMapping
    public ResponseEntity<Job> scheduleTheJob(@Valid @RequestBody CreateJobRequest jobRequest){
        Job savedJob = jobService.createJob(jobRequest);
        return new ResponseEntity<>(savedJob, HttpStatus.CREATED);
    }

    @GetMapping("/stream")
    public SseEmitter streamJobs() {
        return sseEmitterService.subscribe();
    }

    @GetMapping
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }
}