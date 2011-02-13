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

package org.isatools.gui.errorprocessing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Enumeration;

/**
 * ErrorReportUI creates a table representation of errors found in an ISATab submission.
 *
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @date Mar 19, 2010
 */


public class ErrorReportUI extends JPanel implements ListSelectionListener {

    private ErrorTableHeader errorTableHeader = new ErrorTableHeader();
    private ErrorInformationUI errorInformation;
    private TableModel errorTableModel;
    private JTable errorTable;
    private ErrorReport report;

    public ErrorReportUI(ErrorReport report) {
        this.report = report;
    }

    public void createGUI() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(400, 350));

        errorInformation = new ErrorInformationUI();

        add(createErrorTable(), BorderLayout.NORTH);
        add(errorInformation, BorderLayout.CENTER);
    }

    private Container createErrorTable() {
        errorTableModel = new DefaultTableModel(createTableDataFromReport(), createTableColumnNames());
        errorTable = new JTable(errorTableModel) {
            @Override
            public boolean isCellEditable(int i, int i1) {
                return false;
            }
        };

        try {
            errorTable.setDefaultRenderer(Class.forName("java.lang.Object"), new ErrorTableRenderer());
        } catch (ClassNotFoundException e) {
            System.err.println("Problem setting renderer in ErrorReportUI for table");
        }

        errorTable.setOpaque(false);
        errorTable.setAutoCreateRowSorter(true);
        errorTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        errorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        errorTable.setShowGrid(false);
        errorTable.setShowHorizontalLines(false);
        errorTable.setShowVerticalLines(false);
        errorTable.getSelectionModel().addListSelectionListener(this);
        errorTable.getTableHeader().setReorderingAllowed(false);
        errorTable.getTableHeader().addMouseListener(new ErrorTableHeaderListener(errorTable.getTableHeader(), errorTableHeader));
        errorTable.getTableHeader().setResizingAllowed(false);


        Enumeration<TableColumn> tableColumns = errorTable.getColumnModel().getColumns();
        while (tableColumns.hasMoreElements()) {
            TableColumn tc = tableColumns.nextElement();
            tc.setHeaderRenderer(errorTableHeader);
        }

        JScrollPane errorTableScroller = new JScrollPane(errorTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        errorTableScroller.setOpaque(false);
        errorTableScroller.getViewport().setOpaque(false);
        errorTableScroller.setBorder(new EmptyBorder(1, 1, 1, 1));
        errorTableScroller.setPreferredSize(new Dimension(400, 200));

        // select first row in table if there is one!
        if (errorTable.getRowCount() > 0) {

            errorTable.setRowSelectionInterval(0, 0);
        }

        return errorTableScroller;
    }

    private Object[][] createTableDataFromReport() {
        Object[][] rowData = new Object[report.getReport().size()][3];

        int count = 0;
        for (ValidationError ve : report.getReport()) {
            rowData[count][0] = ve.getErrorType();
            rowData[count][1] = ve.getMessage();
            rowData[count][2] = ve.getLocation();
            count++;
        }

        return rowData;
    }

    private Object[] createTableColumnNames() {
        return new Object[]{"type", "message", "in file"};
    }


    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        int rowSelected = errorTable.getSelectedRow();

        if (rowSelected > -1) {
            errorInformation.updateView(errorTable.getValueAt(rowSelected, 0).toString(),
                    errorTable.getValueAt(rowSelected, 1).toString(), errorTable.getValueAt(rowSelected, 2).toString());
        }
    }
}
