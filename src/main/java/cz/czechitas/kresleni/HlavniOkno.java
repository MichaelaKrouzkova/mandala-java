package cz.czechitas.kresleni;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import net.miginfocom.swing.*;
import net.sevecek.util.*;

public class HlavniOkno extends JFrame {

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    JLabel labBarva1;
    JLabel labBarva2;
    JLabel labBarva3;
    JLabel labBarva4;
    JLabel labPlocha;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    JPanel contentPane;
    MigLayout migLayoutManager;
    Graphics2D stetec;
    BufferedImage obrazek;
    Color vybranaBarva;

    public HlavniOkno() {
        initComponents();
        nahrajVychoziObrazek();
        stetec.setStroke(new BasicStroke(10));
        stetec.setColor(labBarva1.getBackground());

    }

    //nahraj vychozi vlozenou mandalu
    private void nahrajVychoziObrazek() {
        try {
            InputStream zdrojObrazku = getClass().getResourceAsStream("/vychozi-mandala.png");
            obrazek = ImageIO.read(zdrojObrazku);
            labPlocha.setIcon(new ImageIcon(obrazek));
            labPlocha.setMinimumSize(new Dimension(obrazek.getWidth(), obrazek.getHeight()));
            stetec = (Graphics2D) obrazek.getGraphics();
        } catch (IOException ex) {
            throw new ApplicationPublicException(ex, "Nepodařilo se nahrát zabudovaný obrázek mandaly:\n\n" + ex.getMessage());
        }
    }

    // Pri kliknuti vybarvi plochu zvolenou barvou//zkopirovat z txt
    private void priKlikuNaObrazek(MouseEvent e) {
        vyplnObrazek(obrazek, e.getX(), e.getY(), vybranaBarva);
        labPlocha.repaint();
    }

    // Pri kliknuti na pole s barvou nastav tuhle zvolenou barvu
    private void priKliknutiNaBarvu(MouseEvent e) {
        JLabel labBarva = (JLabel) e.getSource();
        vybranaBarva = labBarva.getBackground();
        stetec.setColor(vybranaBarva);
        labBarva1.setText("");
        labBarva2.setText("");
        labBarva3.setText("");
        labBarva4.setText("");
        labBarva.setText("X");
    }

    // Zkopirovano y txt vypln obrazek

    /**
     * Vyplni <code>BufferedImage obrazek</code>
     * na pozicich <code>int x</code>, <code>int y</code>
     * barvou <code>Color barva</code>
     */
    public void vyplnObrazek(BufferedImage obrazek, int x, int y, Color barva) {
        if (barva == null) {
            barva = new Color(100, 20, 250);
        }

        // Zamez vyplnovani mimo rozsah
        if (x < 0 || x >= obrazek.getWidth() || y < 0 || y >= obrazek.getHeight()) {
            return;
        }

        WritableRaster pixely = obrazek.getRaster();
        int[] novyPixel = new int[] {barva.getRed(), barva.getGreen(), barva.getBlue(), barva.getAlpha()};
        int[] staryPixel = new int[] {255, 255, 255, 255};
        staryPixel = pixely.getPixel(x, y, staryPixel);

        // Pokud uz je pocatecni pixel obarven na cilovou barvu, nic nemen
        if (pixelyMajiStejnouBarvu(novyPixel, staryPixel)) {
            return;
        }

        // Zamez prebarveni cerne cary
        int[] cernyPixel = new int[] {0, 0, 0, staryPixel[3]};
        if (pixelyMajiStejnouBarvu(cernyPixel, staryPixel)) {
            return;
        }

        vyplnovaciSmycka(pixely, x, y, novyPixel, staryPixel);
    }

    /**
     * Provede skutecne vyplneni pomoci zasobniku
     */
    private void vyplnovaciSmycka(WritableRaster raster, int x, int y, int[] novaBarva, int[] nahrazovanaBarva) {
        Rectangle rozmery = raster.getBounds();
        int[] aktualniBarva = new int[] {255, 255, 255, 255};

        Deque<Point> zasobnik = new ArrayDeque<>(rozmery.width * rozmery.height);
        zasobnik.push(new Point(x, y));
        while (zasobnik.size() > 0) {
            Point point = zasobnik.pop();
            x = point.x;
            y = point.y;
            if (!pixelyMajiStejnouBarvu(raster.getPixel(x, y, aktualniBarva), nahrazovanaBarva)) {
                continue;
            }

            // Najdi levou zed, po ceste vyplnuj
            int levaZed = x;
            do {
                raster.setPixel(levaZed, y, novaBarva);
                levaZed--;
            }
            while (levaZed >= 0 && pixelyMajiStejnouBarvu(raster.getPixel(levaZed, y, aktualniBarva), nahrazovanaBarva));
            levaZed++;

            // Najdi pravou zed, po ceste vyplnuj
            int pravaZed = x;
            do {
                raster.setPixel(pravaZed, y, novaBarva);
                pravaZed++;
            }
            while (pravaZed < rozmery.width && pixelyMajiStejnouBarvu(raster.getPixel(pravaZed, y, aktualniBarva), nahrazovanaBarva));
            pravaZed--;

            // Pridej na zasobnik body nahore a dole
            for (int i = levaZed; i <= pravaZed; i++) {
                if (y > 0 && pixelyMajiStejnouBarvu(raster.getPixel(i, y - 1, aktualniBarva), nahrazovanaBarva)) {
                    if (!(i > levaZed && i < pravaZed
                            && pixelyMajiStejnouBarvu(raster.getPixel(i - 1, y - 1, aktualniBarva), nahrazovanaBarva)
                            && pixelyMajiStejnouBarvu(raster.getPixel(i + 1, y - 1, aktualniBarva), nahrazovanaBarva))) {
                        zasobnik.add(new Point(i, y - 1));
                    }
                }
                if (y < rozmery.height - 1 && pixelyMajiStejnouBarvu(raster.getPixel(i, y + 1, aktualniBarva), nahrazovanaBarva)) {
                    if (!(i > levaZed && i < pravaZed
                            && pixelyMajiStejnouBarvu(raster.getPixel(i - 1, y + 1, aktualniBarva), nahrazovanaBarva)
                            && pixelyMajiStejnouBarvu(raster.getPixel(i + 1, y + 1, aktualniBarva), nahrazovanaBarva))) {
                        zasobnik.add(new Point(i, y + 1));
                    }
                }
            }
        }
    }

