package wxdgaming.boot.core.io;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.core.str.StringUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;
import java.util.HashMap;

/**
 * 二维码
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-06 19:53
 **/
@Accessors(chain = true)
public class QRCodeBox {

    public static QRCodeBox of() {
        return new QRCodeBox();
    }

    /** 二维码的颜色部分 */
    @Setter int BLACK = 0xFF000000;
    /** 二维码的颜色部分 */
    @Setter int WHITE = 0xFFFFFFFF;
    /** 二维码四周的白色边框 */
    @Setter int margin = 1;
    /** logo 标记部分 */
    @Setter int logoPart = 4;
    /** jpg png */
    @Setter String format = "png";
    /** 二维码内容 */
    @Setter String content = null;
    /** 宽度 */
    @Setter int width = 120;
    /** 高度 */
    @Setter int height = 120;
    /** 目前 针对容错等级为H reduceWhiteArea 二维码空白区域的大小 根据实际情况设置，如果二维码内容长度不固定的话 需要自己根据实际情况计算reduceWhiteArea的大小 */
    @Setter int reduceWhiteArea = 8;
    private BufferedImage bufferedImage;


    public static void main(String[] args) throws WriterException {
        /*二维码内容*/
        String content = "https://wan.ludashi.com/h5pay/index?sign=26a401818f015dbbf966cd4f0f25fadf&change=0";
        String logoPath = "D:\\logo.jpeg"; // 二维码中间的logo信息 非必须

        /*设置二维码矩阵的信息*/
        QRCodeBox.of()
                .setContent(content)
                .setWidth(500)
                .setHeight(580)
                .setReduceWhiteArea(22)
                .addLogo(logoPath)
                .writeToFile("target/" + new Date().getTime() + ".png");
    }

    public QRCodeBox writeToFile(String outFile) {
        FileUtil.mkdirs(outFile);
        try (OutputStream outStream = new FileOutputStream(outFile)) {
            writeToStream(outStream);
            return this;
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    public QRCodeBox writeToStream(OutputStream outputStream) {
        try {
            if (bufferedImage == null) buildImage();
            ImageIO.write(bufferedImage, format, outputStream);
            return this;
        } catch (IOException e) {
            throw Throw.as(e);
        }
    }

    public byte[] toBytes() {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            writeToStream(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    public void buildImage() {
        try {
            if (StringUtil.emptyOrNull(content)) throw new RuntimeException("二维码内容空");
            HashMap<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // 指定编码方式,避免中文乱码
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 指定纠错等级 如果二维码里面的内容比较多的话推荐使用H 容错率30%， 这样可以避免一些扫描不出来的问题
            hints.put(EncodeHintType.MARGIN, margin); // 指定二维码四周白色区域大小 官方的这个方法目前没有没有作用默认设置为0
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            bufferedImage = new BufferedImage(width - 2 * reduceWhiteArea, height - 2 * reduceWhiteArea, BufferedImage.TYPE_3BYTE_BGR);
            for (int x = reduceWhiteArea; x < width - reduceWhiteArea; x++) {
                for (int y = reduceWhiteArea; y < height - reduceWhiteArea; y++) {
                    bufferedImage.setRGB(x - reduceWhiteArea, y - reduceWhiteArea, bitMatrix.get(x, y) ? BLACK : WHITE);
                }
            }
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 给二维码图片中绘制logo信息 非必须
     *
     * @param logoPath logo图片路径
     */

    public QRCodeBox addLogo(String logoPath) {
        return addLogo(new File(logoPath));
    }

    public QRCodeBox addLogo(File logoPath) {
        try {
            return addLogo(new FileInputStream(logoPath));
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    public QRCodeBox addLogo(InputStream logoPath) {
        try {
            if (bufferedImage == null) buildImage();
            BufferedImage logoImage = ImageIO.read(logoPath);
            /*计算logo图片大小,可适应长方形图片,根据较短边生成正方形*/
            int width = bufferedImage.getWidth() < bufferedImage.getHeight() ? bufferedImage.getWidth() / logoPart : bufferedImage.getHeight() / logoPart;
            int height = width;
            /*计算logo图片放置位置*/
            int x = (bufferedImage.getWidth() - width) / 2;
            int y = (bufferedImage.getHeight() - height) / 2;
            Graphics2D g = bufferedImage.createGraphics();
            try {
                /*在二维码图片上绘制中间的logo*/
                g.drawImage(logoImage, x, y, width, height, null);
                /*绘制logo边框,可选*/
                g.setStroke(new BasicStroke(2)); // 画笔粗细
                g.setColor(Color.WHITE); // 边框颜色
                g.drawRect(x, y, width, height); // 矩形边框
                logoImage.flush();
            } finally {
                g.dispose();
            }
            return this;
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }


}
