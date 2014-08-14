
function init_users() {

    // init the model and data table

    var window = $("#modal-window");

    var tbody = $("#user_list");
    var template = $("#template");
    template.remove();

    var model = {};

    var addUser = function(u) {
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

    }

    $.get(window.attr("data-source"), function(result) {

        model["users"] = result;
        _.each(result, addUser);
    }, "json");

    function removeUser(user_id) {
        model["users"] = _.filter(model.users, function (u) {
            return u.userid != user_id;
        });

    }


    // the typeahead

    var allUsers = [];
    var selectedUser = null;
    $.get(window.attr("data-source") + "/all", function(result) {
        _.each(result, function(e){
            allUsers.push({userid: e.userid, fullname: e.fullname});
        });
        var organizationUsers = new Bloodhound({
            name: "allUsers",
            datumTokenizer: function(d) {
                return Bloodhound.tokenizers.whitespace(d.fullname);
            },
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            limit: 10,
            local: allUsers
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
    }, "json");

    // add user button
    $("#add-user").click(function (event) {
        event.preventDefault();
        if (selectedUser != null) {
            console.log("selected " + selectedUser.userid + " - " + selectedUser.fullname);

            if ( _.find(model.users, function(u) { return u.userid == selectedUser.userid; }) == undefined) {
                model.users.push(selectedUser);
                addUser(selectedUser);
                selectedUser = null;
                $("#user").val("");
            }
        }
    });


    // overall save

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