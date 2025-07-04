package com.courtlink.court.service;

import com.courtlink.admin.entity.Admin;
import com.courtlink.admin.service.AdminService;
import com.courtlink.court.dto.CourtBatchRequest;
import com.courtlink.court.dto.CourtDTO;
import com.courtlink.court.dto.CourtManagementDTO;
import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;
import com.courtlink.court.repository.CourtRepository;
import com.courtlink.court.service.impl.CourtServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("场地管理功能测试")
class CourtManagementTest {

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private CourtServiceImpl courtService;

    private Admin testAdmin;
    private Court testCourt;
    private CourtBatchRequest batchRequest;

    @BeforeEach
    void setUp() {
        // 设置测试管理员
        testAdmin = new Admin();
        testAdmin.setUsername("testAdmin");
        
        // 设置测试场地
        testCourt = new Court();
        testCourt.setId(1L);
        testCourt.setName("测试场地");
        testCourt.setCourtType(Court.CourtType.BADMINTON);
        testCourt.setCourtLocation(Court.CourtLocation.INDOOR);
        testCourt.setPricePerHour(new BigDecimal("50.00"));
        testCourt.setStatus(CourtStatus.AVAILABLE);
        testCourt.setEnabled(true);
        testCourt.setCreatedAt(LocalDateTime.now());

        // 设置批量创建请求
        batchRequest = new CourtBatchRequest();
        batchRequest.setNamePrefix("羽毛球场");
        batchRequest.setCount(3);
        batchRequest.setCourtType(Court.CourtType.BADMINTON);
        batchRequest.setCourtLocation(Court.CourtLocation.INDOOR);
        batchRequest.setPricePerHour(new BigDecimal("45.00"));
        batchRequest.setLocation("体育馆内");
        batchRequest.setStartNumber(1);
        batchRequest.setNumberFormat("A{number}");
    }

    @Test
    @DisplayName("批量创建场地 - 成功")
    void testCreateMultipleCourts_Success() {
        // 准备
        when(adminService.getCurrentAdmin()).thenReturn(testAdmin);
        when(courtRepository.existsByName(anyString())).thenReturn(false);
        when(courtRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Court> courts = invocation.getArgument(0);
            for (int i = 0; i < courts.size(); i++) {
                courts.get(i).setId((long) (i + 1));
            }
            return courts;
        });

        // 执行
        List<CourtDTO> result = courtService.createMultipleCourts(batchRequest);

        // 验证
        assertEquals(3, result.size());
        assertEquals("羽毛球场A1", result.get(0).getName());
        assertEquals("羽毛球场A2", result.get(1).getName());
        assertEquals("羽毛球场A3", result.get(2).getName());
        
