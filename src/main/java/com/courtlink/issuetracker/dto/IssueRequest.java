package com.courtlink.issuetracker.dto;

import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "ȱ�ݴ�������")
public record IssueRequest(
    @Schema(description = "ȱ�ݱ���", example = "��¼��ť����Ӧ")
    @NotBlank(message = "���ⲻ��Ϊ��")
    @Size(min = 5, max = 200, message = "���ⳤ�ȱ�����5-200�ַ�֮��")
    String title,

    @Schema(description = "ȱ������", example = "�����¼��ť��û���κη�Ӧ������̨�޴�����Ϣ")
    @NotBlank(message = "��������Ϊ��")
    String description,

    @Schema(description = "��ͼURL", example = "https://example.com/screenshot.png")
    String screenshotUrl,

    @Schema(description = "���ȼ�", example = "HIGH")
    @NotNull(message = "���ȼ�����Ϊ��")
    IssuePriority priority,

    @Schema(description = "����ģ��", example = "FRONTEND")
    @NotNull(message = "����ģ�鲻��Ϊ��")
    IssueModule module
) {} 