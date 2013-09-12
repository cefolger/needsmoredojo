package com.chrisfolger.needsmoredojo.intellij.actions;

import com.chrisfolger.needsmoredojo.core.amd.psi.AMDPsiUtil;

public class MoveImportUpAction extends ReorderAMDImportAction
{
    public MoveImportUpAction()
    {
        super(AMDPsiUtil.Direction.UP);
    }
}
