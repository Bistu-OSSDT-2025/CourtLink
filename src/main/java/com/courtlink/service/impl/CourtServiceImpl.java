package com.courtlink.service.impl;

import com.courtlink.dto.CourtRequest;
import com.courtlink.dto.CourtResponse;
import com.courtlink.entity.Court;
import com.courtlink.enums.CourtStatus;
import com.courtlink.repository.CourtRepository;
import com.courtlink.service.CourtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {
    
    private final CourtRepository courtRepository;

    @Override
    @Transactional
    public CourtResponse createCourt(CourtRequest request) {
        log.info("开始创建场地: {}", request.getName());
        
        // 参数验证
        validateCourtRequest(request);
        
        try {
            Court court = new Court();
            BeanUtils.copyProperties(request, court);
            court = courtRepository.save(court);
            
            log.info("场地创建成功: ID={}, 名称={}", court.getId(), court.getName());
            return convertToResponse(court);
        } catch (Exception e) {
            log.error("创建场地失败: {}", request.getName(), e);
            throw new RuntimeException("创建场地失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public CourtResponse updateCourt(Long id, CourtRequest request) {
        log.info("开始更新场地: ID={}", id);
        
        // 参数验证
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("场地ID无效");
        }
        validateCourtRequest(request);
        
        try {
            Court court = courtRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("场地不存在: ID=" + id));
            
            String originalName = court.getName();
            BeanUtils.copyProperties(request, court, "id", "createdAt");
            court = courtRepository.save(court);
            
            log.info("场地更新成功: ID={}, 原名称={}, 新名称={}", 
                    id, originalName, court.getName());
            return convertToResponse(court);
        } catch (EntityNotFoundException e) {
            log.warn("更新场地失败，场地不存在: ID={}", id);
            throw e;
        } catch (Exception e) {
            log.error("更新场地失败: ID={}", id, e);
            throw new RuntimeException("更新场地失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteCourt(Long id) {
        log.info("开始删除场地: ID={}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("场地ID无效");
        }
        
        try {
            if (!courtRepository.existsById(id)) {
                log.warn("删除场地失败，场地不存在: ID={}", id);
                throw new EntityNotFoundException("场地不存在: ID=" + id);
            }
            
            courtRepository.deleteById(id);
            log.info("场地删除成功: ID={}", id);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除场地失败: ID={}", id, e);
            throw new RuntimeException("删除场地失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CourtResponse getCourt(Long id) {
        log.debug("查询场地信息: ID={}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("场地ID无效");
        }
        
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("场地不存在: ID={}", id);
                    return new EntityNotFoundException("场地不存在: ID=" + id);
                });
        
        return convertToResponse(court);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtResponse> getAllCourts() {
        log.debug("查询所有场地信息");
        
        try {
            List<Court> courts = courtRepository.findAll();
            log.info("查询到场地数量: {}", courts.size());
            
            return courts.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询所有场地失败", e);
            throw new RuntimeException("查询场地信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtResponse> searchCourts(String keyword, CourtStatus status) {
        log.info("搜索场地: 关键词={}, 状态={}", keyword, status);
        
        try {
            List<Court> courts = courtRepository.searchCourts(keyword, status);
            log.info("搜索到场地数量: {} (关键词: {}, 状态: {})", 
                    courts.size(), keyword, status);
            
            return courts.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索场地失败: 关键词={}, 状态={}", keyword, status, e);
            throw new RuntimeException("搜索场地失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public CourtResponse changeStatus(Long id, CourtStatus status) {
        log.info("更改场地状态: ID={}, 新状态={}", id, status);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("场地ID无效");
        }
        if (status == null) {
            throw new IllegalArgumentException("场地状态不能为空");
        }
        
        try {
            Court court = courtRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("场地不存在: ID=" + id));
            
            CourtStatus originalStatus = court.getStatus();
            court.setStatus(status);
            court = courtRepository.save(court);
            
            log.info("场地状态更改成功: ID={}, 原状态={}, 新状态={}", 
                    id, originalStatus, status);
            return convertToResponse(court);
        } catch (EntityNotFoundException e) {
            log.warn("更改场地状态失败，场地不存在: ID={}", id);
            throw e;
        } catch (Exception e) {
            log.error("更改场地状态失败: ID={}, 状态={}", id, status, e);
            throw new RuntimeException("更改场地状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证场地请求参数
     */
    private void validateCourtRequest(CourtRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("场地信息不能为空");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("场地名称不能为空");
        }
        if (!StringUtils.hasText(request.getLocation())) {
            throw new IllegalArgumentException("场地位置不能为空");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("场地状态不能为空");
        }
    }

    /**
     * 转换为响应对象
     */
    private CourtResponse convertToResponse(Court court) {
        CourtResponse response = new CourtResponse();
        BeanUtils.copyProperties(court, response);
        return response;
    }
} 