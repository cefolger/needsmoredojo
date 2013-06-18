package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.testutil.MockPsiFile;
import com.intellij.psi.PsiFile;
import org.junit.Before;
import org.junit.Test;

public class TestImportCreator
{
    private ImportCreator creator;

    @Before
    public void setup()
    {
        creator = new ImportCreator();
    }

    @Test
    public void testLibraryPriority()
    {
        PsiFile[] files = new PsiFile[] {
                new MockPsiFile("BorderContainer.js", "dojox"),
                new MockPsiFile("BorderContainer.js", "dijit")
        };

        String[] choices = creator.getChoicesFromFiles(files, ImportCreator.dojoLibraries, "BorderContainer");

        System.out.println(choices);
    }
}
