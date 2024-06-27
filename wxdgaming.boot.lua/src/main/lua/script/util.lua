function printThreadInfo()
    local currentThread = coroutine.running()
    if currentThread then
        result = "当前线程:" .. tostring(currentThread)
    else
        result = "当前线程是主线程。"
    end
    return result
end

function logbackUtil()
    local logger_method = luajava.bindClass("wxdgaming.boot.agent.LogbackUtil");
    return logger_method;
end

function logger()
    local logger_method = luajava.bindClass("wxdgaming.boot.agent.LogbackUtil");
    return logger_method:logger();
end