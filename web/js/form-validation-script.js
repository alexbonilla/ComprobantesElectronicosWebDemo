var Script = function () {

//    $.validator.setDefaults({
//        submitHandler: function() { alert("submitted!"); }
//    });

    $().ready(function() {
        // validate the comment form when it is submitted
        $("#commentForm").validate();

        // validate signup form on keyup and submit
        $("#signupForm").validate({
            rules: {
                firstname: "required",
                lastname: "required",
                username: {
                    required: true,
                    minlength: 6
                },
                password: {
                    required: true,
                    minlength: 8
                },
                confirm_password: {
                    required: true,
                    minlength: 8,
                    equalTo: "#password"
                },
                email: {
                    required: true,
                    email: true
                },
                topic: {
                    required: "#newsletter:checked",
                    minlength: 2
                },
                agree: "required"
            },
            messages: {
                firstname: "Por favor ingrese sus nombres",
                lastname: "Por favor ingrese sus apellidos",
                username: {
                    required: "Por favor ingrese un nombre de usuario",
                    minlength: "Su usuario debe consistir en al menos 6 letras"
                },
                password: {
                    required: "Por favor ingrese una contraseña",
                    minlength: "Su contraseña debe tener al menos 8 caracteres"
                },
                confirm_password: {
                    required: "Por favor confirme su contraseña",
                    minlength: "Su contraseña debe tener al menos 8 caracteres",
                    equalTo: "Por favor confirme correctamente la contraseña (no coinciden)"
                },
                email: "Por favor ingrese una dirección válida de correo electrónico ",
                agree: "Por favor acepte nuestra política"
            }
        });

        // propose username by combining first- and lastname
        $("#username").focus(function() {
            var firstname = $("#firstname").val();
            var lastname = $("#lastname").val();
            if(firstname && lastname && !this.value) {
                this.value = firstname + "." + lastname;
            }
        });

        //code to hide topic selection, disable for demo
        var newsletter = $("#newsletter");
        // newsletter topics are optional, hide at first
        var inital = newsletter.is(":checked");
        var topics = $("#newsletter_topics")[inital ? "removeClass" : "addClass"]("gray");
        var topicInputs = topics.find("input").attr("disabled", !inital);
        // show when newsletter is checked
        newsletter.click(function() {
            topics[this.checked ? "removeClass" : "addClass"]("gray");
            topicInputs.attr("disabled", !this.checked);
        });
    });


}();