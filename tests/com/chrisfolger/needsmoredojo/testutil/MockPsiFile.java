package com.chrisfolger.needsmoredojo.testutil;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.PsiFileImpl;
import org.jetbrains.annotations.NotNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockPsiFile extends PsiFileImpl
{
    private String name;
    private String path;
    private MockPsiDirectory directory;

    public MockPsiFile(String fileName, String path) {
        super(mock(FileViewProvider.class));

        this.name = fileName;
        this.path = path;

        directory = new MockPsiDirectory(path);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return new JavaScriptFileType();
    }

    @Override
    public PsiDirectory getContainingDirectory()
    {
        return this.directory;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor psiElementVisitor) {
        return;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public VirtualFile getVirtualFile()
    {
        VirtualFile file = mock(VirtualFile.class);
        when(file.getCanonicalPath()).thenReturn(this.path);
        return file;
    }
}
