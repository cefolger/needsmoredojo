package com.chrisfolger.needsmoredojo.intellij.configurable;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.util.LinkedHashMap;

public class ExceptionsTableBuilder
{
    private ExceptionsTableModel model;

    public ExceptionsTableBuilder(JTable table, Project project, LinkedHashMap<String, String> map)
    {
        this.model = new ExceptionsTableModel(project, map);
        table.setModel(model);
        table.setTableHeader(new JTableHeader(table.getColumnModel()));
        table.updateUI();
    }

    public ExceptionsTableModel getTableModel()
    {
        return this.model;
    }
}
