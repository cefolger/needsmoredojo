package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.ImportReorderer;

public class MoveImportUpAction extends ReorderAMDImportAction
{
    public MoveImportUpAction()
    {
        super(ImportReorderer.Direction.UP);
    }
}
