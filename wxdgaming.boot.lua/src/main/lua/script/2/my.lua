function t1()
    local ts = tostring(os.time())
    print("2-1 lua script holle world " .. type(ts) .. " " .. ts .. " " .. printThreadInfo())
end