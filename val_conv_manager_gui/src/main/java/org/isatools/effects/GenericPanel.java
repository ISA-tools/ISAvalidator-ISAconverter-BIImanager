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
import java.awt.geom.GeneralPath;


public abstract class GenericPanel extends JPanel {
    protected RenderingHints hints;
    protected int counter = 0;
    protected Color start = new Color(0, 104, 56, 200);
    protected Color end = new Color(141, 198, 63, 0);


    public GenericPanel() {
        this(new BorderLayout());
    }

    public GenericPanel(LayoutManager manager) {
        super(manager);
        hints = createRenderingHints();
    }

    protected RenderingHints createRenderingHints() {
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        hints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        return hints;
    }

    public void animate() {
        counter++;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        RenderingHints oldHints = g2.getRenderingHints();
        g2.setRenderingHints(hints);

        float width = getWidth();
        float height = getHeight();

        g2.translate(0, -30);

        drawCurve(g2,
                20.0f, -10.0f, 20.0f, -10.0f,
                width / 2.0f - 40.0f, 10.0f,
                0.0f, -5.0f,
                width / 2.0f + 40, 1.0f,
                0.0f, 5.0f,
                50.0f, 5.0f, false);

        drawHeader(g2, (int) width, (int) height);

        drawCentralPanel(g2, (int) width, (int) height);


        g2.translate(0, 30);
        g2.translate(0, height - 60);

        drawCurve(g2,
                30.0f, -15.0f, 50.0f, 15.0f,
                width / 2.0f - 40.0f, 1.0f,
                15.0f, -25.0f,
                width / 2.0f, 1.0f / 2.0f,
                0.0f, 25.0f,
                15.0f, 9.0f, false);

        g2.translate(0, -height + 60);

        drawCurve(g2,
                height - 35.0f, -5.0f, height - 50.0f, 10.0f,
                width / 2.0f - 40.0f, 1.0f,
                height - 35.0f, -25.0f,
                width / 2.0f, 1.0f / 2.0f,
                height - 20.0f, 25.0f,
                25.0f, 7.0f, true);

        drawHelpPanel(g2, (int) width, (int) height);


        g2.setRenderingHints(oldHints);
    }

    protected abstract void drawHeader(Graphics2D g2, int width, int height);

    protected abstract void drawCentralPanel(Graphics2D g2, int width, int height);

    protected abstract void drawHelpPanel(Graphics2D g2, int width, int height);

    protected void drawCurve(Graphics2D g2,
                             float y1, float y1_offset,
                             float y2, float y2_offset,
                             float cx1, float cx1_offset,
                             float cy1, float cy1_offset,
                             float cx2, float cx2_offset,
                             float cy2, float cy2_offset,
                             float thickness,
                             float speed,
                             boolean invert) {
        float width = getWidth();

        float offset = (float) Math.sin(counter / (speed * Math.PI));

        float start_x = 0.0f;
        float start_y = offset * y1_offset + y1;
        float end_y = offset * y2_offset + y2;

        float ctrl1_x = offset * cx1_offset + cx1;
        float ctrl1_y = offset * cy1_offset + cy1;
        float ctrl2_x = offset * cx2_offset + cx2;
        float ctrl2_y = offset * cy2_offset + cy2;

        GeneralPath thickCurve = new GeneralPath();
        thickCurve.moveTo(start_x, start_y);
        thickCurve.curveTo(ctrl1_x, ctrl1_y,
                ctrl2_x, ctrl2_y,
                width, end_y);
        thickCurve.lineTo(width, end_y + thickness);
        thickCurve.curveTo(ctrl2_x, ctrl2_y + thickness,
                ctrl1_x, ctrl1_y + thickness,
                start_x, start_y + thickness);
        thickCurve.lineTo(start_x, start_y);

        Rectangle bounds = thickCurve.getBounds();
        if (!bounds.intersects(g2.getClipBounds())) {
            return;
        }

        GradientPaint painter = new GradientPaint(0, bounds.y,
                invert ? end : start,
                0, bounds.y + bounds.height,
                invert ? start : end);

        Paint oldPainter = g2.getPaint();
        g2.setPaint(painter);
        g2.fill(thickCurve);

        g2.setPaint(oldPainter);
    }
}
