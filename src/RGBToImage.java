import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.imageio.ImageIO;

public class RGBToImage {
  private static int width = 352;
  private static int height = 288;
  private static String format = "jpg";

  public void convert(String inputPath, String outputPath) {
    File rgbsFolder = new File(inputPath);
    File imagesFolder = new File(outputPath);
    imagesFolder.mkdir();
    String[] pathParts = inputPath.split("/");
    String videoName = pathParts[pathParts.length - 1];
    for (File rgbFile: rgbsFolder.listFiles()) {
      String[] parts = rgbFile.getName().split("\\.");
      if (!parts[1].equals("rgb")) continue;
      String fileName = parts[0];
      convertToImage(rgbFile.getPath(), format, outputPath + "/" + fileName + "." + format);
    }
  }

  private void convertToImage(String rgbPath, String format, String imgPath) {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    try
    {
      int frameLength = width * height * 3;
      File file = new File(rgbPath);
      RandomAccessFile raf = new RandomAccessFile(file, "r");
      raf.seek(0);
      long len = frameLength;
      byte[] bytes = new byte[(int) len];
      raf.read(bytes);
      int ind = 0;
      for(int y = 0; y < height; y++) {
        for(int x = 0; x < width; x++) {
          byte a = 0;
          byte r = bytes[ind];
          byte g = bytes[ind + height * width];
          byte b = bytes[ind + height * width * 2];

          int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
          //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
          image.setRGB(x , y, pix);
          ind++;
        }
      }
      ImageIO.write(image, format, new File(imgPath));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
