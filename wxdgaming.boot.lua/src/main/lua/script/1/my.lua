function t1()
    local ts = tostring(os.time())
    print("1-1 lua script holle world " .. type(ts) .. " " .. ts .. " " .. printThreadInfo())
end

function t2(t_2)
    -- 通过 logback 输出日志
    log_info("lua script t2 " .. type(t_2))
    log_info("lua script t2 " .. t_2 .. " " .. printThreadInfo())
end

function t3(ts)
    -- 通过调用 logback 日志输出 比调用 print 日志输出还要耗时
    -- 通过 logback 输出日志
    setLuaData("t3", ts:index())
    log_info("lua script t3 调用index=" .. ts:index() .. " - " .. "start")
    log_info("lua script t3 调用index=" .. ts:index() .. " - " .. type(ts))
    log_info("lua script t3 调用index=" .. ts:index() .. " - " .. ts:gString())--string
    log_info("lua script t3 调用index=" .. ts:index() .. " - " .. ts:gStringValue())-- luavalue 转化 string
    log_info("lua script t3 调用index=" .. ts:index() .. " - " .. ts:gValue()[1])-- 这个对象其实是数组

    ---- 创建了一个新的协同程序对象 co，其中协同程序函数打印传入的参数 i
    --co = coroutine.create(
    --        function(i)
    --            log_info("lua script t3 调用index=" .. i .. " 协程");
    --        end
    --)
    --coroutine.resume(co, ts:index())
    log_info("lua script t3 调用index=" .. ts:index() .. " - " .. "end" .. " - " .. getLuaData("t3"))
end

function t4(ts)
    -- 通过调用 logback 日志输出 比调用 print 日志输出还要耗时
    -- 通过 logback 输出日志
    setLuaData("t4", ts:index())
    print("lua script t4 调用index=" .. ts:index() .. " - " .. "start")
    print("lua script t4 调用index=" .. ts:index() .. " - " .. type(ts))
    print("lua script t4 调用index=" .. ts:index() .. " - " .. ts:gString())--string
    print("lua script t4 调用index=" .. ts:index() .. " - " .. ts:gStringValue())-- luavalue 转化 string
    print("lua script t4 调用index=" .. ts:index() .. " - " .. ts:gValue()[1])-- 这个对象其实是数组

    ---- 创建了一个新的协同程序对象 co，其中协同程序函数打印传入的参数 i
    --co = coroutine.create(
    --        function(i)
    --            print("lua script t3 调用index=" .. i .. " 协程");
    --        end
    --)
    --coroutine.resume(co, ts:index())

    print("lua script t4 调用index=" .. ts:index() .. " - " .. "end" .. " - " .. getLuaData("t4"))
end