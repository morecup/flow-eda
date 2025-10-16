package com.flow.eda.web.log;

import com.flow.eda.common.exception.MissingRequestParameterException;
import com.flow.eda.common.http.Result;
import com.flow.eda.common.model.Logs;
import com.flow.eda.common.utils.CollectionUtil;
import com.flow.eda.web.http.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class LogController {
    @Autowired private LogService logService;

    @OperationLog
    @GetMapping("/logs")
    public PageResult<Logs> logList(LogRequest request) {
        if (request.getType() == null) {
            throw new MissingRequestParameterException("type");
        }
        String username = null; // 无用户区分
        List<Logs> logList = logService.getLogList(request, username);
        return PageResult.ofPage(logList, request);
    }

    @OperationLog
    @DeleteMapping("/logs")
    public Result<String> deleteLogs(@RequestBody List<String> ids) {
        // 无用户区分，直接删除
        logService.deleteLogs(ids);
        return Result.ok();
    }
}
