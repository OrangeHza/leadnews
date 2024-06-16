package cn.whu.tess4j;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;

public class Application {
    // 识别图片中的文字
    public static void main(String[] args) throws TesseractException {
        // 创建实例
        Tesseract tesseract = new Tesseract();

        // 设置字体库路径
        tesseract.setDatapath("C:\\software\\workspace\\test\\tessdata");

        // 设置语言 --> 简体中文
        tesseract.setLanguage("chi_sim"); // 参数是: 简体中文库的文件名

        // 识别图片
        File file = new File("C:\\software\\workspace\\test\\image\\sensitive.jpg");
        String result = tesseract.doOCR(file);
        //System.out.println(result);
        // 回车和tab换成-   缩短长度方便进行检测 -最好有，本来就是分开的嘛
        System.out.println(result.replaceAll("\\r|\\n","-"));
    }

}
