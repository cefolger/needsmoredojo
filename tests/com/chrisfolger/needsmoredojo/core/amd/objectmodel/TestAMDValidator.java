package com.chrisfolger.needsmoredojo.core.amd.objectmodel;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestAMDValidator
{
    private AMDValidator validator;
    private Map<String, String> exceptions;

    @Before
    public void setup()
    {
        validator = new AMDValidator();
        exceptions = new HashMap<String, String>();
    }

    @Test
    public void matchOnSimpleCase()
    {
        assertTrue(validator.defineMatchesParameter("dijit/layout/BorderContainer", "BorderContainer", exceptions));
    }

    @Test
    public void mismatchOnSimpleCase()
    {
        assertFalse(validator.defineMatchesParameter("dijit/layout/ContentPane", "BorderContainer", exceptions));
    }

    @Test
    public void matchOnTemplate()
    {
        assertTrue(validator.defineMatchesParameter("dojo/text!foo/template.html", "template", exceptions));
    }

    @Test
    public void matchOnTemplateWithTemplatesFolder()
    {
        assertTrue(validator.defineMatchesParameter("dojo/text!./templates/MainToolbar.html", "template", exceptions));
    }

    @Test
    public void matchOnTemplateWithRelativePath()
    {
        assertTrue(validator.defineMatchesParameter("dojo/text!./Foo.html", "template", exceptions));
    }

    @Test
    public void matchOnTemplateWithExplicitName()
    {
        assertTrue(validator.defineMatchesParameter("dojo/text!./Foo.html", "fooTemplate", exceptions));
    }

    @Test
    public void matchI18n()
    {
        assertTrue(validator.defineMatchesParameter("dojo/i18n!./resources/resources", "resources", exceptions));
    }

    @Test
    public void matchI18nWithConvention()
    {
        // I've seen all of these in the dojo libraries
        assertTrue(validator.defineMatchesParameter("dojo/i18n!./MainToolbar/resources", "resources", exceptions));
        assertTrue(validator.defineMatchesParameter("dojo/i18n!./MainToolbar/resources", "nlsMaintoolbar", exceptions));
        assertTrue(validator.defineMatchesParameter("dojo/i18n!./MainToolbar/resources", "i18nMaintoolbar", exceptions));
        assertTrue(validator.defineMatchesParameter("dojo/i18n!./nls/buttons", "_nls", exceptions));
    }

    @Test
    // we encourage sticking to the convention of using the filename as the parameter variable name
    public void typingMismatch()
    {
        assertFalse(validator.defineMatchesParameter("dijit/layout/ContentPane", "pane", exceptions));
    }

    @Test
    // we encourage the x-y -> xY convention because that's what's used in the dojo reference examples
    public void domClassesMatch()
    {
        assertTrue(validator.defineMatchesParameter("dojo/dom-construct", "domConstruct", exceptions));
    }

    // TODO performance penalty when we hit about 1000 lines

    @Test
    /**
     * due to presence of _ prefixed modules such as _WidgetBase, we have to account for this
     */
    public void testDefinesWithUnderscores()
    {
        assertTrue(validator.defineMatchesParameter("dijit/_WidgetBase", "WidgetBase", exceptions));
    }

    @Test
    /**
     * there is a bug where if you have a define without slashes it will cause a false positive
     */
    public void testNoSlashMatchesCorrectly()
    {
        assertTrue(validator.defineMatchesParameter("a", "a", exceptions));
    }

    @Test
    public void testDoubleQuotes()
    {
        assertTrue(validator.defineMatchesParameter("\"a\"", "a", exceptions));
    }

    @Test
    public void relativePathsMismatch()
    {
        assertFalse(validator.defineMatchesParameter("\"../../../../foo/bar/Element\"", "Eledment", exceptions));
    }

    @Test
    public void baseFxException()
    {
        assertTrue(validator.defineMatchesParameter("dojo/_base/fx", "baseFx", exceptions));
        assertTrue(validator.defineMatchesParameter("dojo/_base/fx", "fx", exceptions));
    }

    @Test
    public void testException()
    {
        // has is an explicit exception
        exceptions.put("dojo/sniff", "has");
        assertTrue(validator.defineMatchesParameter("dojo/sniff", "has", exceptions));
    }

    @Test
    public void testI18nModule()
    {
        assertFalse(validator.defineMatchesParameter("dojo/i18n", "resources", exceptions));
    }

    @Test
    public void testStandardConventionStillWorksWithExceptions()
    {
        // has is an explicit exception
        exceptions.put("dojo/sniff", "has");
        assertTrue(validator.defineMatchesParameter("dojo/sniff", "sniff", exceptions));
    }

    @Test
    public void testNamingResourcesIsValidForI18n()
    {
        assertTrue(validator.defineMatchesParameter("dojo/i18n!./foo/foonls", "resources", exceptions));
    }

    @Test
    /**
     * based on reported issue: Support for generic AMD loader plugins
     * https://github.com/cefolger/needsmoredojo/issues/87
     */
    public void testArbitraryAMDPluginIsNotFlaggedAsMismatchedForCommonCases()
    {
        assertTrue(validator.defineMatchesParameter("test/package/foo!../test", "test", exceptions));
        assertTrue(validator.defineMatchesParameter("foo!bar", "bar", exceptions));
        // if someone names the parameter the same name as the plugin, let it slide
        // this may be taken out in the future though if it proves to be too lax
        assertTrue(validator.defineMatchesParameter("foo!bar", "foo", exceptions));
        assertTrue(validator.defineMatchesParameter("foo!a/b/c/d", "d", exceptions));
        assertTrue(validator.defineMatchesParameter("foo!../../a", "a", exceptions));

        assertFalse(validator.defineMatchesParameter("foo!bar", "bare", exceptions));
        assertFalse(validator.defineMatchesParameter("foo!bar", "ba", exceptions));
    }

    @Test
    public void testExceptionsWithMixedCase()
    {
        exceptions.put("dojo/_base/lang", "dLang");
        exceptions.put("dijit/layout/ContentPane", "cPane");

        assertTrue(validator.defineMatchesParameter("dojo/_base/lang", "dLang", exceptions));
        assertTrue(validator.defineMatchesParameter("dijit/layout/ContentPane", "cPane", exceptions));
    }

    @Test
    public void testMatchOnCustomPlugin()
    {
        assertTrue(validator.defineMatchesParameter("dojo/foo!bar", "foo", exceptions));
    }
}
