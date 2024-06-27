function t1()
    local ts = tostring(os.time())
    print("lua script holle world " .. type(ts) .. " " .. ts .. " " .. printThreadInfo())
end

function t2(t_2)
    -- 通过 logback 输出日志
    logbackUtil():info("lua script t2 " .. type(t_2))
    logbackUtil():info("lua script t2 " .. t_2 .. " " .. printThreadInfo())
end

function t3(ts)
    -- 通过 logback 输出日志
    logbackUtil():info("lua script t3  调用index=" .. ts:index() .. " - " .. "start")
    logbackUtil():info("lua script t3  调用index=" .. ts:index() .. " - " .. type(ts) .. " - objVar=" .. objVar)
    logbackUtil():info("lua script t3  调用index=" .. ts:index() .. " - " .. ts:gString())--string
    logbackUtil():info("lua script t3  调用index=" .. ts:index() .. " - " .. ts:gStringValue())-- luavalue 转化 string
    logbackUtil():info("lua script t3  调用index=" .. ts:index() .. " - " .. ts:gValue()[1])-- 这个对象其实是数组
    logbackUtil():info("lua script t3  调用index=" .. ts:index() .. " - " .. "end")
end