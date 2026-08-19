package com.jobSchedular.p.Schedular;

import com.jobSchedular.p.Entities.Outbox;
import com.jobSchedular.p.Repo.OutBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutBoxPublisher {

    private final OutBoxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;


    private static final String TOPIC = "job-events";


    @Scheduled(fixedRate = 55000)
    public void publishUnpublishedOutboxEvents() {
        List<Outbox> unpublishedEvents = outboxRepository.findByPublishedFalse();

        for (Outbox outbox : unpublishedEvents) {
            kafkaTemplate.send(TOPIC, outbox.getJobId());
            outbox.setPublished(true);
            outboxRepository.save(outbox);
        }
    }


}
