package me.myproject.Utilities.DIMENSION;

import java.awt.BorderLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import me.myproject.Utilities.ColorMain;

public class FrameMain extends  JFrame{
        
    // Lấy chiều rộng và chiều cao
    public static double  width = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static double  height = Toolkit.getDefaultToolkit().getScreenSize().height;
    public static int  widthFrame = (int)width*2/3;
    public static int  heightFrame = (int)height*4/5;

    public static  JPanel pnL=new JPanel();
    public static  JPanel pnT=new JPanel();
    public static  JPanel pnC=new JPanel();

    public FrameMain(String title) {
        this.setSize(widthFrame, heightFrame);
        this.setTitle(title);
        this.setLayout(new BorderLayout());
        
   
        pnL.setBackground(ColorMain.colorSecondary);
        pnL.setPreferredSize(new java.awt.Dimension(100, heightFrame)); // Kích thước mong muốn
        pnL.setMinimumSize(new java.awt.Dimension(100, heightFrame));   // Kích thước tối thiểu
        pnL.setMaximumSize(new java.awt.Dimension(100, heightFrame)); 
      
        pnT.setBackground(ColorMain.colorPrimary);
        pnT.setPreferredSize(new java.awt.Dimension(widthFrame, 50)); // Kích thước mong muốn
        pnT.setMinimumSize(new java.awt.Dimension(widthFrame, 50));   // Kích thước tối thiểu
        pnT.setMaximumSize(new java.awt.Dimension(widthFrame, 50)); 
        

        pnC.setBackground(ColorMain.colorThird);

     
        this.setLocationRelativeTo(null);
        // this.setVisible(true);
    }

    ///color
  
    static class Dimension{
        
    }

    
}
