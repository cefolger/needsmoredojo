package com.chrisfolger.needsmoredojo.intellij.modules;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;

public class DojoToolkitModuleBuilder extends ModuleBuilder
{
    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException
    {
    }

    @Override
    public ModuleType getModuleType()
    {
        return DojoToolkitModuleType.getInstance();
    }
}
