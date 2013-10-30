package com.chrisfolger.needsmoredojo.intellij.modules;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.DefaultModuleConfigurationEditorFactory;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;

import java.util.ArrayList;
import java.util.List;

public class DojoToolkitModuleEditorProvider implements ModuleConfigurationEditorProvider
{
    @Override
    public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState moduleConfigurationState) {
        final Module module = moduleConfigurationState.getRootModel().getModule();
        if (ModuleType.get(module) != DojoToolkitModuleType.getInstance()) return ModuleConfigurationEditor.EMPTY;

        final DefaultModuleConfigurationEditorFactory editorFactory = DefaultModuleConfigurationEditorFactory.getInstance();
        List<ModuleConfigurationEditor> editors = new ArrayList<ModuleConfigurationEditor>();
        //editors.add(editorFactory.createModuleContentRootsEditor(moduleConfigurationState));

        for(Module theModule : ModuleManager.getInstance(moduleConfigurationState.getProject()).getModules())
        {
            ModuleType theType = ModuleType.get(theModule);

            int i=0;
        }

        return editors.toArray(new ModuleConfigurationEditor[editors.size()]);
    }
}
