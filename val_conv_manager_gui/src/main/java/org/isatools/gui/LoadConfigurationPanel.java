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

import org.isatools.effects.RoundedBorder;
import org.isatools.effects.UIHelper;
import org.isatools.fileutils.FileUtils;
import org.isatools.isatab.commandline.AbstractImportLayerShellCommand;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * LoadConfigurationPanel
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Oct 27, 2009
 */


public class LoadConfigurationPanel extends JPanel {

    private static final String CONFIG_DIR = AbstractImportLayerShellCommand.getConfigPath();

    @InjectedResource
    private ImageIcon searchIcon, searchOverIcon, loadIcon, loadOverIcon, loadConfigurationHeader, configListItem;

    private DefaultListModel defaultListModel;
    private String problemLog;
    private JEditorPane problemReporter;

    private File[] configurationFiles = null;
    private JList configurations;

    public LoadConfigurationPanel() {

        setPreferredSize(new Dimension(300, 300));
        setLayout(new BorderLayout());
        setOpaque(false);
    }

    public void createGUI() {
        ResourceInjector.get("gui-package.style").inject(this);

        Box container = Box.createVerticalBox();
        container.setOpaque(false);

        problemReporter = new JEditorPane();
        UIHelper.renderComponent(problemReporter, UIHelper.VER_12_PLAIN, UIHelper.RED_COLOR, false);
        problemReporter.setOpaque(false);
        problemReporter.setContentType("text/html");
        problemReporter.setEditable(false);
        problemReporter.setPreferredSize(new Dimension(300, 75));


        defaultListModel = new DefaultListModel();

        configurationFiles = updatePreviousConfigurations();

        configurations = new JList(defaultListModel);
        configurations.setBorder(null);
        configurations.setOpaque(false);

        configurations.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                if (event.getClickCount() >= 2) {
                    getSelectedConfigurationAndLoad();
                }
            }
        });

        configurations.setCellRenderer(new CustomListCellRenderer());

        JScrollPane listScroller = new JScrollPane(configurations,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listScroller.setBorder(new TitledBorder(
                new RoundedBorder(UIHelper.LIGHT_GREEN_COLOR, 6),
                "select configuration...",
                TitledBorder.DEFAULT_POSITION,
                TitledBorder.ABOVE_TOP, UIHelper.VER_14_BOLD,
                UIHelper.LIGHT_GREY_COLOR));
        listScroller.setOpaque(false);
        listScroller.setPreferredSize(new Dimension(300, 150));
        listScroller.getViewport().setOpaque(false);

        //top container contains images for refreshing the list, and the menu title image :o)
        JPanel topContainer = new JPanel(new GridLayout(1, 1));
        topContainer.setOpaque(false);

        JLabel loadISAImage = new JLabel(loadConfigurationHeader,
                JLabel.RIGHT);
        loadISAImage.setForeground(UIHelper.DARK_GREEN_COLOR);

        topContainer.add(loadISAImage);

        container.add(topContainer);
        container.add(Box.createVerticalStrut(15));
        container.add(listScroller);
        container.add(Box.createVerticalStrut(15));

        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setOpaque(false);

        final JLabel chooseFromElsewhere = new JLabel(searchIcon,
                JLabel.LEFT);
        chooseFromElsewhere.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                chooseFromElsewhere.setIcon(searchIcon);
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (jfc.showOpenDialog(LoadConfigurationPanel.this) == JFileChooser.APPROVE_OPTION) {
                    if (checkDirectoryAndAttemptProceed(jfc.getSelectedFile())) {
                        // fire event to screens.
                        firePropertyChange("configSelected", "", jfc.getSelectedFile());
                    }
                }
            }

            public void mouseEntered(MouseEvent event) {
                chooseFromElsewhere.setIcon(searchOverIcon);
            }

            public void mouseExited(MouseEvent event) {
                chooseFromElsewhere.setIcon(searchIcon);
            }
        });

        final JLabel loadSelected = new JLabel(loadIcon,
                JLabel.RIGHT);
        loadSelected.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                loadSelected.setIcon(loadIcon);
                getSelectedConfigurationAndLoad();
            }

            public void mouseEntered(MouseEvent event) {
                loadSelected.setIcon(loadOverIcon);
            }

            public void mouseExited(MouseEvent event) {
                loadSelected.setIcon(loadIcon);
            }
        });

        selectionPanel.add(chooseFromElsewhere, BorderLayout.WEST);
        selectionPanel.add(loadSelected, BorderLayout.EAST);

        container.add(selectionPanel);

        JLabel progress = new JLabel();

        container.add(Box.createVerticalStrut(10));
        container.add(progress);

        JPanel problemCont = new JPanel(new GridLayout(1, 1));
        problemCont.setOpaque(false);

        JScrollPane problemScroll = new JScrollPane(problemReporter, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        problemScroll.setPreferredSize(new Dimension(300, 75));
        problemScroll.setBorder(null);
        problemScroll.setOpaque(false);
        problemScroll.getViewport().setOpaque(false);

        problemCont.add(problemScroll);

        container.add(problemCont);

        add(container, BorderLayout.CENTER);
    }

    private void getSelectedConfigurationAndLoad() {
        if (configurations.getSelectedIndex() != -1) {
            // select file from list
            for (final File candidate : configurationFiles) {
                if (candidate.getName()
                        .equals(configurations.getSelectedValue()
                                .toString())) {
                    File candidateDir = new File(LoadConfigurationPanel.CONFIG_DIR + File.separator +
                            candidate.getName() + File.separator);

                    if (checkDirectoryAndAttemptProceed(candidateDir)) {
                        // fire event to listeners so that they may deal with the configuration directory.
                        firePropertyChange("configSelected", "", candidateDir);
                    }
                }
            }
        }
    }

    /**
     * Method will check directory to ensure it isn't empty and that it contains xml files!
     *
     * @param candidateConfDir - directory said to contain configurations.
     * @return false if directory is invalid, true if directory is fine.
     */
    private boolean checkDirectoryAndAttemptProceed(File candidateConfDir) {

        problemLog = "";
        if (candidateConfDir.isDirectory()) {
            File[] dirContents = candidateConfDir.listFiles();
            if (dirContents.length > 0) {
                boolean allXML = true;

                for (File f : dirContents) {
                    if (!f.getName().startsWith(".")) {
                        if (!FileUtils.getExtension(f).equalsIgnoreCase("xml")) {
                            allXML = false;
                            break;
                        }
                    }
                }

                if (!allXML) {
                    problemLog = "<p>there are files in this directory that are not <strong>xml</strong> files.</p>";
                    problemReporter.setText(getProblemLog());
                } else {
                    problemReporter.setText("");
                    return true;
                }

            } else {
                problemLog = "<p>the suppied directory is <strong>emtpy</strong></p>";
                problemReporter.setText(getProblemLog());
            }
        } else {
            problemLog = "<p>the suppied location is not a directory.</p>";
            problemReporter.setText(getProblemLog());
        }

        return false;
    }

    public String getProblemLog() {
        return "<html>" + "<head>" +
                "<style type=\"text/css\">" + "<!--" + ".bodyFont {" +
                "   font-family: Verdana;" + "   font-size: 10px;" +
                "   color: #BF1E2D;" + "}" + "-->" + "</style>" + "</head>" +
                "<body class=\"bodyFont\">" +
                "<b>Problem with configuration directory</b>" + problemLog +
                "</body></html>";
    }

    private File[] updatePreviousConfigurations() {
        defaultListModel.clear();

        File f = new File(LoadConfigurationPanel.CONFIG_DIR);

        if (!f.exists() || !f.isDirectory()) {
            f.mkdir();
        }

        configurationFiles = f.listFiles();

        for (File prevSubmission : configurationFiles) {
            if (prevSubmission.isDirectory()) {
                if (!prevSubmission.getName().startsWith(".")) {
                    defaultListModel.addElement(prevSubmission.getName());
                }
            }
        }

        return configurationFiles;
    }

    class CustomListCellRenderer extends JComponent
            implements ListCellRenderer {
        DefaultListCellRenderer listCellRenderer;

        /**
         * CustomListCellRenderer Constructor
         */
        public CustomListCellRenderer() {
            setLayout(new BorderLayout());
            listCellRenderer = new DefaultListCellRenderer();

            JLabel image = new JLabel(configListItem);
            image.setOpaque(false);

            add(image, BorderLayout.WEST);
            add(listCellRenderer, BorderLayout.CENTER);

            setBorder(null);
        }

        public Component getListCellRendererComponent(JList jList,
                                                      Object val, int index, boolean selected, boolean b1) {
            listCellRenderer.getListCellRendererComponent(jList, val, index, selected, b1);
            listCellRenderer.setBorder(null);
            Component[] components = getComponents();

            for (Component c : components) {
                if (selected) {
                    UIHelper.renderComponent((JComponent) c, UIHelper.VER_14_BOLD, UIHelper.LIGHT_GREY_COLOR, false);
                } else {
                    UIHelper.renderComponent((JComponent) c, UIHelper.VER_12_PLAIN, UIHelper.LIGHT_GREY_COLOR, false);
                }
            }

            return this;
        }
    }
}
