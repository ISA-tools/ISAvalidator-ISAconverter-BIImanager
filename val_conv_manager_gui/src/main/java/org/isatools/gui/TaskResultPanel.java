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
import org.isatools.gui.errorprocessing.ErrorReport;
import org.isatools.gui.errorprocessing.ErrorReportUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * ResultPanel
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Dec 1, 2009
 */


public class TaskResultPanel extends JPanel {
    public static final int SUMMARY = 0;
    public static final int WARNINGS = 1;

    private ImageIcon mainImage;
    private ImageIcon anotherImage;
    private ImageIcon anotherImageOver;
    private ImageIcon exitImage;
    private ImageIcon exitImageOver;
    private String message;
    private ErrorReport report;

    private JLabel showSummary;
    private JLabel showWarnings;

    private JPanel swappableUIContainer;
    private Map<Integer, Container> swappableComponents;

    private int currentlySelectedPane;

    public TaskResultPanel(ImageIcon mainImage, ImageIcon anotherImage, ImageIcon anotherImageOver,
                           ImageIcon exitImage, ImageIcon exitImageOver, String message) {
        this(mainImage, anotherImage, anotherImageOver, exitImage, exitImageOver, message, null);
    }

    public TaskResultPanel(ImageIcon mainImage, ImageIcon anotherImage, ImageIcon anotherImageOver,
                           ImageIcon exitImage, ImageIcon exitImageOver, String message, ErrorReport report) {
        this.mainImage = mainImage;
        this.anotherImage = anotherImage;
        this.anotherImageOver = anotherImageOver;
        this.exitImage = exitImage;
        this.exitImageOver = exitImageOver;
        this.message = message;
        this.report = report;

        setPreferredSize(new Dimension(400, 400));
        setLayout(new BorderLayout());
        setOpaque(false);
    }

