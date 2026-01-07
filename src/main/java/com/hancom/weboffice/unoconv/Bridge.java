package com.hancom.weboffice.unoconv;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Bridge {

    List<ProcessBuilder> pList = new ArrayList<ProcessBuilder>();
    boolean isRunning = false;
    boolean startFlag = false;
    public Bridge(){


    }

    public void addToQueue(String docPath, String outputDir) throws IOException, InterruptedException {
        List<String> commandList = new ArrayList<String>();
        //commandList.add("/usr/bin/unoconv -f pdf "+ dest.getAbsolutePath());
        commandList.add("unoconv");
        commandList.add("-f");
        commandList.add("pdf");
        commandList.add(docPath);
        ProcessBuilder build = new ProcessBuilder(commandList);
        build.directory(new File(outputDir));
        pList.add(build);
        run();
    }

    private void run() throws IOException, InterruptedException {
        if(isRunning){
            startFlag = true;
            return;
        }else{
            startFlag = false;
        }
        Iterator<ProcessBuilder> it = pList.iterator();
        while (it.hasNext()) {
            ProcessBuilder b = it.next();
            isRunning = true;
            Process p = b.start();
            p.waitFor();
            it.remove();
        }
        isRunning = false;
        if(startFlag){
            run();
        }
    }

}
