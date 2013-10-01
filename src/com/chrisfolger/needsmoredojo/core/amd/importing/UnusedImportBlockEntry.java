package com.chrisfolger.needsmoredojo.core.amd.importing;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.psi.PsiElement;

import java.util.Collection;
import java.util.List;

public class UnusedImportBlockEntry
{
    private JSCallExpression block;
    private List<PsiElement> defines;
    private List<PsiElement> parameters;
    private boolean isDefine;

    public UnusedImportBlockEntry(boolean isDefine, JSCallExpression block, List<PsiElement> defines, List<PsiElement> parameters) {
        this.block = block;
        this.defines = defines;
        this.parameters = parameters;
        this.isDefine = isDefine;
    }

    public JSCallExpression getBlock() {
        return block;
    }

    public List<PsiElement> getDefines() {
        return defines;
    }

    public List<PsiElement> getParameters() {
        return parameters;
    }

    public static UnusedImportBlockEntry getDefine(Collection<UnusedImportBlockEntry> entries)
    {
        for(UnusedImportBlockEntry entry : entries)
        {
            if(entry.isDefine)
            {
                return entry;
            }
        }

        return null;
    }
}
