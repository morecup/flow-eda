package com.flow.eda.web.log;

import com.flow.eda.common.http.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LogContentController {

    @Autowired private LogClient logClient;

    @GetMapping("/logs/content")
    public Result<String> getLogContent(@RequestParam String path) {
        return logClient.getLogContent(path);
    }
}
