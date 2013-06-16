package com.chrisfolger.dojotools.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Chris
 * Date: 12/20/12
 * Time: 8:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class DojoSettingsForm implements Configurable
{
    private JTextField textField1;
    private JPanel panel;

    @NotNull
    @Nls
    @Override
    public String getDisplayName() {
        return "Dojo Settings";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "Dojo Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {


        return panel;
    }

    @Override
    public boolean isModified() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void apply() throws ConfigurationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reset() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disposeUIResources() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
