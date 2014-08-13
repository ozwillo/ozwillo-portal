
function init_users() {
    var window = $("#modal-window");

    var tbody = $("#user_list");
    var template = $("#template");
    template.remove();

    var model = {};
    $.get(window.attr("data-source"), function(result) {

        model["users"] = result;
        _.each(result, function(u) {
            var item = template.clone();
            item.children().first().text(u.fullname);


            var button = item.find("button");
            button.attr("data-userid", u.userid);
            button.click(function(event) {
                event.preventDefault();
                var b = $(this);
                removeUser(b.attr("data-userid"));
                b.parentsUntil($("tbody")).remove();
            });


            tbody.append(item);

        });
    }, "json");

    function removeUser(user_id) {
        model["users"] = _.filter(model.users, function (u) {
            return u.userid != user_id;
        });

    }


    $("#saveUsers").click(function(e){

        $.ajax({
            url: window.attr("data-source"),
            type: "POST",
            data: JSON.stringify(model.users),
            contentType: "application/json; charset=utf-8",
            dataType: "html",
            success: function(x) {
                window.modal("hide");
            }
        });

    });
}