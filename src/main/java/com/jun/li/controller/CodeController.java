package com.jun.li.controller;


import com.jun.li.core.Result;
import com.jun.li.core.ResultGenerator;
import com.jun.li.res.CodeRes;
import com.jun.li.util.FileUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/tagmanager")
public class CodeController {
    private static final Logger LOG = LoggerFactory.getLogger(CodeController.class);
    public static final String DEFAULT_PAGESIZE = "50";
    public static final int MAX_PAGESIZE = 10000;

    @Value("${store.path}")
    private String storePath;
    public static final String DONE = "DONE";
    public static final String DOING = "DOING";
    public static final String REGEX = "\001";


    @GetMapping("/healthCheck")
    public Result<String> healthCheck() {
        return ResultGenerator.genSuccessResult("OK");
    }


    @GetMapping("/getBatchCodes")
    public Result<CodeRes> getCodes(@RequestParam("lineCode") String machineNo, @RequestParam(value = "timeNo", required = false) String timeNo,
                                    @RequestParam(value = "pageSize", required = false, defaultValue = DEFAULT_PAGESIZE) int pageSize) {
        //1.先从machineNo_meta.txt中读取 如果不存在此文件或者最后一行是读完了的状态
        // 则新挑选一个归属此machineNo的文件继续读
        if (StringUtils.isBlank(machineNo)) {
            return ResultGenerator.genFailResult("lineCode is empty.");
        }
        if (pageSize > MAX_PAGESIZE || pageSize <= 0) {
            return ResultGenerator.genFailResult("pageSize must large than 0 and less than 10000.");
        }
        File readingFile;
        int lineNumber = 0;
        File machineNoMeta = new File(storePath + "meta_info_" + machineNo + ".txt");
        if (!machineNoMeta.exists()) {
            try {
                machineNoMeta.createNewFile();
            } catch (IOException e) {
                LOG.error("IOException", e);
            }
            File nextFile = getNextFile(machineNo, machineNoMeta, null);
            if (nextFile == null) {
                // 没有需要执行的文件了 return
                return ResultGenerator.genSuccessResult(new CodeRes(Collections.emptyList(), 0));
            }
            // 在最后一行加上重新挑选的归属此machineNo的文件
            try {
                Files.write(Paths.get(machineNoMeta.getAbsolutePath()), (nextFile.getName() + REGEX + "0" + REGEX + DOING).getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                LOG.error("IOException", e);
            }
            readingFile = nextFile;
        } else {
            String lastLine = FileUtil.readLastLine(machineNoMeta);
            if (StringUtils.isBlank(lastLine) || DONE.equals(lastLine.split(REGEX)[2])) {
                // meta.txt内容为空 或者 最后一行内容表示此文件读完了
                File nextFile = getNextFile(machineNo, machineNoMeta, null);
                if (nextFile == null) {
                    // 没有需要执行的文件了 return
                    return ResultGenerator.genSuccessResult(new CodeRes(Collections.emptyList(), 0));
                }
                // 在最后一行加上重新挑选的归属此machineNo的文件
                try {
                    Files.write(Paths.get(machineNoMeta.getAbsolutePath()), (nextFile.getName() + REGEX + "0" + REGEX + DOING).getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    LOG.error("IOException", e);
                }
                readingFile = nextFile;

            } else {
                readingFile = new File(storePath + lastLine.split(REGEX)[0]);
                lineNumber = Integer.parseInt(lastLine.split(REGEX)[1]);
            }
        }


        // 接着lineNumber往下读
        CodeRes res = new CodeRes();
        List<String> result = null;
        try {
            result = readLines(readingFile, lineNumber, pageSize);

        } catch (IOException e) {
            LOG.error("IOException", e);
        }

        if (result == null) {
            return ResultGenerator.genFailResult("system error");
        }

        // 更新读取行数的水位线 若刚好读完则把是否读完标志置为1 换行下个读的文件也更新好 若没有读完则正常更新水位线
        try {
            List<String> metaLines = IOUtils
                    .readLines(new InputStreamReader(Files.newInputStream(machineNoMeta.toPath()), StandardCharsets.UTF_8));

            String lastLine = metaLines.get(metaLines.size() - 1);
            int newLastLineNumber = lineNumber + result.size();
            String fileName = lastLine.split(REGEX)[0];
            if (FileUtil.getTotalLines(readingFile) == newLastLineNumber) {
                // 刚好读完 换行
                metaLines.remove(metaLines.size() - 1);
                metaLines.add(fileName + REGEX + newLastLineNumber + REGEX + DONE);
                File nextFile = getNextFile(machineNo, machineNoMeta, fileName);
                if (nextFile != null) {
                    metaLines.add(nextFile.getName() + REGEX + "0" + REGEX + DOING);
                }
            } else {
                // 没读完
                metaLines.remove(metaLines.size() - 1);
                metaLines.add(fileName + REGEX + newLastLineNumber + REGEX + DOING);
            }
            IOUtils.writeLines(metaLines, null, new FileOutputStream(machineNoMeta, false), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("IOException", e);
        }
        res.setThis_nums(result.size());
        res.setLists(result);
        return ResultGenerator.genSuccessResult(res);
    }

    private List<String> readLines(File sourceFile, int lineNumber, int lineCount)
            throws IOException {
        List<String> result = new ArrayList<>();
        FileReader in = new FileReader(sourceFile);
        LineNumberReader reader = new LineNumberReader(in);
        if (lineNumber < 0) {
            return Collections.emptyList();
        }
        String line;
        while ((line = reader.readLine()) != null) {
            if (reader.getLineNumber() > lineNumber && reader.getLineNumber() <= (lineNumber + lineCount)) {
                result.add(line);
            }
        }
        reader.close();
        in.close();
        return result;
    }


    private File getNextFile(String machineNo, File meta, String additionFileName) {
        List<String> doneFiles = new ArrayList<>();
        List<String> metaLines = null;
        try {
            metaLines = IOUtils
                    .readLines(new InputStreamReader(Files.newInputStream(meta.toPath()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOG.error("IOException", e);
        }
        if (!CollectionUtils.isEmpty(metaLines)) {
            for (String metaLine : metaLines) {
                String isDone = metaLine.split(REGEX)[2];
                String fileName = metaLine.split(REGEX)[0];
                if (DONE.equals(isDone)) {
                    doneFiles.add(fileName);
                }
            }
        }
        if (StringUtils.isNoneEmpty(additionFileName)) {
            doneFiles.add(additionFileName);
        }
        for (File file : Objects.requireNonNull(new File(storePath).listFiles())) {
            String fileName = file.getName();
            if (fileName.substring(fileName.lastIndexOf(".")).equals(".txt")
                    && fileName.startsWith(machineNo + "_") && !doneFiles.contains(fileName)) {
                return file;
            }
        }
        return null;
    }
}
