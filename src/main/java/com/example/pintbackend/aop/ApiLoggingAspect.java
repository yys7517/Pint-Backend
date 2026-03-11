/**
 * File: null.java
 * Path: com.example.pintbackend.aop
 * <p>
 * Outline:
 * using AOP to measure how long it takes to execute an api request
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ApiLoggingAspect {

    @Around("execution(* com.example.pintbackend.controller..*(..))")
    public Object logApi(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long time = System.currentTimeMillis() - start;

        log.info("{} executed in {}ms",
                joinPoint.getSignature().toShortString(),
                time);
        return result;
    }
}
