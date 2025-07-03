package com.courtlink.issuetracker.controller;

import com.courtlink.issuetracker.dto.IssueRequest;
import com.courtlink.issuetracker.dto.IssueResponse;
import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import com.courtlink.issuetracker.enums.IssueStatus;
import com.courtlink.issuetracker.service.IssueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IssueController.class)
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IssueService issueService;

    @Test
    void createIssue_ValidRequest_ReturnsCreated() throws Exception {
        // 准备测试数据
        var request = new IssueRequest(
            "测试标题",
            "测试描述",
            "http://example.com/screenshot.png",
            IssuePriority.HIGH,
            IssueModule.FRONTEND
        );

        var response = new IssueResponse(
            1L,
            request.title(),
            request.description(),
            request.screenshotUrl(),
            IssueStatus.NEW,
            request.priority(),
            request.module(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(issueService.createIssue(any(IssueRequest.class))).thenReturn(response);

        // 执行测试
        mockMvc.perform(post("/api/issues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(response.id()))
            .andExpect(jsonPath("$.title").value(response.title()))
            .andExpect(jsonPath("$.status").value(response.status().name()));
    }

    @Test
    void findIssues_ValidParams_ReturnsPagedResponse() throws Exception {
        // 准备测试数据
        var response = new IssueResponse(
            1L,
            "测试标题",
            "测试描述",
            "http://example.com/screenshot.png",
            IssueStatus.NEW,
            IssuePriority.HIGH,
            IssueModule.FRONTEND,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        var page = new PageImpl<>(List.of(response));
        when(issueService.findIssues(eq(IssueStatus.NEW), eq(IssuePriority.HIGH), eq(IssueModule.FRONTEND), any()))
            .thenReturn(page);

        // 执行测试
        mockMvc.perform(get("/api/issues")
                .param("status", "NEW")
                .param("priority", "HIGH")
                .param("module", "FRONTEND"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(response.id()))
            .andExpect(jsonPath("$.content[0].title").value(response.title()));
    }

    @Test
    void getIssue_ExistingId_ReturnsIssue() throws Exception {
        // 准备测试数据
        var response = new IssueResponse(
            1L,
            "测试标题",
            "测试描述",
            "http://example.com/screenshot.png",
            IssueStatus.NEW,
            IssuePriority.HIGH,
            IssueModule.FRONTEND,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(issueService.getIssue(1L)).thenReturn(response);

        // 执行测试
        mockMvc.perform(get("/api/issues/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(response.id()))
            .andExpect(jsonPath("$.title").value(response.title()));
    }

    @Test
    void updateIssueStatus_ValidStatus_ReturnsUpdatedIssue() throws Exception {
        // 准备测试数据
        var response = new IssueResponse(
            1L,
            "测试标题",
            "测试描述",
            "http://example.com/screenshot.png",
            IssueStatus.IN_PROGRESS,
            IssuePriority.HIGH,
            IssueModule.FRONTEND,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(issueService.updateIssueStatus(1L, IssueStatus.IN_PROGRESS)).thenReturn(response);

        // 执行测试
        mockMvc.perform(patch("/api/issues/1/status")
                .param("status", "IN_PROGRESS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(response.id()))
            .andExpect(jsonPath("$.status").value(response.status().name()));
    }
} 