package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.chrisfolger.needsmoredojo.core.amd.filesystem.AttachPointResolver;
import com.chrisfolger.needsmoredojo.core.settings.DojoSettings;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.lang.javascript.psi.JSThisExpression;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class DojoUnresolvedVariableInspection extends JSUnresolvedVariableInspection
{
    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName()
    {
        return "Needs More Dojo";
    }

    @NotNull
    @Override
    public String[] getGroupPath()
    {
        return new String[] { "JavaScript", "Needs More Dojo "};
    }

    @NotNull
    @Override
    public String getDisplayName()
    {
        return "Unresolved JavaScript variable (replaces default inspection)";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "JSUnresolvedVariable";
    }

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element) {
        Project project = element.getProject();
        DojoSettings settings = ServiceManager.getService(project, DojoSettings.class);
        if(!settings.isNeedsMoreDojoEnabled())
        {
            return super.isSuppressedFor(element);
        }

        if(element instanceof LeafPsiElement)
        {
            LeafPsiElement leafPsiElement = (LeafPsiElement) element;
            if(leafPsiElement.getElementType() == JSTokenTypes.IDENTIFIER &&
                    leafPsiElement.getParent() != null &&
                    leafPsiElement.getParent().getFirstChild() instanceof JSThisExpression)
            {
                return AttachPointResolver.getGotoDeclarationTargets(element, 0, null).length > 0 || super.isSuppressedFor(element);
            }
        }
        return super.isSuppressedFor(element);
    }
}
