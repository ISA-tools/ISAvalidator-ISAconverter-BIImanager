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

package org.isatools.gui.datamanager.studyaccess;

import org.isatools.effects.CustomRoundedBorder;
import org.isatools.effects.UIHelper;
import org.isatools.gui.AppContainer;
import org.isatools.gui.Globals;
import org.isatools.gui.ViewingPane;
import org.isatools.isatab.manager.UserManagementControl;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;
import uk.ac.ebi.bioinvindex.model.security.Person;
import uk.ac.ebi.bioinvindex.model.security.UserRole;
import uk.ac.ebi.bioinvindex.utils.StringEncryption;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

/**
 * LoginUI
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Nov 16, 2009
 */


public class LoginUI extends ViewingPane {

    private JLabel status;
    private JPasswordField password;
    private JTextField username;
    private JPanel loginEntryPanel;

    private String loggedInUser;

    @InjectedResource
    private ImageIcon login, loginOver, exit, exitOver, littlePeople, authenticateYourselfHeader;


    public LoginUI(AppContainer container) {
        super(container);
        ResourceInjector.get("datamanager-package.style").inject(this);
        setPreferredSize(new Dimension(275, 250));
        setLayout(new BorderLayout());
        setOpaque(false);
    }

    public void clearFields() {
        status.setText("");
        password.setText("");
        username.setText("");
    }

    public void createGUI() {
        // create username field info
        Box fields = Box.createVerticalBox();
        fields.add(Box.createVerticalStrut(10));
        fields.setOpaque(false);

        JPanel userNameCont = new JPanel(new GridLayout(1, 2));
        JLabel usernameLabel = UIHelper.createLabel("username", UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREY_COLOR);
        userNameCont.add(usernameLabel);

        username = new JTextField(10);
        UIHelper.renderComponent(username, UIHelper.VER_12_PLAIN, UIHelper.LIGHT_GREY_COLOR, false);
        username.setBorder(new CustomRoundedBorder(UIHelper.LIGHT_GREEN_COLOR, false));
        username.setCaretColor(UIHelper.LIGHT_GREY_COLOR);
        userNameCont.add(username);
        userNameCont.setOpaque(false);


        JPanel passwordCont = new JPanel(new GridLayout(1, 2));
        JLabel passwordLabel = UIHelper.createLabel("password", UIHelper.VER_12_BOLD, UIHelper.LIGHT_GREY_COLOR);
        passwordCont.add(passwordLabel);
        password = new JPasswordField("");
        password.setBorder(new CustomRoundedBorder(UIHelper.LIGHT_GREEN_COLOR, false));
        password.setCaretColor(UIHelper.LIGHT_GREY_COLOR);
        password.setOpaque(false);
        UIHelper.renderComponent(password, UIHelper.VER_12_PLAIN, UIHelper.LIGHT_GREY_COLOR, false);

        passwordCont.add(password);
        passwordCont.setOpaque(false);

        fields.add(userNameCont);
        fields.add(Box.createVerticalStrut(10));
        fields.add(passwordCont);

        loginEntryPanel = new JPanel();
        loginEntryPanel.setLayout(new BoxLayout(loginEntryPanel, BoxLayout.PAGE_AXIS));
        loginEntryPanel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        headerPanel.add(new JLabel(littlePeople), BorderLayout.WEST);


        headerPanel.add(new JLabel(authenticateYourselfHeader), BorderLayout.EAST);

        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setOpaque(false);

        final JLabel exitButton = new JLabel(exit,
                JLabel.LEFT);
        exitButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                System.exit(0);
            }

            public void mouseEntered(MouseEvent event) {
                exitButton.setIcon(exitOver);
            }

