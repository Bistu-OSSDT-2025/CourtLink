package com.bistu.ossdt.courtlink.admin.aspect;

import com.bistu.ossdt.courtlink.admin.model.AuditLog;
import com.bistu.ossdt.courtlink.admin.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) && within(com.bistu.ossdt.courtlink.admin.controller..*)")
    public Object logAuditOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        // Record operation time
        LocalDateTime operationTime = LocalDateTime.now();
        
        // Execute the actual method
        Object result = joinPoint.proceed();
        
        // Create audit log
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType(joinPoint.getSignature().getName());
        auditLog.setOperationTarget(joinPoint.getTarget().getClass().getSimpleName());
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setOperationTime(operationTime);
        
        // Save audit log
        auditLogService.save(auditLog);
        
        return result;
    }
} 