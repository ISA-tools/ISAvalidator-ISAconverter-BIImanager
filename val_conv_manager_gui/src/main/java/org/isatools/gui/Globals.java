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

import javax.swing.*;

/**
 * Globals provide a way to access images/variable used across classes and where we only want one instance of say an image
 * instead of multiple instances of the same image.
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Oct 27, 2009
 */


public class Globals {

    public static final ImageIcon BACK_MAIN = new ImageIcon(Globals.class.getResource("/images/DataManager/toMenu.png"));
    public static final ImageIcon BACK_MAIN_OVER = new ImageIcon(Globals.class.getResource("/images/DataManager/toMenuOver.png"));

    public static final ImageIcon CREATE_CURATOR = new ImageIcon(Globals.class.getResource("/images/DataManager/create_curator.png"));
    public static final ImageIcon CREATE_CURATOR_OVER = new ImageIcon(Globals.class.getResource("/images/DataManager/create_curator_over.png"));

    public static final ImageIcon CLOSE = new ImageIcon(Globals.class.getResource("/images/DataManager/close.png"));
    public static final ImageIcon CLOSE_OVER = new ImageIcon(Globals.class.getResource("/images/DataManager/close_over.png"));

    public static final ImageIcon SET_PERMISSION = new ImageIcon(Globals.class.getResource("/images/DataManager/set_permission_button.png"));
    public static final ImageIcon SET_PERMISSION_OVER = new ImageIcon(Globals.class.getResource("/images/DataManager/set_permission_button_over.png"));

    public static final ImageIcon LOAD_ISATAB = new ImageIcon(Globals.class.getResource("/images/DataManager/load_isatab.png"));
    public static final ImageIcon LOAD_ISATAB_OVER = new ImageIcon(Globals.class.getResource("/images/DataManager/load_isatab_over.png"));
    public static final ImageIcon UNLOAD_STUDY = new ImageIcon(Globals.class.getResource("/images/DataManager/unload_study.png"));
    public static final ImageIcon UNLOAD_STUDY_OVER = new ImageIcon(Globals.class.getResource("/images/DataManager/unload_study_over.png"));
    public static final ImageIcon SECURITY = new ImageIcon(Globals.class.getResource("/images/DataManager/security_menu.png"));
    public static final ImageIcon SECURITY_OVER = new ImageIcon(Globals.class.getResource("/images/DataManager/security_menu_over.png"));
    public static final ImageIcon EXPORT_ISA = new ImageIcon(Globals.class.getResource("/images/DataManager/export_isatab.png"));
    public static final ImageIcon EXPORT_ISA_OVER = new ImageIcon(Globals.class.getResource("/images/DataManager/export_isatab_over.png"));
    public static final ImageIcon REINDEX = new ImageIcon(Globals.class.getResource("/images/DataManager/reindex_menu_item.png"));
    public static final ImageIcon REINDEX_OVER = new ImageIcon(Globals.class.getResource("/images/DataManager/reindex_menu_item_over.png"));

    public static final ImageIcon INVALID_ISATAB = new ImageIcon(Globals.class.getResource("/images/common/invalid_isatab.png"));
    public static final ImageIcon VALID_ISATAB = new ImageIcon(Globals.class.getResource("/images/validator/valid_isatab.png"));
    public static final ImageIcon VALIDATE_ANOTHER = new ImageIcon(Globals.class.getResource("/images/validator/validate_another_button.png"));
    public static final ImageIcon VALIDATE_ANOTHER_OVER = new ImageIcon(Globals.class.getResource("/images/validator/validate_another_button_over.png"));

    // images for loader
    public static final ImageIcon LOAD_FAILED = new ImageIcon(Globals.class.getResource("/images/DataManager/load_failed.png"));
    public static final ImageIcon LOAD_SUCCESS = new ImageIcon(Globals.class.getResource("/images/DataManager/load_success.png"));
    public static final ImageIcon LOAD_SUCCESS_UM_FAILED = new ImageIcon(Globals.class.getResource("/images/DataManager/load_success_access_failed.png"));
    public static final ImageIcon LOAD_ANOTHER = new ImageIcon(Globals.class.getResource("/images/DataManager/load_another.png"));
    public static final ImageIcon LOAD_ANOTHER_OVER = new ImageIcon(Globals.class.getResource("/images/DataManager/load_another_over.png"));
    public static final ImageIcon PRIVACY_MOD_SUCCESS = new ImageIcon(Globals.class.getResource("/images/DataManager/privacy-settings-changed.png"));
    public static final ImageIcon PRIVACY_MOD_FAILED = new ImageIcon(Globals.class.getResource("/images/DataManager/privacy-settings-not-changed.png"));

    // images for unloader
    public static final ImageIcon UNLOAD_ANOTHER = new ImageIcon(Globals.class.getResource("/images/DataManager/unload_another.png"));
    public static final ImageIcon UNLOAD_ANOTHER_OVER = new ImageIcon(Globals.class.getResource("/images/DataManager/unload_another_over.png"));

    public static final ImageIcon CONVERT_ANOTHER = new ImageIcon(Globals.class.getResource("/images/converter/convert_another_button.png"));
    public static final ImageIcon CONVERT_ANOTHER_OVER = new ImageIcon(Globals.class.getResource("/images/converter/convert_another_over.png"));

    public static final ImageIcon EXIT = new ImageIcon(Globals.class.getResource("/images/common/exit_other.png"));
    public static final ImageIcon EXIT_OVER = new ImageIcon(Globals.class.getResource("/images/common/exit_other_over.png"));

    // List icons
    public static final ImageIcon LIST_ICON_STUDY = new ImageIcon(Globals.class.getResource("/images/DataManager/study-list-icon.png"));
    public static final ImageIcon LIST_ICON_USER = new ImageIcon(Globals.class.getResource("/images/DataManager/user-list-icon.png"));
    public static final ImageIcon LIST_ICON_DEFAULT = new ImageIcon(Globals.class.getResource("/images/DataManager/list_image.png"));
}