            public void mouseExited(MouseEvent event) {
                exitButton.setIcon(exit);
            }
        });

        buttonContainer.add(exitButton, BorderLayout.WEST);

        final JLabel createCurator = new JLabel(Globals.CREATE_CURATOR,
                JLabel.CENTER);
        createCurator.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                createCurator.setIcon(Globals.CREATE_CURATOR);

                final CreateCuratorProfileInterface createCuratorInterface = new CreateCuratorProfileInterface(LoginUI.this);

                createCuratorInterface.addPropertyChangeListener("userAdded", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

                        status.setText("<html><b>new curator added</b>, please login using the details you just passed</html>");
                    }
                });
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        createCuratorInterface.createGUI();
                    }
                });
            }

            public void mouseEntered(MouseEvent event) {
                createCurator.setIcon(Globals.CREATE_CURATOR_OVER);
            }

            public void mouseExited(MouseEvent event) {
                createCurator.setIcon(Globals.CREATE_CURATOR);
            }
        });

        buttonContainer.add(createCurator, BorderLayout.CENTER);

        final JLabel loginButton = new JLabel(login,
                JLabel.RIGHT);
        loginButton.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                loginButton.setIcon(login);
                login();
            }

            public void mouseEntered(MouseEvent event) {
                loginButton.setIcon(loginOver);
            }

            public void mouseExited(MouseEvent event) {
                loginButton.setIcon(login);
            }
        });

        Action loginAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        };

        password.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "LOGIN");
        password.getActionMap().put("LOGIN", loginAction);
        username.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "LOGIN");
        username.getActionMap().put("LOGIN", loginAction);

        buttonContainer.add(loginButton, BorderLayout.EAST);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.PAGE_AXIS));
        southPanel.setOpaque(false);

        status = new JLabel();
        status.setForeground(UIHelper.RED_COLOR);

        JPanel statusPanel = UIHelper.wrapComponentInPanel(status);
        statusPanel.setPreferredSize(new Dimension(275, 40));
        statusPanel.setOpaque(false);

        southPanel.add(statusPanel);
        southPanel.add(buttonContainer);

        loginEntryPanel.add(headerPanel);
        loginEntryPanel.add(Box.createVerticalStrut(10));


        loginEntryPanel.add(fields);
        loginEntryPanel.add(Box.createVerticalStrut(15));
        loginEntryPanel.add(southPanel);

        add(loginEntryPanel, BorderLayout.CENTER);
        username.requestFocusInWindow();
    }

    public boolean createUser(String username, String email, String affiliation, String forename, String surname, String password) throws Exception {
        Person user = new Person();
        user.setUserName(username);
        user.setEmail(email);
        user.setAffiliation(affiliation);
        user.setFirstName(forename);
        user.setLastName(surname);
        user.setRole(UserRole.CURATOR);
        user.setJoinDate(new Date());
        user.setPassword(StringEncryption.getInstance().encrypt(password));

        UserManagementControl umControl = new UserManagementControl();
        umControl.createUser(user);

        return true;
    }

    public void login() {

        if (!username.getText().trim().equals("")) {
            Thread loadingThread = new Thread(new Runnable() {
                public void run() {
                    container.setGlassPanelContents(createProgressScreen("attempting log in..."));
                    container.validate();
                    sl.start();
                    try {

                        UserManagementControl umControl = new UserManagementControl();

                        String passwordStr = new String(password.getPassword());

                        if (umControl.validateLogin(username.getText(), passwordStr)) {
                            loggedInUser = username.getText();
                            firePropertyChange("doLogin", "", "login");
                        } else {
                            sl.stop();
                            container.setGlassPanelContents(loginEntryPanel);
                            status.setText(
                                    "<html><b>Invalid user:</b> username doesn't exist or password is incorrect! </html>");
                        }
                    } catch (Exception e) {
                        sl.stop();
                        container.setGlassPanelContents(loginEntryPanel);
                        status.setText(
                                "<html><b>Problem occurred</b>: " + e.getMessage() + "</html>");
                    }

                }
            });
            loadingThread.start();
        } else {
            status.setText(
                    "<html>Please enter a <b>username</b>...</html>");
        }
    }

    public String getLoggedInUserName() {
        return loggedInUser;
    }
}
