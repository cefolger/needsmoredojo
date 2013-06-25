package com.chrisfolger.needsmoredojo.intellij.configurable;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.util.LinkedHashMap;

public class ExceptionsTableBuilder
{
    private class ExceptionsModel extends AbstractTableModel
    {
        private String[] columnNames = new String[] { "Dojo Module", "Parameter Name" };
        private LinkedHashMap<String, String> exceptionsMap;

        public ExceptionsModel()
        {
            exceptionsMap = DojoSettings.getInstance().getExceptionsMap();
        }

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
            if(col == 0)
            {
                return exceptionsMap.keySet().toArray(new String[0])[row];
            }
            else
            {
                return exceptionsMap.values().toArray(new String[0])[row];
            }
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
