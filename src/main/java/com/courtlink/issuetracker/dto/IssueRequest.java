package com.courtlink.issuetracker.dto;

import com.courtlink.issuetracker.enums.IssueModule;
import com.courtlink.issuetracker.enums.IssuePriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "缺陷创建请求")
public record IssueRequest(
    @Schema(description = "缺陷标题", example = "登录按钮无响应")
    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 200, message = "标题长度必须在5-200字符之间")
    String title,

    @Schema(description = "缺陷描述", example = "点击登录按钮后没有任何反应，控制台无错误信息")
    @NotBlank(message = "描述不能为空")
    String description,

    @Schema(description = "截图URL", example = "https://example.com/screenshot.png")
    String screenshotUrl,

    @Schema(description = "优先级", example = "HIGH")
    @NotNull(message = "优先级不能为空")
    IssuePriority priority,

    @Schema(description = "所属模块", example = "FRONTEND")
    @NotNull(message = "所属模块不能为空")
    IssueModule module
) {} 