package utils;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.swing.*;

public class QrCodeGen {

    public Image createQR(String data, String charset, Map<EncodeHintType, ErrorCorrectionLevel> hints,
                          int height, int width) throws WriterException, IOException {

        BitMatrix matrix = new com.google.zxing.MultiFormatWriter().encode(
                new String(data.getBytes(charset), charset),
                BarcodeFormat.QR_CODE, width, height);

        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    public QrCodeGen() {
    }

    public static void generateDefaultQRCode(String url) {

        String charset = "UTF-8";

        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        QrCodeGen qrCodeGen = new QrCodeGen();
        try {
            Image qrCodeImage = qrCodeGen.createQR(url, charset, hints, 200, 200);
            System.out.println("QR Code Generated!!!");

            displayQRCode(qrCodeImage);

        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void displayQRCode(Image qrCodeImage) {
        JFrame frame = new JFrame("QR Code Display");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(720, 540);

        ImageIcon icon = new ImageIcon(qrCodeImage);

        JLabel label = new JLabel(icon);

        frame.getContentPane().add(label, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
