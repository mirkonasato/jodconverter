//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
//    -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
//    -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.office;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

import org.artofsolving.jodconverter.util.PlatformUtils;

import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.UnoRuntime;

public class OfficeUtils {

    public static final String SERVICE_DESKTOP = "com.sun.star.frame.Desktop";

    private OfficeUtils() {
        throw new AssertionError("utility class must not be instantiated");
    }

    public static <T> T cast(Class<T> type, Object object) {
        return (T) UnoRuntime.queryInterface(type, object);
    }

    public static PropertyValue property(String name, Object value) {
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.Name = name;
        propertyValue.Value = value;
        return propertyValue;
    }

    @SuppressWarnings("unchecked")
    public static PropertyValue[] toUnoProperties(Map<String,?> properties) {
        PropertyValue[] propertyValues = new PropertyValue[properties.size()];
        int i = 0;
        for (Map.Entry<String,?> entry : properties.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                Map<String,Object> subProperties = (Map<String,Object>) value;
                value = toUnoProperties(subProperties);
            }
            propertyValues[i++] = property((String) entry.getKey(), value);
        }
        return propertyValues;
    }

    public static String toUrl(File file) {
        String path = file.toURI().getRawPath();
        String url = path.startsWith("//") ? "file:" + path : "file://" + path;
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static File getDefaultOfficeHome() {
        if (System.getProperty("office.home") != null) {
            return new File(System.getProperty("office.home"));
        }
        if (PlatformUtils.isWindows()) {
            // %ProgramFiles(x86)% on 64-bit machines; %ProgramFiles% on 32-bit ones
            String programFiles = System.getenv("ProgramFiles(x86)");
            if (programFiles == null) {
                programFiles = System.getenv("ProgramFiles");
            }
            return findOfficeHome(
                programFiles + File.separator + "OpenOffice.org",
                programFiles + File.separator + "LibreOffice"
            );
        } else if (PlatformUtils.isMac()) {
            File officeHome = findOfficeHome(
                "/Applications/OpenOffice.org.app/Contents",
                "/Applications/LibreOffice.app/Contents"
            );

            // Try with locate
            if (officeHome == null) {
            	officeHome = locateOfficeHome();
            }
            return officeHome;
        } else {
            // Linux or other *nix variants
            File officeHome = findOfficeHome(
                "/opt/openoffice",
                "/opt/libreoffice",
                "/usr/lib/openoffice",
                "/usr/lib/libreoffice",
                "/usr/local/lib/openoffice",
                "/usr/local/lib/libreoffice"

            );

            // Try with locate
            if (officeHome == null) {
            	officeHome = locateOfficeHome();
            }
            return officeHome;
        }
    }

    private static File locateOfficeHome() {

        String s = null;
        String cmd = "locate soffice.bin";
        try {
        	Process p = Runtime.getRuntime().exec(cmd);
        	int i = p.waitFor();
        	if (i == 0){
        		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

        		// read the output from the command
        		while ((s = stdInput.readLine()) != null) {
        			File f = new File(s);
        			if (f.exists() && f.getName().equalsIgnoreCase("soffice.bin") && f.getParentFile() != null && f.getParentFile().getParentFile() != null) {
        				return f.getParentFile().getParentFile();
        			}
        		}
        	}
        	else {
        		BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        		// read the error output from the command
        		while ((s = stdErr.readLine()) != null) {
        		}
        	}
        } catch (Throwable th) {
        }
        return null;
    }

    private static File findOfficeHome(String... knownPaths) {
        for (String path : knownPaths) {
            File home = new File(path);
            File containerPath = home.getParentFile();
            if (containerPath != null && containerPath.isDirectory()) {
            	for (File subDir : containerPath.listFiles()) {
            		if (subDir.isDirectory() && subDir.getName().startsWith(home.getName())) {
                        if (getOfficeExecutable(subDir).isFile()) {
                            return home;
                        }
            		}
            	}
            }
        }
        return null;
    }

    public static File getOfficeExecutable(File officeHome) {
        if (PlatformUtils.isMac()) {
            return new File(officeHome, "MacOS/soffice.bin");
        } else {
            return new File(officeHome, "program/soffice.bin");
        }
    }
}
