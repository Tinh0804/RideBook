package  me.myproject.Utilities.DIMENSION;

import java.awt.Toolkit;

public class DimensionFrame {
    public static DimensionFrame instance = new DimensionFrame();
    public static int widthFrame = Toolkit.getDefaultToolkit().getScreenSize().width *2/3;;
    public static int heightFrame = Toolkit.getDefaultToolkit().getScreenSize().height *4/5;

    public static int width90 = (int) (widthFrame * 0.9);
    public static int width80 = (int) (widthFrame * 0.8);
    public static int width70 = (int) (widthFrame * 0.7);
    public static int width60 = (int) (widthFrame * 0.6);
    public static int width50 = (int) (widthFrame * 0.5);
    public static int width40 = (int) (widthFrame * 0.4);
    public static int width30 = (int) (widthFrame * 0.3);
    public static int width20 = (int) (widthFrame * 0.2);
    public static int width10 = (int) (widthFrame * 0.1);

    public static int height90 = (int) (heightFrame * 0.9);
    public static int height80 = (int) (heightFrame * 0.8);
    public static int height70 = (int) (heightFrame * 0.7);
    public static int height60 = (int) (heightFrame * 0.6);
    public static int height50 = (int) (heightFrame * 0.5);
    public static int height40 = (int) (heightFrame * 0.4);
    public static int height30 = (int) (heightFrame * 0.3);
    public static int height20 = (int) (heightFrame * 0.2);
    public static int height10 = (int) (heightFrame * 0.1);
}