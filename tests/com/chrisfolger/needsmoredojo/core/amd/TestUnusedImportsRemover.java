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
    public void twoUnusedModulesAreBothRemoved()
    {
        addModule("a/b/C", "C", true);
        addModule("a/b/D", "D", false);

        UnusedImportsRemover.RemovalResult result = remover.removeUnusedParameters(parameters, defines);

        // make sure all modules + commas were deleted
        assertEquals(4, result.getElementsToDelete().size());
    }

    @Test
    /*
        Say you have this structure:

        define([
            'a/b/C',
            'a/b/D',
            'a/b/E',
            'a/b/F'
        ], function(C, D, E, F) {});

        where E, and F are unused. Then the trailing comma from the D import and parameter should be removed
     */
    public void itemsThatComeAtTheEndHaveTrailingCommasRemoved()
    {

    }

    private PsiElement[] addDefine(String module, boolean useComma)
    {
        MockJSElement define = new MockJSElement(module);

        defines.add(define);

        if(useComma)
        {
            MockJSElement comma = BasicPsiElements.comma();
            comma.comesAfter(define);
            return new PsiElement[] { define, comma };
        }

        return new PsiElement[] { define };
    }

    private PsiElement[] addParameter(String parameter, boolean useComma)
    {
        MockJSElement parameterElement = new MockJSElement(parameter);

        parameters.add(parameterElement);

        if(useComma)
        {
            MockJSElement comma = BasicPsiElements.comma();
            comma.comesAfter(parameterElement);
            return new PsiElement[] { parameterElement, comma };
        }

        return new PsiElement[] { parameterElement };
    }

    private void addModule(String module, String name, boolean commas)
    {
        addDefine(module, commas);
        addParameter(name, commas);
    }
}