    /**
     * Vrati true pokud RGB hodnoty v polich jsou stejne. Pokud jsou ruzne, vraci false
     */
    private boolean pixelyMajiStejnouBarvu(int[] barva1, int[] barva2) {
        return barva1[0] == barva2[0] && barva1[1] == barva2[1] && barva1[2] == barva2[2];
    }

   


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        labBarva1 = new JLabel();
        labBarva2 = new JLabel();
        labBarva3 = new JLabel();
        labBarva4 = new JLabel();
        labPlocha = new JLabel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Mandala");
        Container contentPane = getContentPane();
        contentPane.setLayout(new MigLayout(
            "insets rel,hidemode 3",
            // columns
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[grow,fill]",
            // rows
            "[]" +
            "[grow,fill]"));
        this.contentPane = (JPanel) this.getContentPane();
        this.contentPane.setBackground(this.getBackground());
        LayoutManager layout = this.contentPane.getLayout();
        if (layout instanceof MigLayout) {
            this.migLayoutManager = (MigLayout) layout;
        }

        //---- labBarva1 ----
        labBarva1.setOpaque(true);
        labBarva1.setMinimumSize(new Dimension(32, 32));
        labBarva1.setBackground(new Color(100, 20, 250));
        labBarva1.setText("X");
        labBarva1.setForeground(Color.white);
        labBarva1.setHorizontalAlignment(SwingConstants.CENTER);
        labBarva1.setFont(labBarva1.getFont().deriveFont(labBarva1.getFont().getStyle() | Font.BOLD));
        labBarva1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priKliknutiNaBarvu(e);
            }
        });
        contentPane.add(labBarva1, "cell 0 0");

        //---- labBarva2 ----
        labBarva2.setOpaque(true);
        labBarva2.setMinimumSize(new Dimension(32, 32));
        labBarva2.setBackground(new Color(0, 180, 60));
        labBarva2.setForeground(Color.white);
        labBarva2.setHorizontalAlignment(SwingConstants.CENTER);
        labBarva2.setFont(labBarva2.getFont().deriveFont(labBarva2.getFont().getStyle() | Font.BOLD));
        labBarva2.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priKliknutiNaBarvu(e);
            }
        });
        contentPane.add(labBarva2, "cell 1 0");

        //---- labBarva3 ----
        labBarva3.setOpaque(true);
        labBarva3.setMinimumSize(new Dimension(32, 32));
        labBarva3.setBackground(new Color(250, 0, 0));
        labBarva3.setForeground(Color.white);
        labBarva3.setHorizontalAlignment(SwingConstants.CENTER);
        labBarva3.setFont(labBarva3.getFont().deriveFont(labBarva3.getFont().getStyle() | Font.BOLD));
        labBarva3.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priKliknutiNaBarvu(e);
            }
        });
        contentPane.add(labBarva3, "cell 2 0");

        //---- labBarva4 ----
        labBarva4.setOpaque(true);
        labBarva4.setMinimumSize(new Dimension(32, 32));
        labBarva4.setBackground(new Color(255, 255, 50));
        labBarva4.setForeground(Color.white);
        labBarva4.setHorizontalAlignment(SwingConstants.CENTER);
        labBarva4.setFont(labBarva4.getFont().deriveFont(labBarva4.getFont().getStyle() | Font.BOLD));
        labBarva4.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priKliknutiNaBarvu(e);
            }
        });
        contentPane.add(labBarva4, "cell 3 0");

        //---- labPlocha ----
        labPlocha.setVerticalAlignment(SwingConstants.TOP);
        labPlocha.setBackground(new Color(204, 255, 255));
        labPlocha.setOpaque(true);
        labPlocha.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                priKlikuNaObrazek(e);
            }
        });
        contentPane.add(labPlocha, "cell 0 1 5 1");
        setSize(638, 494);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}




    

