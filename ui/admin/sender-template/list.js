importResource("/admin/css/common.css");

importMiniui(function () {
    mini.parse();
    require(["request", "miniui-tools"], function (request, tools) {

        var typeData = [
            {id:"", text:""},
            {id:"sms", text:"短信"},
            {id:"email", text:"邮件"}
        ];

        var type = mini.getbyName("type");
        type.setData(typeData);

        var grid = window.grid = mini.get("data-grid");
        tools.initGrid(grid);

        grid.setUrl(request.basePath + "sender-template/_query/no-paging");

        grid.setDataField("result");
        function search() {
            tools.searchGrid("#search-box", grid);
        }

        $(".search-button").click(search);
        tools.bindOnEnter("#search-box", search);
        $(".add-button").click(function () {
            tools.openWindow("admin/sender-template/save.html", "添加模板", "550", "430", function (e) {
                grid.reload();
            })
        });
        search();
        window.renderStatus = function (e) {
            return e.value === 1 ? "是" : "否";
        }

        function edit(id) {
            tools.openWindow("admin/sender-template/save.html?id=" + id, "编辑模板", "550", "430", function (e) {
                grid.reload();
            })
        }

        window.renderAction = function (e) {
            var row = e.record;

            var html = [
                tools.createActionButton("编辑", "icon-edit", function () {
                    edit(row.id);
                })
            ];

            html.push(
                tools.createActionButton("删除", "icon-remove", function () {
                    require(["message"], function (message) {
                        message.confirm("确定删除该模板?", function () {
                            var loading = message.loading("删除中...");
                            request["delete"]("sender-template/" + row.id, {}, function (res) {
                                loading.close();
                                if (res.status === 200) {
                                    grid.reload();
                                    message.showTips("删除成功");
                                } else {
                                    message.showTips("删除失败:" + res.message);
                                }
                            })
                        });
                    });
                })
            );
            return html.join("");
        }

    });
});
