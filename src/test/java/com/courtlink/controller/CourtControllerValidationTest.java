package com.courtlink.controller;

import com.courtlink.dto.CourtRequest;
import com.courtlink.service.CourtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ������������֤����
 * ���Ը�����Ч���ݺͱ߽�����
 */
@WebMvcTest(CourtController.class)
@ExtendWith(MockitoExtension.class)
public class CourtControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourtService courtService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // ���Գ�ʼ��
    }

    @Test
    void createCourt_WithNullName_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName(null);  // ������
        request.setDescription("��������");
        request.setCapacity(20);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("");  // ���ַ�������
        request.setDescription("��������");
        request.setCapacity(20);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithNegativeCapacity_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("���Գ���");
        request.setDescription("��������");
        request.setCapacity(-5);  // ��������

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithZeroCapacity_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("���Գ���");
        request.setDescription("��������");
        request.setCapacity(0);  // ������

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithTooLongName_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("����һ���ǳ��ǳ��ǳ��ǳ��ǳ��ǳ��ǳ��ǳ��ǳ����ĳ������ƣ�Ӧ�ó�����ϵͳ���Ƶ���󳤶ȣ��������Գ�����֤�����Ƿ���������");
        request.setDescription("��������");
        request.setCapacity(20);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithSpecialCharactersName_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("<script>alert('xss')</script>");  // XSS��������
        request.setDescription("��������");
        request.setCapacity(20);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithSQLInjection_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("'; DROP TABLE courts; --");  // SQLע�����
        request.setDescription("��������");
        request.setCapacity(20);

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCourt_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("���µĳ���");
        request.setDescription("���µ�����");
        request.setCapacity(25);

        mockMvc.perform(put("/api/courts/invalid-id")  // ��ЧID
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        String incompleteJson = "{\"name\":\"���Գ���\"}";  // ȱ�ٱ����ֶ�

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithInvalidJSON_ShouldReturnBadRequest() throws Exception {
        String invalidJson = "{\"name\":\"���Գ���\",\"capacity\":}";  // ��ЧJSON��ʽ

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithExtremelyLargeCapacity_ShouldReturnBadRequest() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("���Գ���");
        request.setDescription("��������");
        request.setCapacity(999999999);  // ��������ֵ

        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourt_WithNullDescription_ShouldHandleGracefully() throws Exception {
        CourtRequest request = new CourtRequest();
        request.setName("���Գ���");
        request.setDescription(null);  // ������
        request.setCapacity(20);

        // ������Ӧ�ñ������򷵻��ʵ��Ĵ���
        mockMvc.perform(post("/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk().or(status().isBadRequest()));
    }
} 