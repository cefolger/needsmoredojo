package com.chrisfolger.needsmoredojo.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestFileUtil
{
    @Test
    public void relativePathConversion()
    {
        assertEquals("../../StandbyWrapper.js", FileUtil.convertToRelativePath("website/About/ProjectList", "website/StandbyWrapper.js"));
        assertEquals("../../widgets/StandbyWrapper.js", FileUtil.convertToRelativePath("website/About/ProjectList", "website/widgets/StandbyWrapper.js"));
    }
}
