package org.noisesmith.projector;

import java.util.Collections;
import java.util.Arrays;
import java.util.LinkedList;
import java.io.File;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.concurrent.SynchronousQueue;

public class Projector {
    static FootPedal pedal;

    public static void main(String[] args) {
        final String name = String.join(" ", args);
        final SynchronousQueue<FootPedal.Event>
            midiEvents = new SynchronousQueue<FootPedal.Event>();
        final File[] images = getImages("images/");
        pedal = new FootPedal("SSCOM.*", midiEvents);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI(name, midiEvents, images);
                }
            });
    }

    private static File[] getImages(String root) {
        return getImages(new File(root));
    }
    private static File[] getImages(File[] files) {
        Arrays.sort(files);
        LinkedList<File> images = new LinkedList<File>();
        for(File f : files) {
            for(File child : getImages(f)) {
                images.push(child);
            }
        }
        Collections.reverse(images);
        return images.toArray(new File[0]);
    }
    private static File[] getImages(File top) {
        File[] children = top.listFiles();
        if (children != null && children.length > 0) {
            return getImages(children);
        } else {
            return new File[] {top};
        }
    }

    private static void createAndShowGUI(String name,
                                         SynchronousQueue<FootPedal.Event>
                                         midiEvents,
                                         File[] images) {
        JFrame f = new JFrame("Projector " + name);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new ProjectorPanel(midiEvents, images));
        f.setSize(250,250);
        f.setVisible(true);
    }
}

class ProjectorPanel extends JPanel {
    String eventStatus = "";
    BufferedImage[] images;
    int position;

    void loadImages(File[] files) {
        position = 0;
        images = new BufferedImage[files.length];
        try {
            for(int i = 0; i < images.length; i++) {
                images[i] = ImageIO.read(files[i]);
            }
        }catch (Exception e) {
            System.out.println("could not load images");
            e.printStackTrace();
            images = new BufferedImage[0];
        }
    }

    public ProjectorPanel(final SynchronousQueue<FootPedal.Event> queue,
                          File[] imgs) {
        loadImages(imgs);
        Thread midi = new Thread(new Runnable() {
                public void run() {
                    while(true) {
                        try {
                            FootPedal.Event evt = queue.take();
                            eventStatus = evt.toString();
                            if (evt.type == FootPedal.Events.FOOT_DOWN) {
                                if (evt.parameter == 1) {
                                    position--;
                                } else if (evt.parameter == 2) {
                                    position++;
                                }
                            }
                            repaint();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        midi.start();
    }

    public Dimension getPreferredSize() {
        return new Dimension(250,200);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString("At " + System.currentTimeMillis() + " " + eventStatus,
                     10, 20);
        while (position < 0) position += images.length;
        position %= images.length;
        if(images[position] != null)
            g.drawImage(images[position], 10, 50, null);
        repaint(10, 20, 500, 200);
    }
}
