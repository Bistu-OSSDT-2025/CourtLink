package com.courtlink.payment.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class QrCodeUtil {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    private static final String DEFAULT_FORMAT = "PNG";

    /**
     * 生成二维码图片Base64字符串
     *
     * @param content 二维码内容
     * @return Base64编码的图片字符串
     */
    public static String generateQrCodeBase64(String content) {
        return generateQrCodeBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码图片Base64字符串
     *
     * @param content 二维码内容
     * @param width   图片宽度
     * @param height  图片高度
     * @return Base64编码的图片字符串
     */
    public static String generateQrCodeBase64(String content, int width, int height) {
        try {
            BufferedImage image = generateQrCodeImage(content, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, DEFAULT_FORMAT, baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("生成二维码失败", e);
            return null;
        }
    }

    /**
     * 生成二维码图片
     *
     * @param content 二维码内容
     * @param width   图片宽度
     * @param height  图片高度
     * @return BufferedImage对象
     */
    public static BufferedImage generateQrCodeImage(String content, int width, int height) 
            throws WriterException, IOException {
        
        // 设置二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        // 生成二维码
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        
        // 设置抗锯齿
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充背景色
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        
        // 绘制二维码
        graphics.setColor(Color.BLACK);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }
        
        graphics.dispose();
        return image;
    }

    /**
     * 生成带logo的二维码
     *
     * @param content    二维码内容
     * @param logoBase64 logo图片的Base64字符串
     * @param width      图片宽度
     * @param height     图片高度
     * @return Base64编码的图片字符串
     */
    public static String generateQrCodeWithLogo(String content, String logoBase64, int width, int height) {
        try {
            BufferedImage qrImage = generateQrCodeImage(content, width, height);
            
            if (logoBase64 != null && !logoBase64.isEmpty()) {
                // 解码logo图片
                byte[] logoBytes = Base64.getDecoder().decode(logoBase64.split(",")[1]);
                BufferedImage logoImage = ImageIO.read(new java.io.ByteArrayInputStream(logoBytes));
                
                // 计算logo大小（二维码的1/5）
                int logoWidth = width / 5;
                int logoHeight = height / 5;
                
                // 缩放logo
                Image scaledLogo = logoImage.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);
                BufferedImage logo = new BufferedImage(logoWidth, logoHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = logo.createGraphics();
                g2d.drawImage(scaledLogo, 0, 0, null);
                g2d.dispose();
                
                // 在二维码中心绘制logo
                Graphics2D qrGraphics = qrImage.createGraphics();
                qrGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int logoX = (width - logoWidth) / 2;
                int logoY = (height - logoHeight) / 2;
                
                // 绘制白色背景
                qrGraphics.setColor(Color.WHITE);
                qrGraphics.fillRoundRect(logoX - 5, logoY - 5, logoWidth + 10, logoHeight + 10, 10, 10);
                
                // 绘制logo
                qrGraphics.drawImage(logo, logoX, logoY, null);
                qrGraphics.dispose();
            }
            
            // 转换为Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, DEFAULT_FORMAT, baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (Exception e) {
            log.error("生成带logo的二维码失败", e);
            return generateQrCodeBase64(content, width, height);
        }
    }

    /**
     * 验证二维码内容格式
     *
     * @param content 二维码内容
     * @return 是否为有效格式
     */
    public static boolean isValidQrContent(String content) {
        return content != null && content.trim().length() > 0 && content.length() <= 2048;
    }
} 