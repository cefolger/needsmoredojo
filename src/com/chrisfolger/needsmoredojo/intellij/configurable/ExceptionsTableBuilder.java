package com.chrisfolger.needsmoredojo.intellij.configurable;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

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
        private DojoSettings settingsService;

        public ExceptionsModel(Project project)
        {
            settingsService = ServiceManager.getService(project, DojoSettings.class);
            exceptionsMap = settingsService.getExceptionsMap();
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
            return col != 0;
        }

        public void setValueAt(Object value, int row, int col)
        {
            if(col == 1)
            {
                exceptionsMap.put(exceptionsMap.keySet().toArray(new String[0])[row], (String) value);
                fireTableCellUpdated(row, col);
            }
        }
    }

    public ExceptionsTableBuilder(JTable table, Project project)
    {
        table.setModel(new ExceptionsModel(project));
        table.setTableHeader(new JTableHeader(table.getColumnModel()));
        table.updateUI();
    }
}
