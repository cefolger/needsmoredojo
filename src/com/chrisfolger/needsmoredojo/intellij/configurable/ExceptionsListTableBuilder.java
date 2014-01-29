package com.chrisfolger.needsmoredojo.intellij.configurable;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.util.LinkedHashMap;
import java.util.List;

public class ExceptionsListTableBuilder
{
    private ExceptionsListTableModel model;

    public ExceptionsListTableBuilder(JTable table, Project project, List<String> map)
    {
        this.model = new ExceptionsListTableModel(project, map);
        table.setModel(model);
        table.setTableHeader(new JTableHeader(table.getColumnModel()));
        table.updateUI();
    }

    public ExceptionsListTableModel getTableModel()
    {
        return this.model;
    }
}
