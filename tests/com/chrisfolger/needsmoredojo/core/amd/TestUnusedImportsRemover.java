package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.testutil.BasicPsiElements;
import com.chrisfolger.needsmoredojo.testutil.MockJSElement;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.psi.PsiElement;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestUnusedImportsRemover
{
    private UnusedImportsRemover remover;
    private List<PsiElement> parameters;
    private List<PsiElement> defines;

    @Before
    public void setup()
    {
        remover = new UnusedImportsRemover();
        parameters = new ArrayList<PsiElement>();
        defines = new ArrayList<PsiElement>();
    }

    @Test
    public void happyPath()
    {
        addModule("a/b/C", "C", true);
        addModule("a/b/D", "D", false);

        UnusedImportsRemover.RemovalResult result = remover.removeUnusedParameters(parameters, defines);

        // make sure all modules + commas were deleted
        assertEquals(4, result.getElementsToDelete().size());
    }

    private void addModule(String module, String name, boolean commas)
    {
        MockJSElement define = new MockJSElement(module);
        MockJSElement parameter = new MockJSElement(name);

        if(commas)
        {
            define.comesBefore(BasicPsiElements.comma());
            parameter.comesBefore(BasicPsiElements.comma());
        }

        defines.add(define);
        parameters.add(parameter);
    }
}
