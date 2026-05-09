console.log("JS conectado");
document.addEventListener("DOMContentLoaded", () => {

    const canvas = document.getElementById("citasEstadoChart");

    if(canvas){

        new Chart(canvas, {

            type: "doughnut",

            data: {

                labels: [
                    "Confirmadas",
                    "Pendientes",
                    "Canceladas"
                ],

                datasets: [{

                    data: [45, 20, 10],

                    backgroundColor: [
                        "#20c997",
                        "#ffc107",
                        "#dc3545"
                    ],

                    borderWidth: 0

                }]
            },

            options: {

                responsive: true,

                plugins: {

                    legend: {

                        position: "bottom"

                    }

                }

            }

        });

    }

});

document.addEventListener("DOMContentLoaded", function () {

    const buscador = document.getElementById("buscarPaciente");

    if(buscador){

        buscador.addEventListener("keyup", function () {

            const texto = buscador.value.toLowerCase();

            const filas = document.querySelectorAll("#tablaPacientes tr");

            filas.forEach(fila => {

                const contenido = fila.textContent.toLowerCase();

                if (contenido.includes(texto)) {

                    fila.style.display = "";

                } else {

                    fila.style.display = "none";

                }

            });

        });

    }

});