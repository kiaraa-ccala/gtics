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
