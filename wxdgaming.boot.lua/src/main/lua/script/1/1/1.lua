Player = {
    id = "1";
    name = "d";

    show = function()
        return "id=" .. id .. ", name=" .. name
    end;

    len = function()
        return 0
    end
}

function Player:new(_i, _n)
    o = {}

    return o
end

function login()
    print(Player:new(1, "roe").show())
    print(Player:new(2, "red").show())

end