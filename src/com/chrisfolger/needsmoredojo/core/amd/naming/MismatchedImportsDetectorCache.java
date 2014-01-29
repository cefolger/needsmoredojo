package com.chrisfolger.needsmoredojo.core.amd.naming;

import com.intellij.psi.PsiFile;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MismatchedImportsDetectorCache
{
    private ConcurrentHashMap<PsiFile, ConcurrentHashMap<String, String>> pathReferences;

    public MismatchedImportsDetectorCache()
    {
        pathReferences = new ConcurrentHashMap<PsiFile, ConcurrentHashMap<String, String>>();

        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for(ConcurrentHashMap<String, String> map : pathReferences.values())
                {
                    map.clear();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Logger.getLogger(MismatchedImportsDetectorCache.class).warn("thread interrupted ", e);
                }
            }
        };
        service.scheduleWithFixedDelay(runnable, 0, 5, TimeUnit.SECONDS);
    }

    public String getAbsolutePath(PsiFile file, String moduleImport)
    {
        if(file == null)
        {
            return null; // can't resolve it
        }

        if(pathReferences.containsKey(file) && pathReferences.get(file).containsKey(moduleImport))
        {
            return pathReferences.get(file).get(moduleImport);
        }
        else
        {
            return null;
        }
    }

    public void put(PsiFile file, String module, String absolutePath)
    {
        if(file == null)
        {
            return;
        }

        pathReferences.putIfAbsent(file, new ConcurrentHashMap<String, String>());
        pathReferences.get(file).putIfAbsent(module, absolutePath);
    }
}
