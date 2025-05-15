const mapDias = {
    "Monday": "Lunes",
    "Tuesday": "Martes",
    "Wednesday": "Miercoles",
    "Thursday": "Jueves",
    "Friday": "Viernes",
    "Saturday": "Sabado",
    "Sunday": "Domingo"
};

function limpiarListasTurnos() {
    Object.values(mapDias).forEach(dia => {
        const lista = document.getElementById(`listaTurnos${dia}`);
        if (lista) lista.innerHTML = "";
    });
}

function mostrarTurnosEnFormulario(turnos) {
    limpiarListasTurnos();

    turnos.forEach(turno => {
        const diaEsp = mapDias[turno.diaSemana];
        const lista = document.getElementById(`listaTurnos${diaEsp}`);

        if (lista) {
            const li = document.createElement("li");
            li.className = "list-group-item d-flex justify-content-between align-items-center px-2 py-1";
            li.dataset.id = turno.id;
            li.innerHTML = `
                <span>${turno.horaInicio.slice(0,5)} - ${turno.horaFin.slice(0,5)} | ${turno.nombreComplejo}</span>
                <button class="btn btn-sm btn-outline-danger" onclick="eliminarTurno(${turno.id}, this)">×</button>
            `;
            lista.appendChild(li);
        }
    });
}


document.addEventListener("DOMContentLoaded", function () {
    const selectCoordinador = document.getElementById("coordinador");
    const inputSemana = document.getElementById("semana");

    function cargarTurnos() {
        const idCoord = selectCoordinador.value;
        const semana = inputSemana.value;

        if (!idCoord || !semana) return;

        fetch(`/admin/test/horarios/buscar?coordinadorId=${idCoord}&semana=${semana}`)
            .then(response => response.json())
            .then(data => {
                mostrarTurnosEnFormulario(data); // Llama tu función
            })
            .catch(err => console.error("Error al obtener turnos:", err));
    }

    selectCoordinador.addEventListener("change", cargarTurnos);
    inputSemana.addEventListener("change", cargarTurnos);
});
