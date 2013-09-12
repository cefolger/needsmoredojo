package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;

public class MoveImportDownAction extends ReorderAMDImportAction
{
    public MoveImportDownAction()
    {
        super(AMDPsiUtil.Direction.DOWN);
    }
}
