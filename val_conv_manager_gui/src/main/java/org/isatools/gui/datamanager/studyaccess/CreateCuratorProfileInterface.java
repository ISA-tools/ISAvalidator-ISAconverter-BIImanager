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
import org.isatools.effects.SmallLoader;
import org.isatools.effects.UIHelper;
import org.isatools.gui.Globals;
import org.isatools.gui.TitlePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CreateCuratorProfileInterface
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Dec 1, 2009
 */


public class CreateCuratorProfileInterface extends JFrame {
    private LoginUI login;

    private JPanel glass;

    private SmallLoader loader;

    private JLabel status;
    private JTextField usernameVal;
    private JTextField forename;
    private JTextField surnameVal;
    private JPasswordField passwordVal;
    private JPasswordField passwordConfirmationVal;
    private JTextField affiliation;
    private JTextField emailVal;
    private GridBagConstraints c;

    private JPanel swappableContainer;
    private JLabel createCurator;

    private static int WIN_WIDTH = 250;
    private static int WIN_HEIGHT = 250;

    public static final ImageIcon USER_ADDED = new ImageIcon(CreateCuratorProfileInterface.class.getResource("/images/DataManager/user_added.png"));


    public CreateCuratorProfileInterface(LoginUI login) throws HeadlessException {
        this.login = login;

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.gridheight = 2;
        c.gridx = 1;
        c.gridy = 3;
    }

    public void createGUI() {
        TitlePanel titlePanel = new CreateCuratorProfileInterfaceTitlePanel();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
        setLocation(calculateLocation());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setAlwaysOnTop(true);

        setBackground(UIHelper.BG_COLOR);

        add(titlePanel, BorderLayout.NORTH);
        titlePanel.installListeners();

        swappableContainer = new JPanel(new BorderLayout());
        swappableContainer.setOpaque(false);
        swappableContainer.add(createDisplay());

        add(swappableContainer, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    private Point calculateLocation() {
        Point containerLocation = login.getApplicationContainer().getLocation();
        Dimension containerSize = login.getApplicationContainer().getSize();

        int x = ((containerLocation.x + containerSize.width) / 2) - (WIN_WIDTH / 2);
        int y = ((containerLocation.y + containerSize.height) / 2) - (WIN_HEIGHT / 2);

        return new Point(x, y);
    }

    private Container createDisplay() {
        Box fields = Box.createVerticalBox();
        fields.setPreferredSize(new Dimension(WIN_WIDTH, 200));
        fields.add(Box.createVerticalStrut(5));
        fields.setOpaque(false);

        // username
        JPanel userNameCont = createPanel();
        JLabel usernameLabel = createLabel(" username *");
        userNameCont.add(usernameLabel);

        usernameVal = createTextField();
        userNameCont.add(usernameVal);

        //password
        JPanel passwordCont = createPanel();
        JLabel passwordLabel = createLabel(" password *");
        passwordCont.add(passwordLabel);

        passwordVal = createPasswordField();
        passwordCont.add(passwordVal);

        // confirm password
        JPanel confirmPasswordCont = createPanel();
        JLabel confirmPasswordLabel = createLabel(" confirm *");
        confirmPasswordCont.add(confirmPasswordLabel);

        passwordConfirmationVal = createPasswordField();
        confirmPasswordCont.add(passwordConfirmationVal);

        //forename
        JPanel firstNameCont = createPanel();
        JLabel firstNameLabel = createLabel(" forename *");
        firstNameCont.add(firstNameLabel);

        forename = createTextField();
        firstNameCont.add(forename);

        //surname
        JPanel surnameCont = createPanel();
        JLabel surnameLabel = createLabel(" surname *");
        surnameCont.add(surnameLabel);

        surnameVal = createTextField();
        surnameCont.add(surnameVal);

        // institution
        JPanel institutionCont = createPanel();
        JLabel institutionLabel = createLabel(" affiliation *");
        institutionCont.add(institutionLabel);

        affiliation = createTextField();
        institutionCont.add(affiliation);

        // email
        JPanel emailCont = createPanel();
        JLabel emailLabel = createLabel(" email *");
        emailCont.add(emailLabel);

        emailVal = createTextField();
        emailCont.add(emailVal);

        fields.add(userNameCont);
        fields.add(Box.createVerticalStrut(5));
        fields.add(passwordCont);
        fields.add(Box.createVerticalStrut(5));
        fields.add(confirmPasswordCont);
        fields.add(Box.createVerticalStrut(5));
        fields.add(firstNameCont);
        fields.add(Box.createVerticalStrut(5));
        fields.add(surnameCont);
        fields.add(Box.createVerticalStrut(5));
        fields.add(institutionCont);
        fields.add(Box.createVerticalStrut(5));
        fields.add(emailCont);
        fields.add(Box.createVerticalStrut(5));


        JLabel info = new JLabel(
                "<html><b>* </b> indicates required field </html>", SwingConstants.CENTER);
        UIHelper.renderComponent(info, UIHelper.VER_11_ITALIC, UIHelper.LIGHT_GREY_COLOR, false);
        fields.add(UIHelper.wrapComponentInPanel(info));

        status = UIHelper.createLabel("", UIHelper.VER_11_BOLD, UIHelper.LIGHT_GREY_COLOR, SwingConstants.CENTER);

        JPanel statusPanel = new JPanel(new GridLayout(1, 1));
        statusPanel.setPreferredSize(new Dimension(WIN_WIDTH, 30));

        statusPanel.add(status);

        fields.add(statusPanel);

        return fields;

    }

    private Container createButtonPanel() {
        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setOpaque(false);

        final JLabel close = new JLabel(Globals.CLOSE,
                JLabel.RIGHT);
        close.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                close.setIcon(Globals.CLOSE);
                CreateCuratorProfileInterface.this.setVisible(false);
                CreateCuratorProfileInterface.this.dispose();
            }

            public void mouseEntered(MouseEvent event) {
                close.setIcon(Globals.CLOSE_OVER);
            }

            public void mouseExited(MouseEvent event) {
                close.setIcon(Globals.CLOSE);
            }
        });

