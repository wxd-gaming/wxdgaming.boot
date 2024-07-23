function t1()
    local ts = tostring(os.time())
    print("2-1 lua script holle world " .. type(ts) .. " " .. ts .. " " .. printThreadInfo())
    print(testfun0())
    print(testfun0(1, 1, 1, 1))
    print(testfun1(1))
    print(testfun1(1, 3, 4, 5))
    print(testfun2(1, 2))
    print(testfun4())
end

function t2_2(c1, c2)
    return 1 + 1 + c1 + c2;
end

function t2_2(c)
    return 1 + 1 + c;
end

function t2_2()
    return 1 + 1;
end

function login()

end