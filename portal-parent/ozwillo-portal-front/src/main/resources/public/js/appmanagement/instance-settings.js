$(document).ready(function() {

    var model = {
        users: []
    };

    var userTemplate = $("#template");
    userTemplate.remove();

    var userList = $("#user-list");

    function removeUser(id) {
        model.users = _.filter(model.users, function(u) { return u.userid != id; });
        refresh();
    }

    function refresh() {
        userList.html(""); // empty it out

        _.each(model.users, function(user) {
            var markup = userTemplate.clone();
            markup.find(".user-name").html(user.fullname);
            var link = markup.find("a");
            link.attr("data-userid", user.userid);
            link.click(function(event) {
                event.preventDefault();
                removeUser($(this).attr("data-userid"));
            });

            userList.append(markup);

            $(".list-group-item").hover(function() {
                    $(this).find(".show-on-hover").show();
                },
                function() {
                    $(this).find(".show-on-hover").hide();
                }
            );


        });
    }


    var source = $(".settings-container").attr("data-source");
    var origin = $(".settings-container").attr("data-origin");

    $.get(source + "/app_users", function (result) {
        model.users = result;
        refresh();
    }, "json");



    $.get(source + "/org_users", function(result) {
        var selectedUser = null;

        // init the typeahead
        var organizationUsers = new Bloodhound({
            name: "allUsers",
            datumTokenizer: function(d) {
                return Bloodhound.tokenizers.whitespace(d.fullname);
            },
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            limit: 10,
            local: result

        });

        organizationUsers.initialize();
        // init the typeahead
        $("#user").typeahead({
            hint:true,
            highlight:true,
            minLength:1
        },{
            name: "allUsers",
            displayKey: "fullname",
            source: organizationUsers.ttAdapter()
        }).on("typeahead:selected", function(event, selected) {
            selectedUser = selected;
        }).on("typeahead:autocompleted", function(event, autocompleted) {
            selectedUser = autocompleted;
        }).keypress(function(key) {
            if ((key.keyCode || key.which) == 13) {
                $("#add-user").click();
            }
        });

        $("#add-user").click(function(e) {
            e.preventDefault();
            if (selectedUser == null) {
                return;
            }


            var matching = _.filter(model.users, function (u) {
                return u.userid == selectedUser.userid;
            }).length;
            if (matching != 0) {
                return;
            }

            model.users.push(selectedUser);
            selectedUser = null;

            $("#user").val("");

            refresh();
        });


    }, "json");


    $("#save").click(function(event) {
        event.preventDefault();

        $.ajax({
            url: source + "/save",
            type: "POST",
            data: JSON.stringify(model.users),
            contentType: "application/json; charset=utf-8",
            dataType: "html",
            success: function(x) {
                window.location=origin;
            }
        });
    });

    var redirectToOrigin = function () {
        window.location = origin;
    };

    $(".settings-background").click(redirectToOrigin);
    $("#close").click(redirectToOrigin);


    $(".settings-container").click(function(event) {
        event.stopPropagation();
    });

});