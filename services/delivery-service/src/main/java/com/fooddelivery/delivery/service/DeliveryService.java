package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.dto.DeliveryDto;
import com.fooddelivery.delivery.entity.Delivery;
import com.fooddelivery.delivery.entity.DeliveryStatus;
import com.fooddelivery.delivery.event.DeliveryEvent;
import com.fooddelivery.delivery.event.PaymentEvent;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final DeliveryRepository deliveryRepository;
    private final KafkaTemplate<String, DeliveryEvent> kafkaTemplate;

    public DeliveryService(DeliveryRepository deliveryRepository, KafkaTemplate<String, DeliveryEvent> kafkaTemplate) {
        this.deliveryRepository = deliveryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String DELIVERY_EVENTS_TOPIC = "delivery-events";

    @Transactional
    public DeliveryDto createDelivery(PaymentEvent paymentEvent) {
        log.info("Creating delivery for orderId={}", paymentEvent.getOrderId());

        Delivery delivery = new Delivery();
        delivery.setOrderId(paymentEvent.getOrderId());
        delivery.setDeliveryAddress(paymentEvent.getDeliveryAddress());
        delivery.setDeliveryAgentId(null);
        delivery.setStatus(DeliveryStatus.PENDING);

        delivery = deliveryRepository.save(delivery);
        log.info("Delivery assigned: deliveryId={}, orderId={}, agentId={}",
                delivery.getId(), delivery.getOrderId(), delivery.getDeliveryAgentId());

        DeliveryEvent event = new DeliveryEvent();
        event.setDeliveryId(delivery.getId());
        event.setOrderId(delivery.getOrderId());
        event.setDeliveryAgentId(delivery.getDeliveryAgentId());
        event.setStatus(delivery.getStatus().name());
        event.setDeliveryAddress(delivery.getDeliveryAddress());
        event.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send(DELIVERY_EVENTS_TOPIC, String.valueOf(delivery.getOrderId()), event);

        return mapToDto(delivery);
    }

    public DeliveryDto getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId).orElse(null);

        if (delivery == null) {
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

    public List<DeliveryDto> getPendingDeliveries() {
        return deliveryRepository.findByStatus(DeliveryStatus.PENDING).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliveryDto assignAgentToDelivery(Long deliveryId, Long agentId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));
        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new RuntimeException("Delivery is already assigned or completed");
        }
        delivery.setDeliveryAgentId(agentId);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery = deliveryRepository.save(delivery);

        DeliveryEvent event = new DeliveryEvent();
        event.setDeliveryId(delivery.getId());
        event.setOrderId(delivery.getOrderId());
        event.setDeliveryAgentId(delivery.getDeliveryAgentId());
        event.setStatus(delivery.getStatus().name());
        event.setDeliveryAddress(delivery.getDeliveryAddress());
        event.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send(DELIVERY_EVENTS_TOPIC, String.valueOf(delivery.getOrderId()), event);

        return mapToDto(delivery);
    }

    @Transactional
    public DeliveryDto updateDeliveryStatus(Long deliveryId, String status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        delivery.setStatus(DeliveryStatus.valueOf(status));
        delivery = deliveryRepository.save(delivery);
        log.info("Delivery status updated: deliveryId={}, status={}", deliveryId, status);

        DeliveryEvent event = new DeliveryEvent();
        event.setDeliveryId(delivery.getId());
        event.setOrderId(delivery.getOrderId());
        event.setDeliveryAgentId(delivery.getDeliveryAgentId());
        event.setStatus(delivery.getStatus().name());
        event.setDeliveryAddress(delivery.getDeliveryAddress());
        event.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send(DELIVERY_EVENTS_TOPIC, String.valueOf(delivery.getOrderId()), event);

        return mapToDto(delivery);
    }

    private DeliveryDto mapToDto(Delivery d) {
        DeliveryDto res = new DeliveryDto();
        res.setId(d.getId());
        res.setOrderId(d.getOrderId());
        res.setDeliveryAgentId(d.getDeliveryAgentId());
        res.setDeliveryAddress(d.getDeliveryAddress());
        res.setStatus(d.getStatus().name());
        res.setCreatedAt(d.getCreatedAt());
        res.setUpdatedAt(d.getUpdatedAt());
        return res;
    }
}
