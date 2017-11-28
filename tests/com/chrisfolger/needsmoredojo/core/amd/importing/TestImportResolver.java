package com.chrisfolger.needsmoredojo.core.amd.importing;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.SourceLibrary;
import com.chrisfolger.needsmoredojo.testutil.MockPsiFile;
import com.intellij.openapi.project.impl.ProjectImpl;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.PsiManagerImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * the *Priority tests are to check if suggested imports for some common modules come up in the expected order
 */
public class TestImportResolver
{
    private ImportResolver resolver;
    private List<SourceLibrary> libraries;
    private FileViewProvider fileViewProviderMock;

    @Before
    public void setup()
    {
        resolver = new ImportResolver();
        libraries = new ArrayList<SourceLibrary>();

        // libraries are guaranteed to be sorted by length
        libraries.add(new SourceLibrary("dijit", "js/dijit", false));
        libraries.add(new SourceLibrary("dojox", "js/dojox", false));
        libraries.add(new SourceLibrary("dgrid", "js/dgrid", false));
        libraries.add(new SourceLibrary("dojo", "js/dojo", false));
        libraries.add(new SourceLibrary("util", "js/util", false));

        fileViewProviderMock = mock(FileViewProvider.class);
        PsiManagerEx psiManagerMock = mock(PsiManagerEx.class);
        ProjectImpl projectMock = mock(ProjectImpl.class);
        when(fileViewProviderMock.getManager()).thenReturn(psiManagerMock);
        when(psiManagerMock.getProject()).thenReturn(projectMock);
    }

