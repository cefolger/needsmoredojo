package com.chrisfolger.needsmoredojo.intellij.modules;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

public class DojoToolkitModuleType extends ModuleType<DojoToolkitModuleBuilder>
{
    @NonNls
    private static final String ID = "DOJO_TOOLKIT_MODULE";

    public DojoToolkitModuleType()
    {
        super(ID);
    }

    public static DojoToolkitModuleType getInstance() {
        return (DojoToolkitModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    @Override
    public DojoToolkitModuleBuilder createModuleBuilder() {
        return new DojoToolkitModuleBuilder();
    }

    @Override
    public String getName() {
        return "Dojo Toolkit Module";
    }

    @Override
    public String getDescription() {
        return "A module that contains AMD modules written to take advantage of the Dojo Toolkit and used by Needs More Dojo. " +
                "The module's content root should be the root of your most top level packages so that they can be imported correctly.";
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean b) {
        return AllIcons.Nodes.WebFolder;
    }
}
