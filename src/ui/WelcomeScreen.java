package ui;

import javax.microedition.lcdui.*;
import java.io.*;

public class WelcomeScreen extends Canvas {
    public interface Color {
        public static final int RED = 0xffff0000;
        public static final int GREEN = 0xff00ff00;
        public static final int BLUE = 0xff0000ff;
    };
    
    private Image bg_img, fg_img;
    private int width, height;
    
    private static final String WELCOME_SCREEN_IMAGE = "/icon/welcome_screen";
    
    private static int[][] res = {{240, 185}, {160, 125}, {120, 90}, {100, 75}};
    /*
     * @arg frgb The foreground color.
     * @arg brgb The background color.
     */
    public WelcomeScreen(int bg_color) {
        try {
            width = getWidth();
            height = getHeight();
            
            fg_img = Image.createImage(WELCOME_SCREEN_IMAGE + "_" + getBestFit() + ".png");
            bg_img = Image.createImage(width, height);
            Graphics g = bg_img.getGraphics();
            g.setColor(bg_color);
            g.fillRect(0, 0, width, height);
        } catch (IOException e) {
            fg_img = null;
            bg_img = null;
        }
    }
    
    private String getBestFit() {
        int best_fit_index = 0;
        int min_delta = 10000;       // just a big number
        
        for (int i = 0; i < res.length; i++) {
            int delta = Math.abs(res[i][0] - width) + Math.abs(res[i][1] - height);
            if (delta < min_delta) {
                min_delta = delta;
                best_fit_index = i;
            }
        }
        
        return "[" + Integer.toString(res[best_fit_index][0]) + "x" + Integer.toString(res[best_fit_index][1]) + "]";
    }
    
    protected void paint(Graphics g) {
        g.drawImage(bg_img, 0, 0, Graphics.TOP|Graphics.LEFT);
        g.drawImage(fg_img, width/2, height/2, Graphics.HCENTER|Graphics.VCENTER);
        /*
        int width = getWidth(), height = getHeight();
        int diff = Math.abs(frgb - brgb);
        
        for (int i = 0; i < n; i++) {
            int new_rgb = brgb | (int)(diff * i / (double)n);
            int dw = (int)(width * i / (2*(double)n));
            int dh = (int)(height * i / (2*(double)n));
            
            g.setColor(new_rgb);
            g.fillRect(dw, dh, (width - 2*dw), (height - 2*dh));
        }
        */ 
        //g.drawString("Taxi App", n, n, n);
        //g.drawString("Taxi App", this.getWidth()/4, this.getHeight()/2 - 3, LEFT);
    }
}