        verify(courtRepository).saveAll(argThat(courts -> {
            if (courts instanceof List) {
                List<Court> courtList = (List<Court>) courts;
                return courtList.size() == 3 && 
                       courtList.get(0).getName().equals("羽毛球场A1");
            }
            return false;
        }));
    }

    @Test
    @DisplayName("批量创建场地 - 跳过重复名称")
    void testCreateMultipleCourts_SkipDuplicateNames() {
        // 准备
        when(adminService.getCurrentAdmin()).thenReturn(testAdmin);
        when(courtRepository.existsByName("羽毛球场A1")).thenReturn(false);
        when(courtRepository.existsByName("羽毛球场A2")).thenReturn(true); // 名称已存在
        when(courtRepository.existsByName("羽毛球场A3")).thenReturn(false);
        when(courtRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Court> courts = invocation.getArgument(0);
            for (int i = 0; i < courts.size(); i++) {
                courts.get(i).setId((long) (i + 1));
            }
            return courts;
        });

        // 执行
        List<CourtDTO> result = courtService.createMultipleCourts(batchRequest);

        // 验证
        assertEquals(2, result.size()); // 只创建了2个，跳过了重复的
        verify(courtRepository).saveAll(argThat(courts -> {
            if (courts instanceof List) {
                List<Court> courtList = (List<Court>) courts;
                return courtList.size() == 2;
            }
            return false;
        }));
    }

    @Test
    @DisplayName("批量删除场地 - 成功")
    void testDeleteMultipleCourts_Success() {
        // 准备
        List<Long> courtIds = Arrays.asList(1L, 2L, 3L);
        List<Court> courts = Arrays.asList(
            createTestCourt(1L, "场地1", CourtStatus.AVAILABLE),
            createTestCourt(2L, "场地2", CourtStatus.CLOSED),
            createTestCourt(3L, "场地3", CourtStatus.MAINTENANCE)
        );
        
        when(courtRepository.findAllById(courtIds)).thenReturn(courts);

        // 执行
        assertDoesNotThrow(() -> courtService.deleteMultipleCourts(courtIds));

        // 验证
        verify(courtRepository).deleteAll(courts);
    }

    @Test
    @DisplayName("批量删除场地 - 场地不存在")
    void testDeleteMultipleCourts_CourtNotFound() {
        // 准备
        List<Long> courtIds = Arrays.asList(1L, 2L, 3L);
        List<Court> courts = Arrays.asList(
            createTestCourt(1L, "场地1", CourtStatus.AVAILABLE)
            // 只返回1个场地，但请求删除3个
        );
        
        when(courtRepository.findAllById(courtIds)).thenReturn(courts);

        // 执行 & 验证
        assertThrows(EntityNotFoundException.class, 
            () -> courtService.deleteMultipleCourts(courtIds));
        verify(courtRepository, never()).deleteAll(any());
    }

    @Test
    @DisplayName("批量删除场地 - 有活跃预订")
    void testDeleteMultipleCourts_HasActiveBookings() {
        // 准备
        List<Long> courtIds = Arrays.asList(1L, 2L);
        List<Court> courts = Arrays.asList(
            createTestCourt(1L, "场地1", CourtStatus.AVAILABLE),
            createTestCourt(2L, "场地2", CourtStatus.OCCUPIED) // 已占用，不能删除
        );
        
        when(courtRepository.findAllById(courtIds)).thenReturn(courts);

        // 执行 & 验证
        assertThrows(IllegalStateException.class, 
            () -> courtService.deleteMultipleCourts(courtIds));
        verify(courtRepository, never()).deleteAll(any());
    }

    @Test
    @DisplayName("批量更新状态 - 成功")
    void testBatchUpdateStatus_Success() {
        // 准备
        List<Long> courtIds = Arrays.asList(1L, 2L);
        List<Court> courts = Arrays.asList(
            createTestCourt(1L, "场地1", CourtStatus.AVAILABLE),
            createTestCourt(2L, "场地2", CourtStatus.AVAILABLE)
        );
        
        when(adminService.getCurrentAdmin()).thenReturn(testAdmin);
        when(courtRepository.findAllById(courtIds)).thenReturn(courts);
        when(courtRepository.saveAll(courts)).thenReturn(courts);

        // 执行
        List<CourtDTO> result = courtService.batchUpdateStatus(courtIds, CourtStatus.MAINTENANCE);

        // 验证
        assertEquals(2, result.size());
        verify(courtRepository).saveAll(argThat(updatedCourts -> {
            if (updatedCourts instanceof List) {
                List<Court> courtList = (List<Court>) updatedCourts;
                return courtList.stream().allMatch(court -> 
                    court.getStatus() == CourtStatus.MAINTENANCE);
            }
            return false;
        }));
    }

    @Test
    @DisplayName("设置维护模式 - 成功")
    void testSetMaintenanceMode_Success() {
        // 准备
        String reason = "定期维护检查";
        when(adminService.getCurrentAdmin()).thenReturn(testAdmin);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(testCourt));
        when(courtRepository.save(any(Court.class))).thenReturn(testCourt);

        // 执行
        CourtDTO result = courtService.setMaintenanceMode(1L, reason);

        // 验证
        assertNotNull(result);
        verify(courtRepository).save(argThat(court -> 
            court.getStatus() == CourtStatus.MAINTENANCE &&
            court.getMaintenanceReason().equals(reason) &&
            court.getMaintenanceBy().equals("testAdmin")));
    }

    @Test
    @DisplayName("设置维护模式 - 场地有活跃预订")
    void testSetMaintenanceMode_HasActiveBookings() {
        // 准备
        testCourt.setStatus(CourtStatus.OCCUPIED);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(testCourt));

        // 执行 & 验证
        assertThrows(IllegalStateException.class, 
            () -> courtService.setMaintenanceMode(1L, "维护"));
        verify(courtRepository, never()).save(any());
    }

    @Test
    @DisplayName("结束维护模式 - 成功")
    void testEndMaintenanceMode_Success() {
        // 准备
        testCourt.setStatus(CourtStatus.MAINTENANCE);
        testCourt.setMaintenanceReason("维护中");
        testCourt.setMaintenanceStartTime(LocalDateTime.now().minusHours(2));
        
        when(adminService.getCurrentAdmin()).thenReturn(testAdmin);
        when(courtRepository.findById(1L)).thenReturn(Optional.of(testCourt));
        when(courtRepository.save(any(Court.class))).thenReturn(testCourt);

        // 执行
        CourtDTO result = courtService.endMaintenanceMode(1L);

        // 验证
        assertNotNull(result);
        verify(courtRepository).save(argThat(court -> 
            court.getStatus() == CourtStatus.AVAILABLE &&
            court.getMaintenanceEndTime() != null));
    }

    @Test
    @DisplayName("结束维护模式 - 场地不在维护状态")
    void testEndMaintenanceMode_NotInMaintenance() {
        // 准备
        testCourt.setStatus(CourtStatus.AVAILABLE); // 不在维护状态
        when(courtRepository.findById(1L)).thenReturn(Optional.of(testCourt));

        // 执行 & 验证
        assertThrows(IllegalStateException.class, 
            () -> courtService.endMaintenanceMode(1L));
        verify(courtRepository, never()).save(any());
    }

    @Test
    @DisplayName("获取场地管理统计信息")
    void testGetCourtManagementInfo() {
        // 准备
        when(courtRepository.count()).thenReturn(10L);
        when(courtRepository.countEnabledCourts()).thenReturn(8L);
        when(courtRepository.countDisabledCourts()).thenReturn(2L);
        when(courtRepository.countByStatus(CourtStatus.AVAILABLE)).thenReturn(6L);
        when(courtRepository.countByStatus(CourtStatus.MAINTENANCE)).thenReturn(1L);
        when(courtRepository.countByStatus(CourtStatus.OCCUPIED)).thenReturn(1L);
        when(courtRepository.countByStatus(CourtStatus.CLOSED)).thenReturn(2L);
        when(courtRepository.countByStatus(CourtStatus.RESERVED)).thenReturn(0L);
        
        when(courtRepository.findAveragePrice()).thenReturn(new BigDecimal("45.50"));
        when(courtRepository.findMinPrice()).thenReturn(new BigDecimal("30.00"));
        when(courtRepository.findMaxPrice()).thenReturn(new BigDecimal("60.00"));

        // 执行
        CourtManagementDTO result = courtService.getCourtManagementInfo();

        // 验证
        assertNotNull(result);
        assertEquals(10, result.getTotalCourts());
        assertEquals(8, result.getEnabledCourts());
        assertEquals(2, result.getDisabledCourts());
        assertEquals(6, result.getAvailableCourts());
        assertEquals("GOOD", result.getSystemHealth()); // 60%可用率应该是GOOD
        assertEquals(60.0, result.getAvailabilityRate(), 0.1);
    }

    private Court createTestCourt(Long id, String name, CourtStatus status) {
        Court court = new Court();
        court.setId(id);
        court.setName(name);
        court.setStatus(status);
        court.setCourtType(Court.CourtType.BADMINTON);
        court.setCourtLocation(Court.CourtLocation.INDOOR);
        court.setPricePerHour(new BigDecimal("45.00"));
        court.setEnabled(true);
        court.setCreatedAt(LocalDateTime.now());
        return court;
    }
} 