    public void createGUI() {
        JPanel centralPanel = new JPanel();
        centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.PAGE_AXIS));
        centralPanel.setOpaque(false);


        centralPanel.add(UIHelper.wrapComponentInPanel(new JLabel(mainImage, SwingUtilities.CENTER)));
        centralPanel.add(Box.createVerticalStrut(10));


        // add buttons here to switch between summary and error report if error report isn't null
        if (report != null && report.getReport().size() > 0) {
            swappableUIContainer = new JPanel();
            swappableUIContainer.setOpaque(false);
            // create a tabbed form of view between summary and warnings log.
            centralPanel.add(createMenuPanel());

            swappableComponents = new HashMap<Integer, Container>();
            swappableComponents.put(WARNINGS, createErrorPanel());

            if (message != null) {
                swappableComponents.put(SUMMARY, createEditorPane(UIUtils.constructReport(message)));
                swappableUIContainer.add(swappableComponents.get(SUMMARY));
            } else {
                swappableUIContainer.add(swappableComponents.get(WARNINGS));
            }

            centralPanel.add(swappableUIContainer);
        } else {
            if (message != null) {
                centralPanel.add(createEditorPane(UIUtils.constructReport(message)));
            }
        }


        add(centralPanel);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        final JLabel backButton = new JLabel(anotherImage, JLabel.LEFT);

        backButton.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                backButton.setIcon(anotherImage);
                firePropertyChange("back", "", "goBack");
            }

            public void mouseEntered(MouseEvent event) {
                backButton.setIcon(anotherImageOver);
            }

            public void mouseExited(MouseEvent event) {
                backButton.setIcon(anotherImage);
            }
        });

        buttonPanel.add(backButton, BorderLayout.WEST);

        final JLabel exitButton = new JLabel(exitImage, JLabel.RIGHT);

        exitButton.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                firePropertyChange("exit", "", "exit");
            }

            public void mouseEntered(MouseEvent event) {
                exitButton.setIcon(exitImageOver);
            }

            public void mouseExited(MouseEvent event) {
                exitButton.setIcon(exitImage);
            }
        });

        buttonPanel.add(exitButton, BorderLayout.EAST);

        add(buttonPanel, BorderLayout.SOUTH);

    }

    private JPanel createErrorPanel() {
        final ErrorReportUI errorReportTable = new ErrorReportUI(report);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                errorReportTable.createGUI();
            }
        });

        return errorReportTable;
    }

    private JComponent createEditorPane(String content) {
        JEditorPane report = new JEditorPane();
        report.setContentType("text/html");
        report.setPreferredSize(new Dimension(390, 235));

        report.setEditable(false);
        report.setOpaque(false);

        JScrollPane reportScroller = new JScrollPane(report,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        reportScroller.setOpaque(false);
        reportScroller.getViewport().setOpaque(false);
        reportScroller.setPreferredSize(new Dimension(400, 240));
        reportScroller.setBorder(new EmptyBorder(0, 0, 0, 0));


        report.setText(content);

        return reportScroller;
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel(new FlowLayout());
        menuPanel.setOpaque(false);

        showSummary = createMenuLabel("summary", new ImageIcon(getClass().getResource("/images/DataManager/summary-icon.png")));
        showSummary.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent mouseEvent) {
                showSummary.setForeground(UIHelper.LIGHT_GREEN_COLOR);
            }

            public void mouseExited(MouseEvent mouseEvent) {
                showSummary.setForeground(setExitColour(TaskResultPanel.SUMMARY));
            }

            public void mousePressed(MouseEvent mouseEvent) {
                changeContentPane(swappableComponents.get(TaskResultPanel.SUMMARY));
            }

        });

        showWarnings = createMenuLabel("important warnings", new ImageIcon(getClass().getResource("/images/DataManager/warning-icon.png")));
        showWarnings.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent mouseEvent) {
                showWarnings.setForeground(UIHelper.LIGHT_GREEN_COLOR);
            }

            public void mouseExited(MouseEvent mouseEvent) {
                showWarnings.setForeground(setExitColour(TaskResultPanel.WARNINGS));
            }

            public void mousePressed(MouseEvent mouseEvent) {
                changeContentPane(swappableComponents.get(TaskResultPanel.WARNINGS));
            }

        });
        if (message != null) {
            menuPanel.add(showSummary);
        }

        if (report != null) {
            menuPanel.add(showWarnings);
        }

        return menuPanel;
    }

    private JLabel createMenuLabel(String itemName, ImageIcon itemImage) {
        JLabel lab = new JLabel(itemName, itemImage, SwingConstants.LEFT);
        UIHelper.renderComponent(lab, UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREY_COLOR, false);

        return lab;
    }

    private Color setExitColour(int type) {
        if (currentlySelectedPane == type) {
            return UIHelper.LIGHT_GREEN_COLOR;
        }
        return UIHelper.LIGHT_GREY_COLOR;

    }

    private void changeContentPane(final Container pane) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (pane == swappableComponents.get(TaskResultPanel.SUMMARY)) {
                    currentlySelectedPane = TaskResultPanel.SUMMARY;
                } else if (pane == swappableComponents.get(TaskResultPanel.WARNINGS)) {
                    currentlySelectedPane = TaskResultPanel.WARNINGS;
                }

                swappableUIContainer.removeAll();
                swappableUIContainer.add(pane);
                pane.validate();
                swappableUIContainer.revalidate();
                swappableUIContainer.repaint();
                updateButtonUI();
            }
        });
    }


    private void updateButtonUI() {

        if (currentlySelectedPane == TaskResultPanel.SUMMARY) {
            showSummary.setForeground(UIHelper.LIGHT_GREEN_COLOR);
            showWarnings.setForeground(UIHelper.LIGHT_GREY_COLOR);
        } else if (currentlySelectedPane == TaskResultPanel.WARNINGS) {
            showSummary.setForeground(UIHelper.LIGHT_GREY_COLOR);
            showWarnings.setForeground(UIHelper.LIGHT_GREEN_COLOR);
        }
    }
}
