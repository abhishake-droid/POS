package com.increff.pos.scheduler;

import com.increff.pos.dto.DailySalesDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailySalesSchedulerTest {

    @Mock
    private Environment environment;

    @Mock
    private DailySalesDto dailySalesDto;

    @InjectMocks
    private DailySalesScheduler scheduler;

    @BeforeEach
    void setUp() {
        // Mocks are automatically injected via @InjectMocks
    }

    @Test
    void testAggregateDailySales_WhenEnabled() {
        // Given
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);
        when(environment.getActiveProfiles()).thenReturn(new String[] {});

        // When
        scheduler.aggregateDailySales();

        // Then
        verify(dailySalesDto, times(1)).aggregateDailySales();
    }

    @Test
    void testAggregateDailySales_WhenDisabled() {
        // Given
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", false);

        // When
        scheduler.aggregateDailySales();

        // Then
        verify(dailySalesDto, never()).aggregateDailySales();
    }

    @Test
    void testAggregateDailySales_InTestEnvironment() {
        // Given
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);
        when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

        // When
        scheduler.aggregateDailySales();

        // Then
        verify(dailySalesDto, never()).aggregateDailySales();
    }

    @Test
    void testAggregateDailySales_InProductionEnvironment() {
        // Given
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);
        when(environment.getActiveProfiles()).thenReturn(new String[] { "prod" });

        // When
        scheduler.aggregateDailySales();

        // Then
        verify(dailySalesDto, times(1)).aggregateDailySales();
    }

    @Test
    void testAggregateDailySales_NoActiveProfiles() {
        // Given
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);
        when(environment.getActiveProfiles()).thenReturn(new String[] {});

        // When
        scheduler.aggregateDailySales();

        // Then
        verify(dailySalesDto, times(1)).aggregateDailySales();
    }
}
