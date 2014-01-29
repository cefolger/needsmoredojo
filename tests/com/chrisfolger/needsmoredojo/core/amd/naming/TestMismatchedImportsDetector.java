
package com.chrisfolger.needsmoredojo.core.amd.naming;

import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestMismatchedImportsDetector
{
    private MismatchedImportsDetector detector;
    private MismatchedImportsDetectorCache cache;
    private List<NameException> exceptions;
    private DojoSettings settings;
    private Project project;

    @Before
    public void setup()
    {
        cache = new MismatchedImportsDetectorCache();
        settings = mock(DojoSettings.class);
        detector = new MismatchedImportsDetector();
        exceptions = new ArrayList<NameException>();
        project = mock(Project.class);
    }

    @Test
    public void simpleCorrectList()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/on"), createPsiElement("dojo/_base/array"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(0, detector.matchOnList(defines, parameters, exceptions, settings, cache).size());
    }

    @Test
    public void simpleMismatchList()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/on"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(2, detector.matchOnList(defines, parameters, exceptions, settings, cache).size());
    }

    @Test
    public void swappedOrderMismatch()
    {
        PsiElement[] defines = new PsiElement[] { createPsiElement("dojo/_base/array"), createPsiElement("dojo/on"), createPsiElement("dijit/layout/ContentPane")};
        PsiElement[] parameters = new PsiElement[] { createPsiElement("on"), createPsiElement("array"), createPsiElement("ContentPane")};

        assertEquals(2, detector.matchOnList(defines, parameters, exceptions, settings, cache).size());
    }

    private PsiElement createPsiElement(String text)
    {
        PsiElement result = mock(PsiElement.class);
        when(result.getText()).thenReturn(text);
        when(result.getProject()).thenReturn(project);

        return result;
    }
}
