package com.increff.pos.util;

import com.increff.pos.db.CounterPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SequenceGeneratorTest {

    @Mock
    private MongoOperations mongoOperations;

    @InjectMocks
    private SequenceGenerator sequenceGenerator;

    @Test
    void testGetNextSequence_FirstCall() {
        // Given
        String key = "order";
        CounterPojo counter = new CounterPojo();
        counter.setId(key);
        counter.setSequence(1L);

        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(CounterPojo.class))).thenReturn(counter);

        // When
        long result = sequenceGenerator.getNextSequence(key);

        // Then
        assertEquals(1L, result);
        verify(mongoOperations).findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(CounterPojo.class));
    }

    @Test
    void testGetNextSequence_SubsequentCall() {
        // Given
        String key = "invoice";
        CounterPojo counter = new CounterPojo();
        counter.setId(key);
        counter.setSequence(42L);

        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(CounterPojo.class))).thenReturn(counter);

        // When
        long result = sequenceGenerator.getNextSequence(key);

        // Then
        assertEquals(42L, result);
    }

    @Test
    void testGetNextSequence_DifferentKeys() {
        // Given
        CounterPojo orderCounter = new CounterPojo();
        orderCounter.setId("order");
        orderCounter.setSequence(10L);

        CounterPojo invoiceCounter = new CounterPojo();
        invoiceCounter.setId("invoice");
        invoiceCounter.setSequence(5L);

        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(CounterPojo.class))).thenReturn(orderCounter, invoiceCounter);

        // When
        long orderSeq = sequenceGenerator.getNextSequence("order");
        long invoiceSeq = sequenceGenerator.getNextSequence("invoice");

        // Then
        assertEquals(10L, orderSeq);
        assertEquals(5L, invoiceSeq);
        verify(mongoOperations, times(2)).findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(CounterPojo.class));
    }
}
