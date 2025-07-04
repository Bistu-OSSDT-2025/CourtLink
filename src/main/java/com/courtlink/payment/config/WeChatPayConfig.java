package com.courtlink.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.pay")
public class WeChatPayConfig {
    
    /**
     * 商户号
     */
    private String merchantId = "1234567890";
    
    /**
     * 应用ID
     */
    private String appId = "wx1234567890abcdef";
    
    /**
     * API密钥
     */
    private String apiKey = "your_api_key_here_32_characters_long";
    
    /**
     * 商户API证书序列号
     */
    private String merchantSerialNumber = "your_merchant_serial_number";
    
    /**
     * 微信支付平台证书路径
     */
    private String platformCertPath = "classpath:wechat/platform_cert.pem";
    
    /**
     * 商户私钥证书路径
     */
    private String merchantPrivateKeyPath = "classpath:wechat/merchant_private_key.pem";
    
    /**
     * 支付回调地址
     */
    private String notifyUrl = "http://localhost:8082/api/payment/wechat/notify";
    
    /**
     * 是否为模拟模式
     */
    private boolean mockMode = true;
    
    /**
     * 订单过期时间（分钟）
     */
    private int orderExpireMinutes = 5;
    
    /**
     * API版本
     */
    private String apiVersion = "v3";
    
    /**
     * 统一下单URL
     */
    private String unifiedOrderUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";
    
    /**
     * 查询订单URL
     */
    private String queryOrderUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/{out_trade_no}";
    
    /**
     * 关闭订单URL
     */
    private String closeOrderUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/{out_trade_no}/close";
} 