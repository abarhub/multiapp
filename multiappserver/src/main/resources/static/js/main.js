"use strict";

function installApp(id) {
    console.log("Coucou", id);
    //if (!!id) {
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/app/install?id=" + id,
        dataType: "text",
        success: function (data, status, jqXHR) {
            console.log("traitement ok : ", status);
        },

        error: function (jqXHR, status) {
            console.log("traitement erreur : ", status, jqXHR);
        }
    }).then(function (data) {
        //$('.greeting-id').append(data.id);
        //$('.greeting-content').append(data.content);
        console.log("traitement ok");
    });
    //}
}

