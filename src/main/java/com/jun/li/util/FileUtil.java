package com.jun.li.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    public static String readLastLine(File file) {
        List<String> metaLines = null;
        try {
            metaLines = IOUtils
                    .readLines(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));
            if (CollectionUtils.isEmpty(metaLines)) {
                return null;
            }
        } catch (IOException e) {
        }
        return metaLines.get(metaLines.size() - 1);
    }

    public static int getTotalLines(File file) throws IOException {
        FileReader in = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(in);
        String s = reader.readLine();
        int lines = 0;
        while (s != null) {
            lines++;
            s = reader.readLine();
        }
        reader.close();
        in.close();
        return lines;
    }

}
