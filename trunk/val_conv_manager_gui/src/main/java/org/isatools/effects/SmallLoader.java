/**

 The ISAconverter, ISAvalidator & BII Management Tool are components of the ISA software suite (http://www.isa-tools.org)

 Exhibit A
 The ISAconverter, ISAvalidator & BII Management Tool are licensed under the Mozilla Public License (MPL) version
 1.1/GPL version 2.0/LGPL version 2.1

 "The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"). You may not use this file except in compliance with the License.
 You may obtain copies of the Licenses at http://www.mozilla.org/MPL/MPL-1.1.html.

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is the ISAconverter, ISAvalidator & BII Management Tool.

 The Initial Developer of the Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are Copyright (c)
 2007-2011 ISA Team. All Rights Reserved.

 Contributor(s):
 Rocca-Serra P, Brandizi M, Maguire E, Sklyar N, Taylor C, Begley K, Field D,
 Harris S, Hide W, Hofmann O, Neumann S, Sterk P, Tong W, Sansone SA. ISA software suite:
 supporting standards-compliant experimental annotation and enabling curation at the community level.
 Bioinformatics 2010;26(18):2354-6.

 Alternatively, the contents of this file may be used under the terms of either the GNU General
 Public License Version 2 or later (the "GPL") - http://www.gnu.org/licenses/gpl-2.0.html, or
 the GNU Lesser General Public License Version 2.1 or later (the "LGPL") -
 http://www.gnu.org/licenses/lgpl-2.1.html, in which case the provisions of the GPL
 or the LGPL are applicable instead of those above. If you wish to allow use of your version
 of this file only under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your decision by deleting
 the provisions above and replace them with the notice and other provisions required by the
 GPL or the LGPL. If you do not delete the provisions above, a recipient may use your version
 of this file under the terms of any one of the MPL, the GPL or the LGPL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project
 (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC
 (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).

 */

package org.isatools.effects;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class SmallLoader extends JComponent {

    /**
     * Contains the bars composing the circular shape.
     */
    protected Area[] ticker = null;

    /**
     * The animation thread is responsible for fade in/out and rotation.
     */
    protected Thread animation = null;

    /**
     * Notifies whether the animation is running or not.
     */
    protected boolean started = false;

    /**
     * Amount of bars composing the circular shape.
     */
    protected int barsCount;

    /**
     * Amount of frames per seconde. Lowers this to save CPU.
     */
    protected float fps;

    protected String text;

    /**
     * Rendering hints to set anti aliasing.
     */
    protected RenderingHints hints = null;

    public SmallLoader(int barsCount,
                       float fps, int width, int height, String text) {


        this.fps = (fps > 0.0f) ? fps : 15.0f;
        this.barsCount = (barsCount > 0) ? barsCount : 6;

        this.text = text;

        hints = new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        setPreferredSize(new Dimension(width, height + 20));
        setOpaque(false);
    }

    /**
     * Builds a bar.
     *
     * @return Area
     */
    private Area createArea() {
        Rectangle2D.Double body = new Rectangle2D.Double(4, 0, 5, 4);
        return new Area(body);
    }

    /**
     * Builds the circular shape and returns the result as an array of
     * <code>Area</code>. Each <code>Area</code> is one of the bars
     * composing the shape.
     *
     * @return Array of areas
     */
    private Area[] createTicker() {
        Area[] ticker = new Area[barsCount];
        Point2D.Double center = new Point2D.Double((double) getWidth() / 2,
                (double) getHeight() / 2);
        double fixedAngle = (2.0 * Math.PI) / ((double) barsCount);

        for (double i = 0.0; i < (double) barsCount; i++) {
            Area primitive = createArea();
            AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(),
                    center.getY());
            AffineTransform toBorder = AffineTransform.getTranslateInstance(2.0,
                    -2.0);
            AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle,
                    center.getX(), center.getY());

            AffineTransform toWheel = new AffineTransform();
            toWheel.concatenate(toCenter);
            toWheel.concatenate(toBorder);

            primitive.transform(toWheel);
            primitive.transform(toCircle);

            ticker[(int) i] = primitive;
        }

        return ticker;
    }


    public void paintComponent(Graphics g) {
        if (started) {

            double maxY = 0.0;

            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHints(hints);

            for (int i = 0; i < ticker.length; i++) {
                int channel = 224 - (128 / (i + 1));
                g2.setColor(new Color(channel, channel, channel, 60));
                g2.fill(ticker[i]);


                try {
                    Rectangle2D bounds = ticker[i].getBounds2D();

                    if (bounds.getMaxY() > maxY) {
                        maxY = bounds.getMaxY();
                    }
                } catch (ArrayIndexOutOfBoundsException ae) {
                    //
                }
            }

            g2.setColor(UIHelper.LIGHT_GREY_COLOR);
            g2.setFont(UIHelper.VER_12_BOLD);

            FontMetrics fm = g2.getFontMetrics(UIHelper.VER_12_BOLD);
            int stringSize = fm.stringWidth(text);

            g2.drawString(text, getWidth() / 2 - stringSize / 2, getHeight() - 15);

        }
    }

    public void start() {
        setVisible(true);
        ticker = createTicker();
        if (animation != null) {
            animation.interrupt();
            animation = null;
        }
        animation = new Thread(new Animator());
        animation.start();

    }

    /**
     * Stops the waiting animation by stopping the rotation
     * of the circular shape and then by fading out the veil.
     * This methods sets the panel invisible at the end.
     */
    public void stop() {
        if (animation != null) {
            animation.interrupt();
            animation = null;
            setVisible(false);
        }
    }

    /**
     * Animation thread.
     */
    private class Animator implements Runnable {

        public void run() {
            try {
                Point2D.Double center = new Point2D.Double((double) getWidth() / 2,
                        (double) getHeight() / 2);
                double fixedIncrement = (2.0 * Math.PI) / ((double) barsCount);
                AffineTransform toCircle = AffineTransform.getRotateInstance(fixedIncrement,
                        center.getX(), center.getY());

                started = true;


                while (!Thread.interrupted()) {

                    for (Area aTicker : ticker) {
                        aTicker.transform(toCircle);
                    }
                    repaint();

                    try {
                        Thread.sleep((int) (1000 / fps));
                    } catch (InterruptedException ie) {
                        break;
                    }

                    Thread.yield();
                }
            } catch (OutOfMemoryError oome) {
                System.out.println("out of memory...attempting clean up of objects using garbage collector");
                System.gc();
            }

        }
    }
}
