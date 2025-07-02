package com.courtlink.service.impl;

import com.courtlink.dto.CourtDTO;
import com.courtlink.entity.Court;
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
@Transactional
public class CourtServiceImpl implements CourtService {
    
    private final CourtRepository courtRepository;

    @Override
    public List<CourtDTO> getAllCourts() {
        log.info("获取所有场地");
        List<Court> courts = courtRepository.findAll();
        return courts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CourtDTO getCourtById(Long id) {
        log.info("获取场地: ID={}", id);
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: ID=" + id));
        return convertToDTO(court);
    }

    @Override
    public CourtDTO createCourt(CourtDTO courtDTO) {
        log.info("创建场地: {}", courtDTO);
        validateCourtDTO(courtDTO);
        Court court = new Court();
        BeanUtils.copyProperties(courtDTO, court);
        court = courtRepository.save(court);
        return convertToDTO(court);
    }

    @Override
    public CourtDTO updateCourt(Long id, CourtDTO courtDTO) {
        log.info("更新场地: ID={}, {}", id, courtDTO);
        validateCourtDTO(courtDTO);
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: ID=" + id));
        BeanUtils.copyProperties(courtDTO, court, "id");
        court = courtRepository.save(court);
        return convertToDTO(court);
    }

    @Override
    public void deleteCourt(Long id) {
        log.info("删除场地: ID={}", id);
        if (!courtRepository.existsById(id)) {
            throw new EntityNotFoundException("场地不存在: ID=" + id);
        }
        courtRepository.deleteById(id);
    }

    @Override
    public CourtDTO updateCourtStatus(Long id, String status) {
        log.info("更新场地状态: ID={}, status={}", id, status);
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: ID=" + id));
        court.setStatus(status);
        court = courtRepository.save(court);
        return convertToDTO(court);
    }

    @Override
    public List<CourtDTO> searchCourts(String keyword, String status) {
        log.info("搜索场地: keyword={}, status={}", keyword, status);
        List<Court> courts = courtRepository.searchCourts(keyword, status);
        return courts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void validateCourtDTO(CourtDTO courtDTO) {
        if (courtDTO == null) {
            throw new IllegalArgumentException("场地信息不能为空");
        }
        if (!StringUtils.hasText(courtDTO.getName())) {
            throw new IllegalArgumentException("场地名称不能为空");
        }
    }

    private CourtDTO convertToDTO(Court court) {
        CourtDTO dto = new CourtDTO();
        BeanUtils.copyProperties(court, dto);
        return dto;
    }
} 