package org.noisesmith.projector;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics; 
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseMotionAdapter;

public class Projector {
    public static void main(String[] args) {
        String name = String.join(" ", args);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI(name); 
                }
            });
    }

    private static void createAndShowGUI(String name) {
        JFrame f = new JFrame("Projector " + name);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        f.add(new ProjectorPanel());
        f.setSize(250,250);
        f.setVisible(true);
    } 
}

class ProjectorPanel extends JPanel {

    RedSquare redSquare = new RedSquare();

    public ProjectorPanel() {
        addMouseListener(new MouseAdapter(){
                public void mousePressed(MouseEvent e){
                    moveSquare(e.getX(),e.getY());
                }
            });
        addMouseMotionListener(new MouseAdapter(){
                public void mouseDragged(MouseEvent e){
                    moveSquare(e.getX(),e.getY());
                }
            });
    }

    private void moveSquare(int x, int y){
        final int CURR_X = redSquare.getX();
        final int CURR_Y = redSquare.getY();
        final int CURR_W = redSquare.getWidth();
        final int CURR_H = redSquare.getHeight();
        final int OFFSET = 1;
        if ((CURR_X!=x) || (CURR_Y!=y)) {
            repaint(CURR_X,CURR_Y,CURR_W+OFFSET,CURR_H+OFFSET);
            redSquare.setX(x);
            redSquare.setY(y);
            repaint(redSquare.getX(), redSquare.getY(), 
                    redSquare.getWidth()+OFFSET, 
                    redSquare.getHeight()+OFFSET);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(250,200);
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);       
        g.drawString("This is my custom Panel!",10,20);
        redSquare.paintSquare(g);
    }  
}

class RedSquare{

    private int xPos = 50;
    private int yPos = 50;
    private int width = 20;
    private int height = 20;

    public void setX(int xPos){ 
        this.xPos = xPos;
    }

    public int getX(){
        return xPos;
    }

    public void setY(int yPos){
        this.yPos = yPos;
    }

    public int getY(){
        return yPos;
    }

    public int getWidth(){
        return width;
    } 

    public int getHeight(){
        return height;
    }

    public void paintSquare(Graphics g){
        g.setColor(Color.RED);
        g.fillRect(xPos,yPos,width,height);
        g.setColor(Color.BLACK);
        g.drawRect(xPos,yPos,width,height);  
    }
}
