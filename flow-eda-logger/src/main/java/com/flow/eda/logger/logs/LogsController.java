package com.flow.eda.logger.logs;

import com.flow.eda.common.http.Result;
import com.flow.eda.common.model.Logs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** 日志服务接口 */
@Slf4j
@RestController
@RequestMapping("/api/v1/feign")
public class LogsController {
    private static final String ROOT = System.getProperty("user.dir");

    /**
     * 获取日志信息列表
     *
     * @param type 日志类型
     * @return 日志信息列表
     */
    @GetMapping("/logs")
    public Result<List<Logs>> getLogList(@RequestParam Type type) {
        if (Type.OPERATION == type) {
            return Result.of(getOperationLogs());
        } else {
            return Result.of(getRunningLogs());
        }
    }

    /**
     * 删除日志文件
     *
     * @param path 日志文件路径
     */
    @DeleteMapping("/logs")
    public void deleteLogs(@RequestBody List<String> path) {
        path.forEach(this::deleteLogFile);
    }

    /** 读取日志文件内容（首版全量返回，控制最大返回大小以防响应过大） */
    @GetMapping("/logs/content")
    public Result<String> getLogContent(@RequestParam String path) {
        try {
            // 仅允许在项目 logs 目录下读取
            Path p = Paths.get(ROOT + path).normalize();
            if (!p.toFile().exists()) {
                return Result.of("");
            }
            byte[] bytes = Files.readAllBytes(p);
            // 最大 1MB 防止过大响应
            int max = 1024 * 1024;
            if (bytes.length > max) {
                bytes = java.util.Arrays.copyOfRange(bytes, bytes.length - max, bytes.length);
            }
            return Result.of(new String(bytes, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("read log content failed:{}, path:{}", e.getMessage(), path);
            return Result.of("");
        }
    }

    /** 获取操作日志信息列表 */
    private List<Logs> getOperationLogs() {
        List<Logs> result = new ArrayList<>();
        String path = "/logs/operation";
        File dir = new File(ROOT + path);
        if (dir.exists()) {
            File[] logs = dir.listFiles();
            if (logs != null) {
                for (File file : logs) {
                    result.add(new Logs(file.getName()).fileInfo(path, file.length()));
                }
            }
        }
        return result;
    }

    /** 获取运行日志信息列表 */
    private List<Logs> getRunningLogs() {
        List<Logs> result = new ArrayList<>();
        String path = "/logs/running";
        File dir = new File(ROOT + path);
        if (!dir.exists()) {
            return result;
        }
        File[] flows = dir.listFiles();
        if (flows == null) {
            return result;
        }
        for (File flowDir : flows) {
            File[] files = flowDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    Logs logs = new Logs(file.getName(), flowDir.getName());
                    logs.fileInfo(path, file.length());
                    result.add(logs);
                }
            }
        }
        return result;
    }

    private void deleteLogFile(String path) {
        try {
            File file = new File(ROOT + path);
            boolean success = file.delete();
            if (success) {
                log.info("delete log file {} success", path);
            }
        } catch (Exception ignored) {
        }
    }

    /** 日志类型 */
    enum Type {
        /** 操作日志 */
        OPERATION,
        /** 运行日志 */
        RUNNING
    }
}
