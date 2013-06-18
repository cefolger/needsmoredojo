package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.amd.AMDImportOrganizer;
import com.intellij.psi.PsiElement;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Chris
 * Date: 1/7/13
 * Time: 10:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestAMDImportOrganizer
{
    private AMDImportOrganizer organizer;

    @Before
    public void setup()
    {
        organizer = new AMDImportOrganizer();
    }

    @Test
    public void sortingOfSimpleCase()
    {
        List<PsiElement> defines = new ArrayList<PsiElement>();
        defines.add(createPsiElement("dojo/z"));
        defines.add(createPsiElement("dojo/a"));
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        parameters.add(createPsiElement("z"));
        parameters.add(createPsiElement("a"));

        AMDImportOrganizer.SortingResult results = organizer.sortDefinesAndParameters(defines, parameters);

        assertEquals(2, results.getDefines().length);
        assertEquals(2, results.getParameters().length);

        assertEquals("dojo/a", results.getDefines()[0].getElement().getText());
        assertEquals("a", results.getParameters()[0].getElement().getText());
    }

    @Test
    /**
     * when we load plugins, do not sort them!
     */
    public void pluginsAreAccountedFor()
    {
        List<PsiElement> defines = new ArrayList<PsiElement>();
        defines.add(createPsiElement("'dojo/c'"));
        defines.add(createPsiElement("'../a'"));
        defines.add(createPsiElement("'dojo/z'"));
        defines.add(createPsiElement("'dojo/a!'"));
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        parameters.add(createPsiElement("c"));
        parameters.add(createPsiElement("a"));
        parameters.add(createPsiElement("z"));

        AMDImportOrganizer.SortingResult results = organizer.sortDefinesAndParameters(defines, parameters);

        assertEquals("'../a'", results.getDefines()[0].getElement().getText());
        assertEquals("a", results.getParameters()[0].getElement().getText());

        assertEquals("'dojo/a!'", results.getDefines()[3].getElement().getText());
    }

    @Test
    public void testDoubleQuotes()
    {
        List<PsiElement> defines = new ArrayList<PsiElement>();
        defines.add(createPsiElement("\"dojo/c\""));
        defines.add(createPsiElement("\"dojo/a!\""));
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        parameters.add(createPsiElement("c"));

        AMDImportOrganizer.SortingResult results = organizer.sortDefinesAndParameters(defines, parameters);

        assertEquals("\"dojo/c\"", results.getDefines()[0].getElement().getText());
    }

    @Test
    public void quotesAreCounted()
    {
        List<PsiElement> defines = new ArrayList<PsiElement>();
        defines.add(createPsiElement("'dojo/c'"));
        defines.add(createPsiElement("'dojo/z'"));
        defines.add(createPsiElement("\"dojo/a\""));
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        parameters.add(createPsiElement("c"));
        parameters.add(createPsiElement("z"));
        parameters.add(createPsiElement("a"));

        AMDImportOrganizer.SortingResult results = organizer.sortDefinesAndParameters(defines, parameters);

        assertEquals(2, results.getSingleQuotes());
        assertEquals(1, results.getDoubleQuotes());
    }

    @Test
    public void dojoModulesAreSorted()
    {
        List<PsiElement> defines = new ArrayList<PsiElement>();
        defines.add(createPsiElement("'dojo/c'"));
        defines.add(createPsiElement("'../a'"));
        defines.add(createPsiElement("'dojo/z'"));
        defines.add(createPsiElement("'dojo/a'"));
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        parameters.add(createPsiElement("c"));
        parameters.add(createPsiElement("a"));
        parameters.add(createPsiElement("z"));
        parameters.add(createPsiElement("a"));

        AMDImportOrganizer.SortingResult results = organizer.sortDefinesAndParameters(defines, parameters);

        assertEquals("'../a'", results.getDefines()[0].getElement().getText());
        assertEquals("a", results.getParameters()[0].getElement().getText());

        assertEquals("'dojo/z'", results.getDefines()[3].getElement().getText());
    }

    @Test
    public void repeatedElementsAreRemoved()
    {
        List<PsiElement> defines = new ArrayList<PsiElement>();
        defines.add(createPsiElement("'dojo/c'"));
        defines.add(createPsiElement("'dojo/z'"));
        defines.add(createPsiElement("'dojo/c'"));
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        parameters.add(createPsiElement("c"));
        parameters.add(createPsiElement("a"));
        parameters.add(createPsiElement("c"));

        AMDImportOrganizer.SortingResult results = organizer.sortDefinesAndParameters(defines, parameters);

        assertTrue(results.getDefines()[1].isInactive());
    }

    @Test
    public void itemsLoadedButNotReferencedAreNotSorted()
    {
        List<PsiElement> defines = new ArrayList<PsiElement>();
        defines.add(createPsiElement("'dojo/c'"));
        defines.add(createPsiElement("'../a'"));
        defines.add(createPsiElement("'dojo/z'"));
        defines.add(createPsiElement("'dojo/b'"));
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        parameters.add(createPsiElement("c"));
        parameters.add(createPsiElement("a"));
        parameters.add(createPsiElement("z"));

        AMDImportOrganizer.SortingResult results = organizer.sortDefinesAndParameters(defines, parameters);

        assertEquals("'../a'", results.getDefines()[0].getElement().getText());
        assertEquals("a", results.getParameters()[0].getElement().getText());

        assertEquals("'dojo/b'", results.getDefines()[3].getElement().getText());
    }

    @Test
    public void repeatsAreRemoved()
    {
        List<PsiElement> defines = new ArrayList<PsiElement>();
        defines.add(createPsiElement("'dojo/c'"));
        defines.add(createPsiElement("'dojo/z'"));
        defines.add(createPsiElement("'dojo/c'"));
        List<PsiElement> parameters = new ArrayList<PsiElement>();
        parameters.add(createPsiElement("c"));
        parameters.add(createPsiElement("a"));
        parameters.add(createPsiElement("c"));

        AMDImportOrganizer.SortingResult results = organizer.sortDefinesAndParameters(defines, parameters);

        AMDImportOrganizer.SortedPsiElementAdapter[] resultingDefines = results.getDefines();
        assertTrue(resultingDefines[0].isInactive() || resultingDefines[1].isInactive() || resultingDefines[2].isInactive());

        AMDImportOrganizer.SortedPsiElementAdapter[] resultingParameters = results.getParameters();
        assertTrue(resultingParameters[0].isInactive() || resultingParameters[1].isInactive() || resultingParameters[2].isInactive());
    }

    private PsiElement createPsiElement(String text)
    {
        PsiElement result = mock(PsiElement.class);
        when(result.getText()).thenReturn(text);

        return result;
    }
}
