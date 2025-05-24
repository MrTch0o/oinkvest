document.getElementById("contact-form").addEventListener("submit", function (event) {
    event.preventDefault();
    console.log("tentando contato em code.js......")
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
function exportarPDF() {
    console.log("Exportando em PDF.....");
    let marginLeft = 40;
    let marginTop = 50;
    let lineHeight = 14;
    let cellPadding = 6;

    var pdf = new jsPDF('p', 'pt', 'a4');
    const pageHeight = pdf.internal.pageSize.pageHeight;

    if (!pdf) {
        alert("Erro ao carregar jsPDF. Verifique se a biblioteca está incluída corretamente.");
        return;
    }

    const headers = [];
    document.querySelectorAll("#tabelaHistorico thead tr th").forEach(th => {
        headers.push(th.innerText);
    });

    const linhas = [];
    document.querySelectorAll("#tabelaHistorico tbody tr").forEach(row => {
        const rowData = [];
        row.querySelectorAll("td").forEach(td => {
            rowData.push(td.innerText);
        });
        linhas.push(rowData);
    });
    const data = [headers, ...linhas];
    const colWidths = [70, 70, 70, 80, 110, 110];

    function drawText(text, x, y, maxWidth, lineHeight) {
        const splitText = pdf.splitTextToSize(text, maxWidth - 2 * cellPadding);
        splitText.forEach((line, i) => {
            pdf.text(line, x + cellPadding, y + i * lineHeight + lineHeight - 4);
        });
        return splitText.length * lineHeight;
    }

    function drawRow(row, x, y, fill) {
        pdf.setFont("helvetica", "normal");
        pdf.setFontSize(10);
        pdf.setTextColor(0, 0, 0);

        let totalWidth = colWidths.reduce((a, b) => a + b, 0);

        // Primeiro: fundo da linha inteira
        if (fill) {
            pdf.setFillColor(230, 240, 255); // azul claro
            pdf.rect(x, y, totalWidth, row.height + 8, 'F');
        }

        // Agora desenha o texto e a borda
        let currentX = x;
        row.data.forEach((cell, i) => {
            // Desenha texto
            drawText(cell, currentX, y + 4, colWidths[i], lineHeight);

            // Borda da célula (sem preenchimento)
            pdf.rect(currentX, y, colWidths[i], row.height + 8);
            currentX += colWidths[i];
        });
    }

    // Título do relatório
    pdf.setFontSize(18);
    pdf.setTextColor(0, 0, 0);
    pdf.setFont("helvetica", "bold");
    pdf.text("Histórico de Dados", marginLeft, marginTop + 20);

    let y = marginTop;
    let headerHeight = 50;
    y += headerHeight + 4;

    data.forEach((rowData, idx) => {
        let maxHeight = 0;
        rowData.forEach((cell, i) => {
            const lines = pdf.splitTextToSize(cell, colWidths[i] - 2 * cellPadding);
            const height = lines.length * lineHeight;
            if (height > maxHeight) maxHeight = height;
        });

        if (y + maxHeight + 12 > pageHeight - marginTop) {
            pdf.addPage();
            y = marginTop;
            headerHeight = 50;
            y += headerHeight + 4;
        }

        drawRow({ data: rowData, height: maxHeight }, marginLeft, y, idx % 2 == 0);
        y += maxHeight + 8;
    });


    pdf.save("tabela_historico.pdf");
}



function exportarExcel() {
    console.log("Exportando em excel.....")
    /*    
        const table = document.querySelector("table");
        const wb = XLSX.utils.table_to_book(table, { sheet: "Transações" });
        XLSX.writeFile(wb, "transacoes.xlsx");
        */
}