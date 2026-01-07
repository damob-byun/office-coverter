package com.hancom.weboffice.hwp.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class WebhwpUnix {
    private static final Logger LOG = LoggerFactory.getLogger(WebhwpUnix.class);
    private static final int RETRY_COUNT = 3;
    public static final String HncPath = System.getProperty("user.dir") + "/hnc";
    private static final int RETRY_DELAY = 300;
    private static final java.lang.reflect.Field LIBRARIES = null;
    
    static {
        try {
            System.loadLibrary("hsp-nox");
            System.loadLibrary("HncXerCore");
            System.loadLibrary("HncXalMesg");
            System.loadLibrary("HncPtnCore");
            System.loadLibrary("HncXalCore");
            System.loadLibrary("HncXsecCore");
            System.loadLibrary("hwp_sdk_unix");
            System.out.println("Native library loaded.");

        } catch (UnsatisfiedLinkError e) {
            System.err.println("!!!Native code library failed to load.\n" + e);
        }
    }

    private static boolean HWP_FAIL(int hr) {
        return ((hr & 0x80000000L) != 0L);
    }

    private static boolean HWP_SUCCESS(int hr) {
        return !HWP_FAIL(hr);
    }

    public WebhwpUnix() {

        String hncPath = System.getProperty("user.dir") + "/hnc";
        String uploadPath = hncPath + "/upload";
        String tempPath = hncPath + "/temp";

        System.out.println("lib_path : " + System.getProperty("java.library.path"));
        LOG.info("HwpSDK: HncPath={}, UploadPath={}, TempDir={}", new Object[] { hncPath, uploadPath, tempPath });

        setString("HNC_DIR", hncPath);
        setString("HNC_SHARED_DIR", hncPath + "/Shared");
        setString("HNC_TEMP_DIR", tempPath);
        HwpSDKInit();
        LOG.info("HwpSDK initialization finished");

    }

    public int saveToJson(String filePath, String format, String jsonDirPath, String documentName, String args,
            String bindataPath) {
        int ret = -1;
        if (!bindataPath.endsWith(File.separator))
            bindataPath = bindataPath + File.separator;
        args = args + "bindatapath:" + bindataPath + ";checkdocdata:false;";
        for (int i = 0; i < 3; i++) {
            ret = HwpSaveFromTo(filePath, format, jsonDirPath + File.separator + documentName, "JSON", args, null);
            if (HWP_SUCCESS(ret))
                break;
            try {
                Thread.sleep(300L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOG.info("Retry saveToJson [{}/3] ErrorCode : {} from : {}, to : {}",
                    new Object[] { Integer.valueOf(i + 1),
                            String.format("0x%08x", new Object[] { Integer.valueOf(ret) }), filePath,
                            jsonDirPath + File.separator + documentName });
        }
        return ret;
    }

    public int saveToHwp(String jsonFilePath, String hwpDirPath, String documentName, String args, String bindataPath) {
        int ret = -1;
        if (!bindataPath.endsWith(File.separator))
            bindataPath = bindataPath + File.separator;
        args = args + "bindatapath:" + bindataPath + ";";
        LOG.info(args);
        for (int i = 0; i < 3; i++) {
            ret = HwpSaveFromTo(jsonFilePath, "JSON", hwpDirPath + File.separator + documentName, "HWP", args, null);
            if (HWP_SUCCESS(ret))
                break;
            try {
                Thread.sleep(300L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOG.info("Retry saveToHwp [{}/3] ErrorCode : {} from : {}, to : {}",
                    new Object[] { Integer.valueOf(i + 1),
                            String.format("0x%08x", new Object[] { Integer.valueOf(ret) }), jsonFilePath,
                            hwpDirPath + File.separator + documentName });
        }
        return ret;
    }

    public int saveAs(String inputFilePath, String ouputDirPath, String documentName, String format, String arg,
            String bindataPath, String password) {
        int ret = -1;
        if (!bindataPath.endsWith(File.separator))
            bindataPath = bindataPath + File.separator;
        arg = "bindatapath:" + bindataPath + ";" + arg;
        for (int i = 0; i < 3; i++) {
            ret = HwpSaveFromTo(inputFilePath, "JSON", ouputDirPath + File.separator + documentName, format, arg,
                    password);
            if (HWP_SUCCESS(ret))
                break;
            try {
                Thread.sleep(300L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOG.info("Retry saveAs [{}/3] ErrorCode : {} from : {}, to : {}",
                    new Object[] { Integer.valueOf(i + 1),
                            String.format("0x%08x", new Object[] { Integer.valueOf(ret) }), inputFilePath,
                            ouputDirPath + File.separator + documentName });
        }
        return ret;
    }
    public int saveFromHwp(String type, String inputFilePath, String ouputDirPath, String documentName, String format, String arg,
            String bindataPath, String password) {
        int ret = -1;
        if (!bindataPath.endsWith(File.separator))
            bindataPath = bindataPath + File.separator;
        arg = "bindatapath:" + bindataPath + ";" + arg;
        LOG.info(arg);
        for (int i = 0; i < 3; i++) {
            ret = HwpSaveFromTo(inputFilePath, type, ouputDirPath + File.separator + documentName, format, arg,
                    password);
            if (HWP_SUCCESS(ret))
                break;
            try {
                Thread.sleep(300L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOG.info("Retry saveAs [{}/3] ErrorCode : {} from : {}, to : {}",
                    new Object[] { Integer.valueOf(i + 1),
                            String.format("0x%08x", new Object[] { Integer.valueOf(ret) }), inputFilePath,
                            ouputDirPath + File.separator + documentName });
        }
        return ret;
    }

    public int savePageImage(String inputFilePath, String ouputDirPath, String imageName, String format, String arg,
            String bindataPath, int pageno, int resolution, int depth) {
        int ret = -1;
        if (!bindataPath.endsWith(File.separator))
            bindataPath = bindataPath + File.separator;
        arg = "bindatapath:" + bindataPath + ";" + arg;
        ret = HwpSavePageImage(inputFilePath, "JSON", ouputDirPath + File.separator + imageName, format, arg, pageno,
                resolution, depth);
        return ret;
    }

    public String detectFormat(String filePath) {
        synchronized (WebhwpUnix.class) {
            String ret = "";
            ret = HwpDetectFormat(filePath);
            return ret;
        }
    }

    public void hwpRelease() {
        HwpSDKRelease();
    };

    private native long nativeInit(String paramString1, String paramString2);

    private native int nativeFini(long paramLong);

    private native int HwpSDKInit();

    private native void HwpSDKRelease();

    private native int HwpSaveFromTo(String paramString1, String paramString2, String paramString3, String paramString4,
            String paramString5, String paramString6);

    private native int HwpSavePageImage(String paramString1, String paramString2, String paramString3,
            String paramString4, String paramString5, int paramInt1, int paramInt2, int paramInt3);

    private native String HwpDetectFormat(String paramString);

    private native void setString(String paramString1, String paramString2);

}
