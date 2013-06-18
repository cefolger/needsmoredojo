package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.file.PsiDirectoryImpl;

import static org.mockito.Mockito.mock;

public class MockPsiDirectory extends PsiDirectoryImpl
{
    private String path;

    public MockPsiDirectory(String path) {
        super(null, mock(VirtualFile.class));

        this.path = path;
    }

    @Override
    public String toString()
    {
        return path;
    }
}
