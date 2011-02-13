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

package org.isatools.gui;

import org.isatools.effects.UIHelper;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class TitlePanel extends JComponent {
    private JButton closeButton;
    private JButton iconifyButton;

    private int preferredHeight = 26;
    private Image backgroundGradient = new ImageIcon(getClass().getResource("/images/titlebar/title-background.png")).getImage();
    private Image backgroundGradientInactive = new ImageIcon(getClass().getResource("/images/titlebar/title-background-inactive.png")).getImage();

    private Image close = new ImageIcon(getClass().getResource("/images/titlebar/title-close.png")).getImage();
    private Image closeInactive = new ImageIcon(getClass().getResource("/images/titlebar/title-close-inactive.png")).getImage();
    private Image closeOver = new ImageIcon(getClass().getResource("/images/titlebar/title-close-over.png")).getImage();
    private Image closePressed = new ImageIcon(getClass().getResource("/images/titlebar/title-close-pressed.png")).getImage();
    private Image minimize = new ImageIcon(getClass().getResource("/images/titlebar/title-minimize.png")).getImage();
    private Image minimizeInactive = new ImageIcon(getClass().getResource("/images/titlebar/title-minimize-inactive.png")).getImage();
    private Image minimizeOver = new ImageIcon(getClass().getResource("/images/titlebar/title-minimize-over.png")).getImage();
    private Image minimizePressed = new ImageIcon(getClass().getResource("/images/titlebar/title-minimize-pressed.png")).getImage();
    private boolean showClose;
    private boolean showIconify;


    public TitlePanel() {
        this(true, true);
    }

    public TitlePanel(boolean showClose, boolean showIconify) {
        this.showClose = showClose;
        this.showIconify = showIconify;
        setLayout(new GridBagLayout());
        createButtons();
        setBackground(UIHelper.GREY_COLOR);
    }

    public void installListeners() {
        MouseInputHandler handler = new MouseInputHandler();
        Window window = SwingUtilities.getWindowAncestor(this);
        window.addMouseListener(handler);
        window.addMouseMotionListener(handler);

        window.addWindowListener(new WindowHandler());
    }

    private void createButtons() {
        add(Box.createHorizontalGlue(),
                new GridBagConstraints(0, 0,
                        1, 1,
                        1.0, 1.0,
                        GridBagConstraints.EAST,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0),
                        0, 0));

        if (showIconify) {
            add(iconifyButton = createButton(new IconifyAction(),
                    minimize, minimizePressed, minimizeOver),
                    new GridBagConstraints(1, 0,
                            1, 1,
                            0.0, 1.0,
                            GridBagConstraints.NORTHEAST,
                            GridBagConstraints.NONE,
                            new Insets(1, 0, 0, 2),
                            0, 0));
        }

        if (showClose) {
            add(closeButton = createButton(new CloseAction(),
                    close, closePressed, closeOver),
                    new GridBagConstraints(2, 0,
                            1, 1,
                            0.0, 1.0,
                            GridBagConstraints.NORTHEAST,
                            GridBagConstraints.NONE,
                            new Insets(1, 0, 0, 2),
                            0, 0));
        }
    }

    private static JButton createButton(final AbstractAction action,
                                        final Image image,
                                        final Image pressedImage,
                                        final Image overImage) {
        JButton button = new JButton(action);
        button.setIcon(new ImageIcon(image));
        button.setPressedIcon(new ImageIcon(pressedImage));
        button.setRolloverIcon(new ImageIcon(overImage));
        button.setRolloverEnabled(true);
        button.setBorder(null);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusable(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(image.getWidth(null),
                image.getHeight(null)));
        return button;
    }

    private void close() {
        Window w = SwingUtilities.getWindowAncestor(this);
        w.dispatchEvent(new WindowEvent(w,
                WindowEvent.WINDOW_CLOSING));
    }

    private void iconify() {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            frame.setExtendedState(frame.getExtendedState() | Frame.ICONIFIED);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.height = preferredHeight;
        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension size = super.getMinimumSize();
        size.height = preferredHeight;
        return size;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension size = super.getMaximumSize();
        size.height = preferredHeight;
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) {
            return;
        }

        boolean active = SwingUtilities.getWindowAncestor(this).isActive();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        Rectangle clip = g2.getClipBounds();


        g2.drawImage(active ? backgroundGradient : backgroundGradientInactive,
                clip.x, 0, clip.width, getHeight() - 2, null);

        g2.setColor(active ? new Color(51, 51, 51) : new Color(51, 51, 51, 100));
        g2.drawLine(0, getHeight(), getWidth(), getHeight());

        g2.setColor(active ? new Color(153, 153, 153) : new Color(153, 153, 153, 100));
        g2.drawLine(0, getHeight(), getWidth(), getHeight());

        drawGrip(g2, active);
        drawTitle(g2);
    }

    protected abstract void drawGrip(Graphics2D g2d, boolean active);

    protected abstract void drawTitle(Graphics2D g2d);

    private class CloseAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            close();
        }
    }

    private class IconifyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            iconify();
        }
    }

    private class MouseInputHandler implements MouseInputListener {
        private boolean isMovingWindow;
        private int dragOffsetX;
        private int dragOffsetY;

        private static final int BORDER_DRAG_THICKNESS = 5;

        public void mousePressed(MouseEvent ev) {
            Point dragWindowOffset = ev.getPoint();
            Window w = (Window) ev.getSource();
            if (w != null) {
                w.toFront();
            }
            Point convertedDragWindowOffset = SwingUtilities.convertPoint(
                    w, dragWindowOffset, TitlePanel.this);

            Frame f = null;
            Dialog d = null;

            if (w instanceof Frame) {
                f = (Frame) w;
            } else if (w instanceof Dialog) {
                d = (Dialog) w;
            }

            int frameState = (f != null) ? f.getExtendedState() : 0;

            if (TitlePanel.this.contains(convertedDragWindowOffset)) {
                if ((f != null && ((frameState & Frame.MAXIMIZED_BOTH) == 0)
                        || (d != null))
                        && dragWindowOffset.y >= BORDER_DRAG_THICKNESS
                        && dragWindowOffset.x >= BORDER_DRAG_THICKNESS
                        && dragWindowOffset.x < w.getWidth()
                        - BORDER_DRAG_THICKNESS) {
                    isMovingWindow = true;
                    dragOffsetX = dragWindowOffset.x;
                    dragOffsetY = dragWindowOffset.y;
                }
            } else if (f != null && f.isResizable()
                    && ((frameState & Frame.MAXIMIZED_BOTH) == 0)
                    || (d != null && d.isResizable())) {
                dragOffsetX = dragWindowOffset.x;
                dragOffsetY = dragWindowOffset.y;
            }
        }

        public void mouseReleased(MouseEvent ev) {
            isMovingWindow = false;
        }

        public void mouseDragged(MouseEvent ev) {
            Window w = (Window) ev.getSource();

            if (isMovingWindow) {
                Point windowPt = MouseInfo.getPointerInfo().getLocation();
                windowPt.x = windowPt.x - dragOffsetX;
                windowPt.y = windowPt.y - dragOffsetY;
                w.setLocation(windowPt);
            }
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    private class WindowHandler extends WindowAdapter {
        @Override
        public void windowActivated(WindowEvent ev) {
            if (showClose) {
                closeButton.setIcon(new ImageIcon(close));
            }
            if (showIconify) {
                iconifyButton.setIcon(new ImageIcon(minimize));
            }
            getRootPane().repaint();
        }

        @Override
        public void windowDeactivated(WindowEvent ev) {
            if (showClose) {
                closeButton.setIcon(new ImageIcon(closeInactive));
            }
            if (showIconify) {
                iconifyButton.setIcon(new ImageIcon(minimizeInactive));
            }
            getRootPane().repaint();
        }
    }
}