        buttonContainer.add(close, BorderLayout.WEST);

        createCurator = new JLabel(Globals.CREATE_CURATOR,
                JLabel.RIGHT);
        createCurator.addMouseListener(new MouseAdapter() {


            public void mousePressed(MouseEvent event) {
                createCurator.setIcon(Globals.CREATE_CURATOR);
                validateTerms();
            }

            public void mouseEntered(MouseEvent event) {
                createCurator.setIcon(Globals.CREATE_CURATOR_OVER);
            }

            public void mouseExited(MouseEvent event) {
                createCurator.setIcon(Globals.CREATE_CURATOR);
            }
        });

        buttonContainer.add(createCurator, BorderLayout.EAST);

        return buttonContainer;
    }

    private void validateTerms() {
        // check password
        String password = new String(passwordVal.getPassword());
        String confirmationPassword = new String(passwordConfirmationVal.getPassword());

        if (!password.equals(confirmationPassword)) {
            status.setText(
                    "<html><b>password and confirmation password do not match</b></html>");
        }

        // check rest of fields to make sure they are filled in!
        if (!usernameVal.getText().equals("")) {
            if (!forename.getText().equals("")) {
                if (!surnameVal.getText().equals("")) {
                    if (!affiliation.getText().equals("")) {
                        if (!emailVal.getText().equals("")) {
                            Pattern p = Pattern.compile("[.]*@[.]*");
                            Matcher m = p.matcher(emailVal.getText());
                            if (m.find()) {
                                status.setText("");
                                try {
                                    doCreateUser(password);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    status.setText(e.getMessage());
                                }
                            } else {
                                status.setText(
                                        "<html><b>email is not valid!</b> please enter a valid email address</html>");
                            }
                        } else {
                            status.setText(
                                    "<html><b>email is required!</b> please enter an email</html>");
                        }
                    } else {
                        status.setText(
                                "<html><b>institution is required!</b> please enter a surname</html>");
                    }
                } else {
                    status.setText(
                            "<html><b>surname is required!</b> please enter a surname</html>");
                }
            } else {
                status.setText(
                        "<html><b>forename is required!</b> please enter a forename</html>");
            }
        } else {
            status.setText(
                    "<html><b>username is required!</b> please enter a username</html>");
        }
    }

    private void doCreateUser(final String password) {
        Thread createUserThread = new Thread(new Runnable() {
            public void run() {
                try {
                    loader = new SmallLoader(10, 5.0f, 150, 70, "creating user");

                    setGlassPanelContents(loader);
                    CreateCuratorProfileInterface.this.validate();
                    loader.start();

                    login.createUser(usernameVal.getText(), emailVal.getText(), affiliation.getText(), forename.getText(), surnameVal.getText(), password);

                    JLabel newUserAdded = new JLabel(USER_ADDED);
                    swappableContainer.removeAll();
                    swappableContainer.add(newUserAdded);
                    swappableContainer.repaint();

                    createCurator.setVisible(false);

                    status.setText("user " + usernameVal.getText() + " added...");
                    clearFields();

                } catch (Exception e) {
                    if (e.getCause().toString().contains("ConstraintViolationException")) {
                        status.setText("username " + usernameVal.getText() + " already exists...");
                    } else {
                        status.setText(e.getMessage());
                    }

                } finally {
                    hideGlassPane();
                    loader.stop();
                }

            }
        });
        createUserThread.start();
    }

    private void clearFields() {
        usernameVal.setText("");
        emailVal.setText("");
        forename.setText("");
        surnameVal.setText("");
        affiliation.setText("");
        passwordVal.setText("");
        passwordConfirmationVal.setText("");
    }

    private JLabel createLabel(String labelName) {
        return UIHelper.createLabel(labelName, UIHelper.VER_11_BOLD, UIHelper.LIGHT_GREY_COLOR);
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.setOpaque(false);
        return panel;
    }

    private JTextField createTextField() {
        JTextField textfield = new JTextField(10);
        UIHelper.renderComponent(textfield, UIHelper.VER_11_BOLD, UIHelper.LIGHT_GREY_COLOR, false);
        textfield.setBorder(new CustomRoundedBorder(UIHelper.LIGHT_GREEN_COLOR, false));
        textfield.setCaretColor(UIHelper.LIGHT_GREY_COLOR);
        textfield.setOpaque(false);

        textfield.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "ADDUSER");
        textfield.getActionMap().put("ADDUSER", addUserAction);

        return textfield;
    }

    private JPasswordField createPasswordField() {
        JPasswordField passwordField = new JPasswordField(10);
        UIHelper.renderComponent(passwordField, UIHelper.VER_11_BOLD, UIHelper.LIGHT_GREY_COLOR, false);
        passwordField.setBorder(new CustomRoundedBorder(UIHelper.LIGHT_GREEN_COLOR, false));
        passwordField.setCaretColor(UIHelper.LIGHT_GREY_COLOR);
        passwordField.setOpaque(false);

        passwordField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "ADDUSER");
        passwordField.getActionMap().put("ADDUSER", addUserAction);

        return passwordField;
    }

    class CreateCuratorProfileInterfaceTitlePanel extends TitlePanel {

        CreateCuratorProfileInterfaceTitlePanel() {
            super(false, false);
        }

        private Image grip = new ImageIcon(getClass().getResource("/images/DataManager/curator_title_grip_active.png")).getImage();
        private Image inactiveGrip = new ImageIcon(getClass().getResource("/images/DataManager/curator_title_grip_inactive.png")).getImage();
        private Image title = new ImageIcon(getClass().getResource("/images/DataManager/create_curator_title_active.png")).getImage();

        protected void drawGrip(Graphics2D g2d, boolean active) {
            g2d.drawImage(active ? grip : inactiveGrip, 0, 0, null);
        }

        protected void drawTitle(Graphics2D g2d) {
            g2d.drawImage(title, getWidth() / 2 - title.getWidth(null) / 2, 0, null);
        }
    }

    public void setGlassPanelContents(Container panel) {
        if (glass != null) {
            glass.removeAll();
        }

        glass = (JPanel) getGlassPane();
        glass.setLayout(new GridBagLayout());
        glass.setOpaque(true);
        glass.setBackground(UIHelper.BG_COLOR);
        glass.addMouseListener(new MouseAdapter() {

        });
        glass.add(panel, c);
        glass.setVisible(true);
        glass.revalidate();
        glass.repaint();
    }

    public void hideGlassPane() {
        glass.setVisible(false);
    }

    Action addUserAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            validateTerms();
        }
    };
}