    @Test
    public void testDijitLibraryPriority()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("BorderContainer.js", "js/dojox/layout", fileViewProviderMock),
                new MockPsiFile("BorderContainer.js", "js/dijit/layout", fileViewProviderMock)
        };

        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "BorderContainer", null);

        assertEquals("dijit/layout/BorderContainer", choices[0]);
    }

    @Test
    public void testFunctionalPriority()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("functional.js", "js/dojox/lang", fileViewProviderMock),
                new MockPsiFile("functional.js", "js/util/docscripts/tests", fileViewProviderMock)
        };

        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "functional", null);

        assertEquals("dojox/lang/functional", choices[0]);
    }

    @Test
    // see Issue #24 entering a module with an underscore resolves into two underscores
    public void testWidgetWithUnderscoreDoesNotGetTwoUnderscoresInserted()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("_WidgetBase.js", "js/dijit", fileViewProviderMock)
        };

        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "_WidgetBase", null);

        assertEquals("dijit/_WidgetBase", choices[0]);
    }

    @Test
    // a little unrealistic, but let's make sure this case works anyway
    public void testWidgetWithDoubleUnderscoresIsStillInsertedCorrectly()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("__WidgetBase.js", "js/dijit", fileViewProviderMock)
        };

        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "__WidgetBase", null);

        assertEquals("dijit/__WidgetBase", choices[0]);
    }

    @Test
    public void testDgridPriority()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("Grid.js", "js/dojox/drawing/plugins/drawing", fileViewProviderMock),
                new MockPsiFile("Grid.js", "js/dojox/grid", fileViewProviderMock),
                new MockPsiFile("Grid.js", "js/dgrid", fileViewProviderMock),
                new MockPsiFile("Grid.js", "js/dojox/charting/plot2d", fileViewProviderMock)
        };

        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "Grid", null);

        assertEquals("dgrid/Grid", choices[0]);
    }

    @Test
    public void testDojoTestPriority()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("on.js", "js/dojo/tests", fileViewProviderMock),
                new MockPsiFile("on.js", "js/dojo", fileViewProviderMock)
        };

        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "on", null);

        assertEquals("dojo/on", choices[0]);
    }

    @Test
    public void testExternalLibrary()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("StandbyWrapper.js", "C:/foo/path/website/static/js/website", fileViewProviderMock)
        };

        libraries.add(new SourceLibrary("website", "C:/foo/path/website/static/js/website", true));

        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]) , "StandbyWrapper" , null);

        assertEquals("website/StandbyWrapper", choices[0]);
    }

    @Test
    public void testLibrary_withNoSourceRoot()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("Grid.js", "website/static/js/dojo/foo", fileViewProviderMock)
        };

        libraries.add(new SourceLibrary("dojo", "website/static/js", true));
        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]) , "Grid", null );

        assertEquals("dojo/foo/Grid", choices[0]);
    }

    @Test
    public void testDojoLibraryPriority()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("BorderContainer.js", "js/dojox", fileViewProviderMock),
                new MockPsiFile("BorderContainer.js", "js/dojo", fileViewProviderMock)
        };

        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "BorderContainer", null);

        assertEquals("dojo/BorderContainer", choices[0]);
    }

    @Test
    public void correctDojoSourceReferenceWithExternalLibrary()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("ContentPane.js", "website/static/deps/dijit/layout", fileViewProviderMock)
        };

        libraries = new ArrayList<SourceLibrary>();
        libraries.add(new SourceLibrary("dijit", "website/static/deps/dijit", true));
        libraries.add(new SourceLibrary("website", "website/static/js/website", true));
        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]) , "ContentPane", null );

        assertEquals("dijit/layout/ContentPane", choices[0]);
    }

    @Test
    public void testRelativePathWithExternalModule()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("StandbyWrapper.js", "C:/foo/path/website/static/js/website", fileViewProviderMock)
        };

        libraries.add(new SourceLibrary("website", "C:/foo/path/website/static/js/website", true));

        PsiFile originalModule = new MockPsiFile("FooModule.js", "C:/foo/path/website/static/js/website/anotherpackage", fileViewProviderMock);
        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]) , "StandbyWrapper" , originalModule, true);

        assertEquals("../StandbyWrapper", choices[0]);
    }

    @Test
    public void testRelativePathWithExternalModuleInSameDirectory()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("StandbyWrapper.js", "C:/foo/path/website/static/js/website", fileViewProviderMock)
        };

        libraries.add(new SourceLibrary("website", "C:/foo/path/website/static/js/website", true));

        PsiFile originalModule = new MockPsiFile("FooModule.js", "C:/foo/path/website/static/js/website", fileViewProviderMock);
        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]) , "StandbyWrapper" , originalModule, true);

        assertEquals("./StandbyWrapper", choices[0]);
    }

    @Test
    public void testRelativePathWithExternalModuleInTopLevel()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("StandbyWrapper.js", "C:/foo/path/website/static/js/website/package", fileViewProviderMock)
        };

        libraries.add(new SourceLibrary("website", "C:/foo/path/website/static/js/website", true));

        PsiFile originalModule = new MockPsiFile("FooModule.js", "C:/foo/path/website/static/js/website", fileViewProviderMock);
        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]) , "StandbyWrapper" , originalModule, true);

        assertEquals("./package/StandbyWrapper", choices[0]);
    }

    @Test
    public void testRelativePathWithExternalModuleInAnotherPackage()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("StandbyWrapper.js", "C:/foo/path/website/static/js/theroot/website/package", fileViewProviderMock)
        };

        libraries.add(new SourceLibrary("theroot", "C:/foo/path/website/static/js/theroot", true));

        PsiFile originalModule = new MockPsiFile("FooModule.js", "C:/foo/path/website/static/js/theroot/other", fileViewProviderMock);
        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]) , "StandbyWrapper" , originalModule, true);

        assertEquals("../website/package/StandbyWrapper", choices[0]);
    }

    @Test
    public void pluginIsTakenIntoAccountCorrectly()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("text.js", "dojo", fileViewProviderMock)
        };

        libraries = new ArrayList<SourceLibrary>();
        libraries.add(new SourceLibrary("dojo", "dojo", true));
        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]) , "text!testing", null );

        assertEquals("dojo/text!testing", choices[0]);
    }

    @Test
    public void testImportOfExternalModuleFromDojoLibrary()
    {
        PsiFile original = new MockPsiFile("behavior.js", "website/static/js/dojo", fileViewProviderMock);

        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("MainToolbar.js", "website/static/js/website/widgets", fileViewProviderMock)
        };

        SourceLibrary website = new SourceLibrary("website", "website/static/js/website", true);
        libraries.add(website);

        String[] results = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "MainToolbar", original);
        assertEquals("website/widgets/MainToolbar", results[0]);
    }

    @Test
    public void testNlsModuleIsLowerPriorityWhenItHasTheSameNameAsTheModule()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("Comment.js", "js/dojo/Comment/nls", fileViewProviderMock),
                new MockPsiFile("Comment.js", "js/dojo/Comment", fileViewProviderMock)
        };

        String[] choices = resolver.getChoicesFromFiles(files, libraries.toArray(new SourceLibrary[0]), "Comment", null);

        assertEquals("dojo/Comment/Comment", choices[0]);
    }
}
