package com.chrisfolger.needsmoredojo.core.amd;

import com.chrisfolger.needsmoredojo.core.util.DefineStatement;
import com.chrisfolger.needsmoredojo.testutil.MockJSArrayLiteralExpression;
import com.chrisfolger.needsmoredojo.testutil.MockJSFunctionExpression;
import org.junit.Before;
import org.junit.Test;

public class TestAMDImportLocator
{
    private AMDImportLocator locator;

    @Before
    public void setup()
    {
        locator = new AMDImportLocator();
    }

    @Test
    public void foo()
    {
        MockJSArrayLiteralExpression literal = new MockJSArrayLiteralExpression(new String[] { "a/b/a", "a/b/b", "a/b/c"});
        MockJSFunctionExpression function = new MockJSFunctionExpression(new String[] { "a", "b", "c"});

        DefineStatement defineStatementItems = new DefineStatement(literal, function, "irrelevant");
    }
}
