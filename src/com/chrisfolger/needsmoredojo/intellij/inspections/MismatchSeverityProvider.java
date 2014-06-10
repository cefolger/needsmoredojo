package com.chrisfolger.needsmoredojo.intellij.inspections;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.SeveritiesProvider;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;

import java.util.ArrayList;
import java.util.List;

public class MismatchSeverityProvider extends SeveritiesProvider {
    @Override
    public List<HighlightInfoType> getSeveritiesHighlightInfoTypes() {
        final List<HighlightInfoType> result = new ArrayList<HighlightInfoType>();

        final HighlightSeverity SPELLING = new HighlightSeverity("NMDMismatch", HighlightSeverity.WARNING.myVal);
        TextAttributes textAttributes = new TextAttributes(JBColor.RED, null, null, null, 0);
        textAttributes.setErrorStripeColor(JBColor.red);

        HighlightInfoType highlightInfoType = new HighlightInfoType.HighlightInfoTypeImpl(SPELLING, TextAttributesKey.createTextAttributesKey("NMDMismatch", textAttributes));

        result.add(highlightInfoType);
        return result;
    }
}
