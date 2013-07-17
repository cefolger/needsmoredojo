package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.ImportReorderer;

public class MoveImportDownAction extends ReorderAMDImportAction
{
    public MoveImportDownAction()
    {
        super(ImportReorderer.Direction.DOWN);
    }
}
