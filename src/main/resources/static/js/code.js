document.getElementById("contact-form").addEventListener("submit", function (event) {
    event.preventDefault();
    console.log("tentando contato......")
    const nameSender = this.name.value;
    const email = this.email.value;
    const subject = this.subject.value;
    const message = this.message.value;

    const serviceId = "service_9ogddug";
    const templateId = "template_i2fgr3r";
    const publicKey = "frqHTWoDUbcDyT2n6";

    const templateParams = {
        from_name: nameSender,
        from_email: email,
        to_name: "OinkVest",
        subject: subject,
        message: message,
    };

    // Enviar o e-mail usando o EmailJS
    emailjs
        .send(serviceId, templateId, templateParams, publicKey)
        .then((response) => {
            console.log("Email enviado com sucesso!", response);
            alert("SUCESSO!")
        })
        .catch((error) => {
            console.error("Erro ao enviar o email:", error);
            alert("ERRO")
        });
});
