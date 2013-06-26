package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.testutil.MockPsiFile;
import com.intellij.psi.PsiFile;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * the *Priority tests are to check if suggested imports for some common modules come up in the expected order
 */
public class TestImportCreator
{
    private ImportCreator creator;
    private List<SourceLibrary> libraries;

    @Before
    public void setup()
    {
        creator = new ImportCreator();
        libraries = new ArrayList<SourceLibrary>();

        libraries.add(new SourceLibrary("dijit", ""));
        libraries.add(new SourceLibrary("dojox", ""));
        libraries.add(new SourceLibrary("dojo", ""));
        libraries.add(new SourceLibrary("util", ""));
        libraries.add(new SourceLibrary("dgrid", ""));
    }

    @Test
    public void testDijitLibraryPriority()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("BorderContainer.js", "dojox/layout"),
                new MockPsiFile("BorderContainer.js", "dijit/layout")
        };

        String[] choices = creator.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "BorderContainer");

        assertEquals("dijit/layout/BorderContainer", choices[0]);
    }

    @Test
    public void testFunctionalPriority()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("functional.js", "dojox/lang"),
                new MockPsiFile("functional.js", "util/docscripts/tests")
        };

        String[] choices = creator.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "functional");

        assertEquals("dojox/lang/functional", choices[0]);
    }

    @Test
    // see Issue #24 entering a module with an underscore resolves into two underscores
    public void testWidgetWithUnderscoreDoesNotGetTwoUnderscoresInserted()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("_WidgetBase.js", "dijit")
        };

        String[] choices = creator.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "_WidgetBase");

        assertEquals("dijit/_WidgetBase", choices[0]);
    }

    @Test
    // a little unrealistic, but let's make sure this case works anyway
    public void testWidgetWithDoubleUnderscoresIsStillInsertedCorrectly()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("__WidgetBase.js", "dijit")
        };

        String[] choices = creator.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "__WidgetBase");

        assertEquals("dijit/__WidgetBase", choices[0]);
    }

    @Test
    public void testDgridPriority()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("Grid.js", "dojox/drawing/plugins/drawing"),
                new MockPsiFile("Grid.js", "dojox/grid"),
                new MockPsiFile("Grid.js", "dgrid"),
                new MockPsiFile("Grid.js", "dojox/charting/plot2d")
        };

        String[] choices = creator.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "Grid");

        assertEquals("dgrid/Grid", choices[0]);
    }

    @Test
    public void testDojoTestPriority()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("on.js", "dojo/tests"),
                new MockPsiFile("on.js", "dojo")
        };

        String[] choices = creator.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "on");

        assertEquals("dojo/on", choices[0]);
    }

    @Test
    public void testExternalLibrary()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("Grid.js", "website/static/js/website/foo")
        };

        libraries.add(new SourceLibrary("website", "website/static/js"));

        String[] choices = creator.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]) , "Grid" );

        assertEquals("website/foo/Grid", choices[0]);
    }

    @Test
    public void testLibrary_withNoSourceRoot()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("Grid.js", "website/static/js/dojo/foo")
        };

        libraries.add(new SourceLibrary("dojo", "website/static/js"));
        String[] choices = creator.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]) , "Grid" );

        assertEquals("dojo/foo/Grid", choices[0]);
    }

    @Test
    public void testDojoLibraryPriority()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("BorderContainer.js", "dojox"),
                new MockPsiFile("BorderContainer.js", "dojo")
        };

        String[] choices = creator.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "BorderContainer");

        assertEquals("dojo/BorderContainer", choices[0]);
    }
}
