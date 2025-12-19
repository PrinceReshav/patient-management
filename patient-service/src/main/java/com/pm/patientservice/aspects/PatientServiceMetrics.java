package com.pm.patientservice.aspects;

import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PatientServiceMetrics {

    private final MeterRegistry meterRegistry;

    public PatientServiceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // Below is setting to run before PatientService.getPatients(..)
    @Around("execution(* com.pm.patientservice.Service.PatientService.getPatients(..))")
    public Object monitorGetPatients(ProceedingJoinPoint joinPoint) throws Throwable {
        meterRegistry.counter("custom.redis.cache,miss", "cache", "patients") // Custom cache miss property
                .increment();
        Object result  = joinPoint.proceed(); // scrapes current state and lets us control that using object method
        return result;
    }

}
