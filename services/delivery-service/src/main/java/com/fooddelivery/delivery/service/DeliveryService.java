package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.dto.DeliveryDto;
import com.fooddelivery.delivery.entity.Delivery;
import com.fooddelivery.delivery.entity.DeliveryStatus;
import com.fooddelivery.delivery.event.DeliveryEvent;
import com.fooddelivery.delivery.event.PaymentEvent;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final KafkaTemplate<String, DeliveryEvent> kafkaTemplate;

    private static final String DELIVERY_EVENTS_TOPIC = "delivery-events";

    /**
     * Create delivery record when payment is completed.
     * In production, this would query user-service for available delivery agents.
     * For demo, we auto-assign agent ID = 1.
     */
    @Transactional
    public DeliveryDto createDelivery(PaymentEvent paymentEvent) {
        log.info("Creating delivery for orderId={}", paymentEvent.getOrderId());

        Delivery delivery = Delivery.builder()
                .orderId(paymentEvent.getOrderId())
                .deliveryAddress(paymentEvent.getDeliveryAddress())
                .deliveryAgentId(1L)  // Simulated auto-assignment
                .status(DeliveryStatus.ASSIGNED)
                .build();

        delivery = deliveryRepository.save(delivery);
        log.info("Delivery assigned: deliveryId={}, orderId={}, agentId={}",
                delivery.getId(), delivery.getOrderId(), delivery.getDeliveryAgentId());

        // Publish delivery event
        DeliveryEvent event = DeliveryEvent.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryAgentId(delivery.getDeliveryAgentId())
                .status(delivery.getStatus().name())
                .deliveryAddress(delivery.getDeliveryAddress())
                .timestamp(LocalDateTime.now())
                .build();
        kafkaTemplate.send(DELIVERY_EVENTS_TOPIC, String.valueOf(delivery.getOrderId()), event);

        return mapToDto(delivery);
    }

    public DeliveryDto getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId).orElse(null);
        
        if (delivery == null) {
            // Return null instead of throwing exception - controller will handle it
            log.info("Delivery not found for orderId={}", orderId);
            return null;
        }
        
        return mapToDto(delivery);
    }

    public List<DeliveryDto> getDeliveriesByAgent(Long agentId) {
        return deliveryRepository.findByDeliveryAgentId(agentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update delivery status (delivery agent updates).
     */
    @Transactional
    public DeliveryDto updateDeliveryStatus(Long deliveryId, String status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        delivery.setStatus(DeliveryStatus.valueOf(status));
        delivery = deliveryRepository.save(delivery);
        log.info("Delivery status updated: deliveryId={}, status={}", deliveryId, status);

        // Publish status update event
        DeliveryEvent event = DeliveryEvent.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryAgentId(delivery.getDeliveryAgentId())
                .status(delivery.getStatus().name())
                .deliveryAddress(delivery.getDeliveryAddress())
                .timestamp(LocalDateTime.now())
                .build();
        kafkaTemplate.send(DELIVERY_EVENTS_TOPIC, String.valueOf(delivery.getOrderId()), event);

        return mapToDto(delivery);
    }

    private DeliveryDto mapToDto(Delivery d) {
        return DeliveryDto.builder()
                .id(d.getId())
                .orderId(d.getOrderId())
                .deliveryAgentId(d.getDeliveryAgentId())
                .deliveryAddress(d.getDeliveryAddress())
                .status(d.getStatus().name())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
