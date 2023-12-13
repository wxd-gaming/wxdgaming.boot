<!DOCTYPE html>
<html>

<head>
	<meta charset="UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Ftp</title>

	<script charset="utf-8" type="text/javascript" src="https://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js"></script>
	<script charset="utf-8" type="text/javascript" src="/js/com.wxd.js"></script>

	<link rel="stylesheet" type="text/css" href="/style/com.wxd.css"/>


	<style>
        html, body {display: block;width: 100%;height: 100%;font-size: 12px;overflow: hidden;overflow-x: auto;background: rgba(98, 143, 139, 0.41);min-width: 600px;}

        html, body, table, tr, td {margin: 0px;border: 0px;box-sizing: border-box;}

        a:link {color: #0866b4;}

        a:visited { color: #0866b4;}

        label {padding-left: 5px;padding-right: 5px;}

        .tableDom {
            display: block;overflow: hidden;border: 0px;border-radius: 5px;box-sizing: border-box;background-color: #ddd;min-width: 600px;
            position: absolute; left: 5px;top: 60px;right: 5px;bottom: 35px; transform-box: revert;
            padding: 2px 10px 2px 2px;
            }

        table { width: 100%; height: 100%; border: 0px; /* 设置无间隙 */ border-collapse: collapse; line-height: 22px; border-spacing: 0px; box-sizing: border-box; }

        table th, table td { color: black; text-align: left; font-size: 12px; border: 1px solid lightgray;border-collapse: collapse; padding-left: 5px;max-width: 410px;overflow: hidden;white-space: nowrap; }

        /* 滚动条宽度 */
        ::-webkit-scrollbar { width: 8px;height: 8px; background-color: transparent; }

        /* 滚动条颜色 */
        ::-webkit-scrollbar-thumb { background-color: #A8A8A8; }

        table thead {width: calc(100% - 8px); background-color: #dbe4f6;text-align: left;font-weight: bolder;height: 40px; font-size: 14px;}

        table thead th {height: 40px;}

        table tbody { display: block; width: calc(100% + 8px); /*这里的8px是滚动条的宽度*/ height: calc(100%); overflow-y: scroll; -webkit-overflow-scrolling: touch; background-color: #ffffff;}

        table thead tr, table tbody tr { box-sizing: border-box; table-layout: fixed; display: table; width: 100%; }

        table tbody tr:nth-of-type(odd) { background: #FAFAFA; }

        table tbody tr:nth-of-type(even) { background: rgba(194, 188, 188, 0.25); }

        table th, table td {box-sizing: border-box; border-top: none; border-left: none; /*border-right: none;*/}

	</style>
	<script type="application/javascript">
        let urlQuery = new wxd.Map();

        $(() => {
            urlQuery.loadSearch();
        });

        function selectPageSize() {
            let pageSize = $("#selectpagesize").val();
            page(pageSize, -1);
        }

        function goto() {
            let pageIndex = $("#cur_pageNumber").val();
            page(-1, pageIndex);
        }

        function page(pageSize, pageIndex) {
            if (pageSize !== -1) {
                urlQuery.put("pageSize", pageSize);
            }
            if (pageIndex !== -1) {
                urlQuery.put("pageNumber", pageIndex);
            }

            urlQuery.put("search", $("#txt_search").val());

            let setUrl = window.location.protocol + "//" + window.location.host + "" + window.location.pathname + "?" + urlQuery.toString();
            console.log(setUrl);
            window.location = setUrl;
        }

        /*键盘按下事件*/
        function catchEnter(event) {
            if (event.keyCode === 13) {
                page(-1, 1);
            }
        }

	</script>
</head>

<body>
<br>
<br>
&nbsp;&nbsp;&nbsp;&nbsp;目录：<#list urls as url>
	<a href="${url.url}">${url.text}</a>&nbsp;&nbsp;
</#list>

<div class="tableDom">
	<table>
		<thead>
		<tr>
			<th style="width: 120px;">操作</th>
			<th style="width: 120px;">序号</th>
			<th>文件名</th>
			<th>最后修改日期</th>
			<th>大小</th>
		</tr>
		</thead>
		<tbody>
        <#list datas as data>
			<tr>
				<td style="width: 120px;">
                    <#if data.isFile >
						<a href="${data.url}&show=true" target="_blank">查看</a>
						<a href="${data.url}" target="_blank">下载</a>
                    <#else>
						<a href="${data.url}">下级</a>
                    </#if>
				</td>
				<td style="width: 120px;">${data.id}</td>
				<td>${data.text}</td>
				<td>${data.byteString}</td>
				<td>${data.dateString}</td>
			</tr>
        </#list>
		</tbody>
	</table>
</div>
<div style="position: absolute;bottom: 5px;left: 5px; right: 5px;text-align: center;">
	<input id="txt_search" type="text" style="width: 220px;" value="${search}" onkeyup="catchEnter(event)">
	&nbsp;&nbsp;
	<a href="#" onclick="page(-1, 1);return false;" title=""> 搜索 </a>
	&nbsp;&nbsp;&nbsp;&nbsp;
	每页显示数量：
	<select id="selectpagesize" style="width: 65px;" onchange="selectPageSize()">
		<option value="20" <#if pageSize == "20"> selected="selected" </#if>>20</option>
		<option value="30" <#if pageSize == "30"> selected="selected" </#if>>30</option>
		<option value="40" <#if pageSize == "40"> selected="selected" </#if>>40</option>
		<option value="50" <#if pageSize == "50"> selected="selected" </#if>>50</option>
		<option value="100" <#if pageSize == "100"> selected="selected" </#if>>100</option>
		<option value="200" <#if pageSize == "200"> selected="selected" </#if>>200</option>
		<option value="500" <#if pageSize == "500"> selected="selected" </#if>>500</option>
		<option value="1000" <#if pageSize == "1000"> selected="selected" </#if>>1000</option>
		<option value="10000" <#if pageSize == "10000"> selected="selected" </#if>>10000</option>
	</select>
	&nbsp;&nbsp;&nbsp;&nbsp;
	<a href="#" onclick="page(-1, 1);return false;"> 首页 </a>
	&nbsp;&nbsp;
	<a href="#" onclick="page(-1, ${pageUpIndex});return false;"> 上一页 </a>
	&nbsp;&nbsp;
	<label style="width: 45px;text-align: center;">${pageNumber}</label>
	&nbsp;&nbsp;/&nbsp;&nbsp;
	<label style="width: 45px;text-align: center;">${pageMaxNumber}</label>
	&nbsp;&nbsp;
	<a href="#" onclick="page(-1, ${pageNextIndex});return false;"> 下一页 </a>
	&nbsp;&nbsp;
	<a href="#" onclick="page(-1, ${pageMaxNumber});return false;"> 尾页 </a>
	&nbsp;&nbsp;
	<input id="cur_pageNumber" type="text" value="" style="width: 45px;text-align: center;">
	<a href="#" onclick="goto();return false;" title="当你想跳转到某一页"> 跳转 </a>
</div>
</body>

</html>