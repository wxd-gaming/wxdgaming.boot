let panels = {};
let navs = {};

let self_tile = document.title;

function changeMenu(e, href) {
    let title = $(e).text();
    try {
        let urlQuery = new wxd.Map().loadSearch();
        let map = "authorization=" + urlQuery.get("authorization");
        if (href.indexOf("?") >= 0) {
            href += "&";
        } else {
            href += "?";
        }
        href += map;
        let panel = panels[title];
        if (panel == null || panel == undefined) {
            panel = document.createElement("iframe");
            panel.setAttribute("id", title);
            panel.setAttribute("onload", "hide_loading();")
            $("#top_td_menu").append(panel);
            panels[title] = panel;

            let label = document.createElement("label");
            label.innerText = title;
            label.setAttribute("onclick", "changePanel(this);");
            label.setAttribute("class", "label");
            $(".div_panel").append(label);
            navs[title] = label;
        }

        $("#div_loading").show();
        changePanel(navs[title]);
        panel.setAttribute("src", href);

    } catch (e) {
        console.error(e);
    }
}

let up_nav_sel = null;

function changePanel(sender) {
    if (up_nav_sel != null) {
        $(up_nav_sel).removeClass("nav_sel");
    }
    let text = $(sender).text();
    document.title = self_tile + "-" + text;
    $(sender).addClass("nav_sel");
    let panel = panels[text];
    changePanel0(panel);
    up_nav_sel = sender;
}

function changePanel0(panel) {
    $("#top_td_menu").children().each(function (e, obj) {
        if (obj === panel) {
            $(obj).show();
        } else {
            $(obj).hide();
        }
    });
}

function hide_loading() {
    setTimeout(() => {
        $("#div_loading").hide();
    }, 100);
}

var up_e = null;
var up_ul = null;

function show_nav(element, ul) {

    let b = element === up_e;

    if (up_e != null) {
        close_nav(up_e, up_ul);
    }

    if (b) return;

    $(element).text("△");
    $(element).parent().addClass("li_sel");
    $("#" + ul).slideDown();

    up_e = element;
    up_ul = ul;
}

function close_nav(element, ul) {
    $(element).text("▽");
    $(element).parent().removeClass("li_sel");
    $("#" + ul).slideUp();
    up_e = null;
    up_ul = null;
}

// $(() => {
//     /*实现第一个标签选中效果 延迟执行是因为网络或者其他原因导致的*/
//     setTimeout(() => {
//         $(".lh:first").find("a:first").click();
//     }, 800);
// });