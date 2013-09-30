package com.chrisfolger.needsmoredojo.core.amd.define.organizer;

public class InvalidDefineException extends Exception
{
    public InvalidDefineException() {
        super("The import block was incomplete or invalid");
    }
}
