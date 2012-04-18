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

import org.isatools.effects.FooterPanel;
import org.isatools.effects.UIHelper;
import org.isatools.gui.converter.ConverterTitlePanel;
import org.isatools.gui.converter.ConverterUI;
import org.isatools.gui.datamanager.DataManagerTitlePanel;
import org.isatools.gui.datamanager.DataManagerToolUI;
import org.isatools.gui.validator.ValidatorTitlePanel;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import java.awt.*;


public class AppContainer extends JFrame {
    private JLayeredPane currentPage = null;
    private JPanel glass;
    private GridBagConstraints c;
    private String[] options;

    private ApplicationType useAs;

    static {
        UIManager.put("ToolTip.foreground", UIHelper.LIGHT_GREEN_COLOR);
        UIManager.put("ToolTip.background", UIHelper.BG_COLOR);

        ResourceInjector.addModule("org.jdesktop.fuse.swing.SwingModule");

        ResourceInjector.get("checkable-package.style").load(
                AppContainer.class.getResource("/dependency-injections/checkable-package.properties"));
        ResourceInjector.get("gui-package.style").load(
                AppContainer.class.getResource("/dependency-injections/gui-package.properties"));
        ResourceInjector.get("optionselector-package.style").load(
                AppContainer.class.getResource("/dependency-injections/optionselector-package.properties"));
        ResourceInjector.get("datamanager-package.style").load(
                AppContainer.class.getResource("/dependency-injections/datamanager-package.properties"));
    }

    public AppContainer(ApplicationType useAs) {
        this(useAs, new String[]{});
    }

    /**
     * Way in to running the application.
     *
     * @param useAs   - tell the code what 'skin' to use, e.g. ApplicationType.[VALIDATOR, CONVERTER, MANAGER]
     * @param options - e.g. -noAnimation to stop the animated panel.
     *                Useful for machines who's performance is not so great or when running the application over X11.
     */
    public AppContainer(ApplicationType useAs, String[] options) {
        this.useAs = useAs;
        this.options = options;

    }

    public void createGUI() {
        setBackground(UIHelper.BG_COLOR);

        TitlePanel titlePanel;
        if (useAs == ApplicationType.VALIDATOR) {
            titlePanel = new ValidatorTitlePanel();
        } else if (useAs == ApplicationType.MANAGER) {
            titlePanel = new DataManagerTitlePanel();
        } else {
            titlePanel = new ConverterTitlePanel();
        }

        setLayout(new BorderLayout());
        Dimension size = (UIHelper.getScreenSize().width < 800 || UIHelper.getScreenSize().height < 600)
                ? new Dimension(UIHelper.getScreenSize()) : new Dimension(800, 600);
        setPreferredSize(size);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(true);

        add(titlePanel, BorderLayout.NORTH);
        titlePanel.installListeners();

        FooterPanel fp = new FooterPanel(this);
        add(fp, BorderLayout.SOUTH);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.gridheight = 2;
        c.gridx = 1;
        c.gridy = 3;

        CommonUI initialScreen;
        if (useAs == ApplicationType.MANAGER) {
            initialScreen = new DataManagerToolUI(this, useAs, options);
        } else {
            initialScreen = new ConverterUI(this, useAs, options);
        }
        setCurrentPage(initialScreen);
        pack();
        setVisible(true);
    }

    public void setCurrentPage(JLayeredPane newPage) {
        if (currentPage == null) {
            currentPage = newPage;
        } else {
            getContentPane().remove(currentPage);
            currentPage = newPage;
        }
        getContentPane().add(currentPage, BorderLayout.CENTER);
        repaint();
        validate();
    }

    public void setGlassPanelContents(Container panel) {
        if (glass != null) {
            glass.removeAll();
        }

        glass = (JPanel) getGlassPane();
        glass.setLayout(new GridBagLayout());
        glass.add(panel, c);
        glass.setVisible(true);
        glass.revalidate();
        glass.repaint();
    }

    public void hideGlassPane() {
        glass.setVisible(false);
    }


}
