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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class FooterPanel extends JComponent {
    private JFrame container;

    // Size of area on screen which will instigate display of the resize cursor
    private static Rectangle mouseTargetArea = new Rectangle(15, 38);
    private Point point = new Point();
    private boolean resizing = false;
    private ImageIcon resizeIcon;

    public FooterPanel(JFrame container) {
        this(container, UIHelper.BG_COLOR, new ImageIcon(
                FooterPanel.class.getResource("/images/effects/resize_active.png")));
    }

    public FooterPanel(final JFrame container, Color bgColor, ImageIcon resizeIcon) {
        this.container = container;
        setLayout(new GridBagLayout());
        setBackground(bgColor);
        this.resizeIcon = resizeIcon;
        instantiateComponent();
        installListeners();

        this.container.getContentPane().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                resizing = container.getCursor().equals(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                if (!e.isMetaDown()) {
                    point.x = e.getX();
                    point.y = e.getY();
                }
            }
        });
    }

    private void installListeners() {

        container.getContentPane().addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (resizing) {
                    container.setSize(container.getWidth() + e.getX() - point.x, container.getHeight() + e.getY() - point.y);
                    point.x = e.getX();
                    point.y = e.getY();
                } else if (!e.isMetaDown()) {
                    Point p = container.getLocation();
                    container.setLocation(p.x + e.getX() - point.x,
                            p.y + e.getY() - point.y);
                }
            }

            public void mouseMoved(MouseEvent me) {

                Point cursorLocation = MouseInfo.getPointerInfo().getLocation();
                if (checkIfMouseIsInBounds(cursorLocation)) {
                    container.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else {
                    container.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }

    /**
     * Method checks whether or not the mouse is in a location to instigate a resize operation,
     * whereby the application will display a resize cursor to tell the user that they can resize.
     *
     * @param mouseLocation - Point object indicating the mouse location on the screen.
     * @return false if not in a valid location, true otherwise.
     */
    private boolean checkIfMouseIsInBounds(Point mouseLocation) {
        Point containerLocation = container.getLocationOnScreen();
        Dimension containerSize = container.getSize();
        Rectangle bounds = new Rectangle((containerLocation.x + containerSize.width) - mouseTargetArea.width, (containerLocation.y + containerSize.height) - mouseTargetArea.height,
                mouseTargetArea.width, mouseTargetArea.height);
        return bounds.contains(mouseLocation);
    }

    private void instantiateComponent() {
        add(Box.createHorizontalGlue(),
                new GridBagConstraints(0, 0,
                        1, 1,
                        1.0, 1.0,
                        GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0),
                        0, 0));

        JLabel resizeButton = new JLabel(resizeIcon);

        add(resizeButton, new GridBagConstraints(2, 0,
                1, 1,
                0.0, 1.0,
                GridBagConstraints.NORTHEAST,
                GridBagConstraints.NONE,
                new Insets(1, 0, 0, 2),
                0, 0));
    }

}