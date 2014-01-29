package com.chrisfolger.needsmoredojo.intellij.configurable;

import com.intellij.openapi.project.Project;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedHashMap;
import java.util.List;

public class ExceptionsListTableModel extends AbstractTableModel
{
    private String[] columnNames = new String[] { "Dojo Module", "Parameter Name" };
    private List<String> exceptionsMap;

    public ExceptionsListTableModel(Project project, List<String> exceptionsMap)
    {
        this.exceptionsMap = exceptionsMap;
    }

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public int getRowCount()
    {
        return exceptionsMap.size();
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public Object getValueAt(int row, int col)
    {
        if(col == 0)
        {
            return exceptionsMap.get(row).split("\\(")[0];
        }
        else
        {
            return exceptionsMap.get(row).split("\\(")[1];
        }
    }

    public boolean isCellEditable(int row, int col)
    {
        return true;
    }

    public void addRow(String[] rowData)
    {
        exceptionsMap.add(rowData[0] + "(" + rowData[1]);
        this.fireTableDataChanged();
    }

    public void removeRow(int row)
    {
        exceptionsMap.remove(row);
        this.fireTableDataChanged();
    }

    public void setValueAt(Object value, int row, int col)
    {
        if(row > exceptionsMap.size())
        {
            exceptionsMap.add("(");
        }

        String[] values = exceptionsMap.get(row).split("\\(");
        if(col == 1)
        {
            values[1] = (String) value;
        }
        else
        {
            values[0] = (String) value;
        }
        exceptionsMap.set(row, values[0] + "(" + values[1]);

        fireTableCellUpdated(row, col);
    }
}
