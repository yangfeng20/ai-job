package com.maple.ai.job.hunting.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Hashtable;

/**
 * @author ThirdGoddess
 * @version 1.0.0
 * @since 2022/12/22 15:18
 * desc 二维码处理工具
 */
public class QrCodeUtil {

    //编码格式,采用utf-8
    private static final String UNICODE = "utf-8";
    //图片格式
    //private static final String FORMAT = "JPG";
    //二维码宽度像素pixels数量
    private static final int QRCODE_WIDTH = 300;
    //二维码高度像素pixels数量
    private static final int QRCODE_HEIGHT = 300;
    //LOGO宽度像素pixels数量
    private static final int LOGO_WIDTH = 100;
    //LOGO高度像素pixels数量
    private static final int LOGO_HEIGHT = 100;

    /**
     * 生成二维码图片
     *
     * @param content 生成内容
     * @return BufferedImage
     */
    public static BufferedImage createImage(String content) throws Exception {
        return createImage(content, null);
    }

    /**
     * 生成二维码图片，带logo
     *
     * @param content  二维码内容
     * @param logoBase64 logo图片base64数据
     * @return BufferedImage
     * @throws Exception Exception
     */
    public static BufferedImage createImage(String content, String logoBase64) throws Exception {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, UNICODE);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QRCODE_WIDTH, QRCODE_HEIGHT, hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        if (null != logoBase64 && !logoBase64.isEmpty()) {
            //向二维码插入图片
            QrCodeUtil.insertImage(image, logoBase64);
        }

        return image;
    }

    /**
     * 向二维码插入图片
     *
     * @param source   二维码BufferedImage
     * @param logoBase64 图片path
     * @throws Exception Exception
     */
    private static void insertImage(BufferedImage source, String logoBase64) throws Exception {

        // 读取LOGO图片
        InputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(logoBase64));
        Image src = ImageIO.read(inputStream);

        int width = src.getWidth(null);
        int height = src.getHeight(null);
        if (width > LOGO_WIDTH) {
            width = LOGO_WIDTH;
        }
        if (height > LOGO_HEIGHT) {
            height = LOGO_HEIGHT;
        }
        Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();

        //绘制缩小后的图
        g.drawImage(image, 0, 0, null);
        g.dispose();
        src = image;
        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = (QRCODE_WIDTH - width) / 2;
        int y = (QRCODE_HEIGHT - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }


}