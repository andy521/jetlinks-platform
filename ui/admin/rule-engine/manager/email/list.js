importResource("/admin/css/common.css");

importMiniui(function () {
    mini.parse();
    require(["request", "miniui-tools", "search-box", "message"], function (request, tools, SearchBox, message) {

        var tag = request.getParameter("tag");
        new SearchBox({
            container: $("#search-box"),
            onSearch: search,
            initSize: 2
        }).init();

        tools.bindOnEnter("#search-box", search);

        var grid = window.grid = mini.get("datagrid");
        tools.initGrid(grid);
        grid.setUrl(request.basePath + "email-sender/_query");

        function search() {
            tools.searchGrid("#search-box", grid);
        }

        search();

        $(".add-button").click(function () {
            tools.openWindow("admin/rule-engine/manager/email/save.html", "新建邮件配置", "700", "700", function () {
                grid.reload();
            })
        });

        window.renderConfig = function (e) {
            if (e.value){
                return JSON.stringify(e.value);
            }
        }

        window.renderAction = function (e) {
            var row = e.record;
            var html = [];
            html.push(tools.createActionButton("编辑", "icon-edit", function () {
                tools.openWindow("admin/rule-engine/manager/email/save.html?id=" + row.id, "编辑邮件email：" + row.name, "700", "700", function () {
                    grid.reload();
                });
            }));

            html.push(tools.createActionButton("删除", "icon-remove", function () {
                message.confirm("确定删除配置：" + row.name + "？删除后将无法恢复", function () {
                    var box = message.loading("删除中...");
                    request["delete"]("email-sender/" + row.id, function (response) {
                        box.hide();
                        if (response.status === 200) {
                            message.showTips("删除成功");
                            grid.reload();
                        } else {
                            message.showTips("删除失败:" + response.message, "danger");
                        }
                    });
                });
            }));
            if (tag === 'select'){
                html.push(tools.createActionButton("选择", "icon-ok", function () {
                    tools.closeWindow(row);
                }));
            }
            return html.join("");
        }

    });
});