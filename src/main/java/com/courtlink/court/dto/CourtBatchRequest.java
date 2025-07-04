package com.courtlink.court.dto;

import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtBatchRequest {
    
    @NotBlank(message = "场地名称前缀不能为空")
    @Size(max = 50, message = "场地名称前缀不能超过50个字符")
    private String namePrefix; // 场地名称前缀，如"羽毛球场"
    
    @Min(value = 1, message = "创建数量必须大于0")
    @Max(value = 50, message = "单次最多创建50个场地")
    private Integer count; // 要创建的场地数量
    
    @NotNull(message = "场地类型不能为空")
    private Court.CourtType courtType;
    
    @NotNull(message = "场地位置不能为空")
    private Court.CourtLocation courtLocation;
    
    @NotNull(message = "每小时价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @DecimalMax(value = "9999.99", message = "价格不能超过9999.99")
    private BigDecimal pricePerHour;
    
    @Size(max = 200, message = "位置描述不能超过200个字符")
    private String location;
    
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
    
    @Size(max = 500, message = "设施描述不能超过500个字符")
    private String facilities;
    
    @Size(max = 500, message = "使用规则不能超过500个字符")
    private String rules;
    
    @Min(value = 1, message = "最大容纳人数必须大于0")
    @Max(value = 50, message = "最大容纳人数不能超过50")
    private Integer maxPlayers;
    
    private Boolean hasLighting = false;
    
    @Min(value = 1, message = "营业时间必须大于0小时")
    @Max(value = 24, message = "营业时间不能超过24小时")
    private Integer operatingHours = 14;
    
    // 初始状态，默认为可用
    private CourtStatus initialStatus = CourtStatus.AVAILABLE;
    
    // 是否启用，默认为true
    private Boolean enabled = true;
    
    // 编号起始值，用于生成场地编号
    @Min(value = 1, message = "起始编号必须大于0")
    private Integer startNumber = 1;
    
    // 编号格式，如"A{number}"会生成A1, A2, A3...
    private String numberFormat = "{number}";
} 