package com.freelancer.portal.monitoring;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * AspectJ component for monitoring the performance of critical methods in the application.
 * This aspect tracks execution time of methods and reports metrics to Micrometer.
 */
@Aspect
@Component
public class PerformanceMonitoringAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);
    private static final String TAG_METHOD = "method";
    private static final String TAG_CLASS = "class";
    private static final String TAG_SERVICE = "service";
    private static final String TAG_EXCEPTION = "exception";
    
    private final MeterRegistry meterRegistry;

    public PerformanceMonitoringAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Around advice that measures execution time for methods annotated with @Timed
     */
    @Around("@annotation(io.micrometer.core.annotation.Timed)")
    public Object measureMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Get the @Timed annotation
        Timed timed = method.getAnnotation(Timed.class);
        String metricName = timed.value().isEmpty() ? 
                "method.execution.time" : timed.value();
        
        // Get full method name for logging
        String methodName = signature.getDeclaringType().getSimpleName() + "." + method.getName();
        
        // Record start time
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute the method
            return joinPoint.proceed();
        } catch (Exception ex) {
            // Record metrics for exceptions
            Counter.builder(metricName + ".exceptions")
                    .tag(TAG_METHOD, methodName)
                    .tag(TAG_EXCEPTION, ex.getClass().getSimpleName())
                    .description("Method execution exceptions")
                    .register(meterRegistry)
                    .increment();
            
            // Re-throw the exception
            throw ex;
        } finally {
            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Record the metrics
            Timer.builder(metricName)
                    .tag(TAG_METHOD, methodName)
                    .tag(TAG_CLASS, signature.getDeclaringTypeName())
                    .description("Method execution time")
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);
            
            // Log the execution time if it exceeds thresholds
            if (executionTime > 1000) {
                logger.warn("Slow method execution: {} took {}ms", methodName, executionTime);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Method {} executed in {}ms", methodName, executionTime);
            }
        }
    }

    /**
     * Around advice that measures execution time for service layer methods
     */
    @Around("execution(* com.freelancer.portal.*.service.*.*(..))")
    public Object measureServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        
        // Record start time
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute the method
            return joinPoint.proceed();
        } finally {
            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Record the metrics
            Timer.builder("service.execution.time")
                    .tag(TAG_SERVICE, signature.getDeclaringType().getSimpleName())
                    .tag(TAG_METHOD, signature.getName())
                    .description("Service method execution time")
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);
            
            // Log the execution time if it exceeds thresholds
            if (executionTime > 300) {
                logger.warn("Slow service execution: {} took {}ms", methodName, executionTime);
            }
        }
    }
}