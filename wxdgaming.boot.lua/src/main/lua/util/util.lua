function printThreadInfo()
    local currentThread = coroutine.running()
    if currentThread then
        result = "当前线程:" .. tostring(currentThread)
    else
        result = "当前线程是主线程。"
    end
    return result
end

-- 不要用这种方式，特别耗时
--function logbackUtil()
--    return luajava.bindClass("logback.LogbackUtil");
--end

--- 输出日志到logback中
function log_info(msg)
    -- logbackUtil 是启动的时候注入的全局变量, 这样在调用输出的时候耗时会好很多
    return logbackUtil:info(msg, "");
end

--- 获取数据
function getLuaData(key)
    return lua_data:get(key);
end

--- 设置数据
function setLuaData(key, value)
    return lua_data:put(key, value);
end