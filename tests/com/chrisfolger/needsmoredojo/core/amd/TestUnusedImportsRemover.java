package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.testutil.BasicPsiElements;
import com.chrisfolger.needsmoredojo.testutil.MockJSArrayLiteralExpression;
import com.chrisfolger.needsmoredojo.testutil.MockJSElement;
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
            'a/b/C', // comment
            'a/b/D'
        ], function(C, D) {});

        where D is unused. Then the trailing comma should also be removed
     */
    public void lineCommentsInDefinesStillRemoveTrailingCommas()
    {
        MockJSElement cDefine = addDefine("a/b/C");
        MockJSElement dDefine = addDefine("a/b/D");

        MockJSArrayLiteralExpression literal = BasicPsiElements.define();
        dDefine.setParent(literal);

        BasicPsiElements.createChain(new MockJSElement[]{
                cDefine, BasicPsiElements.comma(), new MockJSElement("// comment"), BasicPsiElements.lineBreak(),
                dDefine, BasicPsiElements.lineBreak().comesBefore(literal.getBracket())
        });

        defines.add(dDefine);

        MockJSElement cParameter = addParameter("C");
        MockJSElement dParameter = addParameter("D");

        MockJSElement function = BasicPsiElements.defineFunction();
        dParameter.setParent(function);

        BasicPsiElements.createChain(new MockJSElement[]{
                cParameter, BasicPsiElements.comma(), BasicPsiElements.space(),
                dParameter.comesBefore((MockJSElement) function.getLastChild())
        });

        parameters.add(dParameter);

        UnusedImportsRemover.RemovalResult result = remover.removeUnusedParameters(parameters, defines);

        System.out.println(result.getDeletedElementsString());
        assertEquals("Da/b/D\n,,", result.getDeletedElementsString());
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
        MockJSElement cDefine = addDefine("a/b/C");
        MockJSElement dDefine = addDefine("a/b/D");
        MockJSElement eDefine = addDefine("a/b/E");
        MockJSElement fDefine = addDefine("a/b/F");

        MockJSArrayLiteralExpression literal = BasicPsiElements.define();
        eDefine.setParent(literal);

        BasicPsiElements.createChain(new MockJSElement[]{
                cDefine, BasicPsiElements.comma(), BasicPsiElements.lineBreak(),
                dDefine, BasicPsiElements.comma(), BasicPsiElements.lineBreak(),
                eDefine, BasicPsiElements.comma(), BasicPsiElements.lineBreak(),
                fDefine, BasicPsiElements.lineBreak().comesBefore(literal.getBracket())
        });

        defines.add(eDefine);
        defines.add(fDefine);

        MockJSElement cParameter = addParameter("C");
        MockJSElement dParameter = addParameter("D");
        MockJSElement eParameter = addParameter("E");
        MockJSElement fParameter = addParameter("F");

        MockJSElement function = BasicPsiElements.defineFunction();
        eParameter.setParent(function);

        BasicPsiElements.createChain(new MockJSElement[]{
                cParameter, BasicPsiElements.comma(), BasicPsiElements.space(),
                dParameter, BasicPsiElements.comma(), BasicPsiElements.space(),
                eParameter, BasicPsiElements.comma(), BasicPsiElements.space(),
                fParameter.comesBefore((MockJSElement) function.getLastChild())
        });

        parameters.add(eParameter);
        parameters.add(fParameter);

        UnusedImportsRemover.RemovalResult result = remover.removeUnusedParameters(parameters, defines);

        System.out.println(result.getDeletedElementsString());
        assertEquals("E,Fa/b/E,a/b/F\n,,", result.getDeletedElementsString());
    }

    private MockJSElement addDefine(String module)
    {
        return addDefine(module, false)[0];
    }

    private MockJSElement[] addDefine(String module, boolean useComma)
    {
        MockJSElement define = new MockJSElement(module);

        if(useComma)
        {
            MockJSElement comma = BasicPsiElements.comma();
            comma.comesAfter(define);
            return new MockJSElement[] { define, comma };
        }

        return new MockJSElement[] { define };
    }

    private MockJSElement addParameter(String parameter)
    {
        return addParameter(parameter, false)[0];
    }

    private MockJSElement[] addParameter(String parameter, boolean useComma)
    {
        MockJSElement parameterElement = new MockJSElement(parameter);

        if(useComma)
        {
            MockJSElement comma = BasicPsiElements.comma();
            comma.comesAfter(parameterElement);
            return new MockJSElement[] { parameterElement, comma };
        }

        return new MockJSElement[] { parameterElement };
    }

    private void addModule(String module, String name, boolean commas)
    {
        defines.add(addDefine(module, commas)[0]);
        parameters.add(addParameter(name, commas)[0]);
    }
}
