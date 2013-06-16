package com.chrisfolger.needsmoredojo.inspections;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * Created with IntelliJ IDEA.
 * User: Chris
 * Date: 1/1/13
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class MismatchedImportsProvider implements InspectionToolProvider
{
    public Class[] getInspectionClasses()
    {
        return new Class[] { MismatchedImportsInspection.class};
    }
}