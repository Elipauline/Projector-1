package org.noisesmith.projector;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Color;
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
        ArrayDeque<File> images = new ArrayDeque<File>();
        for(File f : files) {
            for(File child : getImages(f)) {
                images.addLast(child);
            }
        }
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
    int imageIndex;
    double imageHeight;
    public double scrolldelta = 1.1;
    public double scrollspeed = 1.0;

    void loadImages(File[] files) {
        ArrayDeque<BufferedImage> deq = new ArrayDeque<BufferedImage>();
        imageIndex = 0;
        for(File file : files) {
            try {
                deq.addLast(ImageIO.read(file));
            } catch (Exception e) {
                System.out.println("could not load image " + file);
                e.printStackTrace();
            }
        }
        images = deq.toArray(new BufferedImage[0]);
    }

    TimerTask scroller () {
        return new TimerTask() {
            @Override
            public void run () {
                scrollspeed *= scrolldelta;
                imageHeight += scrollspeed;
            }
        };
    }

    public ProjectorPanel(final SynchronousQueue<FootPedal.Event> queue,
                          File[] imgs) {
        loadImages(imgs);
        Thread midi = new Thread(new Runnable() {
                public void run() {
                    Timer periodic = new Timer("projector periodic");
                    TimerTask task = null;
                    while(true) {
                        try {
                            FootPedal.Event evt = queue.take();
                            eventStatus = evt.toString();
                            if (evt.type == FootPedal.Events.FOOT_UP) {
                                if (task != null) task.cancel();
                                task = null;
                                periodic.purge();
                            }
                            if (evt.type == FootPedal.Events.FOOT_DOWN) {
                                switch (evt.parameter) {
                                case 1:
                                    imageIndex--;
                                    imageHeight = 0;
                                    break;
                                case 2:
                                    imageIndex++;
                                    imageHeight = 0;
                                    break;
                                case 6:
                                    if (task != null) task.cancel();
                                    task = null;
                                    periodic.purge();
                                    scrollspeed = -5.0;
                                    task = scroller();
                                    periodic.schedule(task, 0, 100);
                                    break;
                                case 7:
                                    if (task != null) task.cancel();
                                    task = null;
                                    periodic.purge();
                                    scrollspeed = 5.0;
                                    task = scroller();
                                    periodic.schedule(task, 0, 100);
                                    break;
                                default:
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
        Rectangle rect = new Rectangle();
        rect = g.getClipBounds(rect);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, (int) rect.getWidth(), (int) rect.getHeight());
        // g.drawString("At " + System.currentTimeMillis() + " " + eventStatus,
        // 10, 20);
        while (imageIndex < 0) imageIndex += images.length;
        imageIndex %= images.length;
        BufferedImage i = images[imageIndex];
        int offset = (int) (rect.getWidth() - i.getWidth()) / 2;
        g.drawImage(images[imageIndex], offset, (int) imageHeight, null);
        repaint(0, 0, (int) rect.getWidth(), (int) rect.getHeight());
    }
}
