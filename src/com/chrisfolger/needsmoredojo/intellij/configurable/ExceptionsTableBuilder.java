package com.chrisfolger.needsmoredojo.intellij.configurable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public class ExceptionsTableBuilder
{
    private class ExceptionsModel extends AbstractTableModel
    {
        private String[] columnNames = new String[] { "Dojo Module", "Parameter Name" };

        public String getColumnName(int col) {
            return columnNames[col].toString();
        }

        public int getRowCount()
        {
            return 1;
        }

        public int getColumnCount()
        {
            return columnNames.length;
        }

        public Object getValueAt(int row, int col)
        {
            return "foo"; //null; //rowData[row][col];
        }

        public boolean isCellEditable(int row, int col)
        {
            return true;
        }

        public void setValueAt(Object value, int row, int col)
        {
           /* rowData[row][col] = value;
            fireTableCellUpdated(row, col); */
        }
    }

    public ExceptionsTableBuilder(JTable table)
    {
        table.setModel(new ExceptionsModel());
        table.setTableHeader(new JTableHeader(table.getColumnModel()));
        table.updateUI();
    }
}
