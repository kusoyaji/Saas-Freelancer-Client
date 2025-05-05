package com.freelancer.portal.monitoring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * Aspect that automatically times and logs the performance of repository methods.
 * This provides additional monitoring beyond p6spy by timing the entire repository method call,
 * not just the SQL execution time.
 */
@Aspect
@Component
public class QueryTimerAspect {
    
    private static final Logger log = LoggerFactory.getLogger(QueryTimerAspect.class);
    
    // Define pointcuts for repository methods
    @Pointcut("execution(* com.freelancer.portal.repository.*.*(..))")
    public void repositoryMethod() {}
    
    // Define pointcut for service methods that might involve complex database operations
    @Pointcut("execution(* com.freelancer.portal.service.*.*(..))")
    public void serviceMethod() {}
    
    /**
     * Times and logs the execution of repository methods.
     * 
     * @param joinPoint The join point representing the intercepted method
     * @return The result of the method execution
     * @throws Throwable if the intercepted method throws an exception
     */
    @Around("repositoryMethod()")
    public Object timeRepositoryMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return timeMethod(joinPoint, "Repository");
    }
    
    /**
     * Times and logs the execution of service methods.
     * 
     * @param joinPoint The join point representing the intercepted method
     * @return The result of the method execution
     * @throws Throwable if the intercepted method throws an exception
     */
    @Around("serviceMethod()")
    public Object timeServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return timeMethod(joinPoint, "Service");
    }
    
    /**
     * Generic method for timing method execution.
     * 
     * @param joinPoint The join point representing the intercepted method
     * @param methodType The type of method being intercepted (e.g., "Repository", "Service")
     * @return The result of the method execution
     * @throws Throwable if the intercepted method throws an exception
     */
    private Object timeMethod(ProceedingJoinPoint joinPoint, String methodType) throws Throwable {
        // Set a warning threshold for slow methods (500ms)
        final long SLOW_METHOD_THRESHOLD_MS = 500;
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String methodIdentifier = className + "." + methodName;
        
        StopWatch stopWatch = new StopWatch(methodIdentifier);
        stopWatch.start();
        
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();
            
            // Log at appropriate level based on execution time
            if (executionTime > SLOW_METHOD_THRESHOLD_MS) {
                log.warn("{} method [{}] executed in {} ms (SLOW)", 
                        methodType, methodIdentifier, executionTime);
                
                // For extremely slow methods, log at error level and include parameters
                if (executionTime > SLOW_METHOD_THRESHOLD_MS * 4) {
                    Object[] args = joinPoint.getArgs();
                    StringBuilder argString = new StringBuilder();
                    for (Object arg : args) {
                        argString.append(arg != null ? arg.toString() : "null").append(", ");
                    }
                    
                    if (argString.length() > 0) {
                        argString.setLength(argString.length() - 2); // Remove trailing ", "
                    }
                    
                    log.error("{} method [{}] executed in {} ms (VERY SLOW). Args: [{}]", 
                            methodType, methodIdentifier, executionTime, argString);
                }
            } else {
                log.debug("{} method [{}] executed in {} ms", 
                        methodType, methodIdentifier, executionTime);
            }
        }
    }
}