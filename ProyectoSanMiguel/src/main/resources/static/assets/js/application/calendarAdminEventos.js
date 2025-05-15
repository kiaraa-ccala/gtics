(function () {
  const calendaroffcanvas = new bootstrap.Offcanvas('#calendar-add_edit_event');
  const calendarmodal = new bootstrap.Modal('#calendar-modal');
  var calendevent = '';

  var date = new Date();
  var d = date.getDate();
  var m = date.getMonth();
  var y = date.getFullYear();

  var calendar = new FullCalendar.Calendar(document.getElementById('calendar'), {
    locale: 'es',
    firstDay: 1,
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    buttonText: {
      today: 'Hoy',
      month: 'Mes',
      week: 'Semana',
      day: 'Día',
      list: 'Lista'
    },
    themeSystem: 'bootstrap',
    initialDate: new Date(y, m, 16),
    slotDuration: '00:10:00',
    navLinks: true,
    height: 'auto',
    droppable: true,
    selectable: false,
    selectMirror: true,
    editable: false,
    dayMaxEvents: true,
    handleWindowResize: true,
    select: function (info) {
      var sdt = new Date(info.start);
      var edt = new Date(info.end);
      document.getElementById('pc-e-sdate').value = sdt.getFullYear() + '-' + getRound(sdt.getMonth() + 1) + '-' + getRound(sdt.getDate());
      document.getElementById('pc-e-edate').value = edt.getFullYear() + '-' + getRound(edt.getMonth() + 1) + '-' + getRound(edt.getDate());

      document.getElementById('pc-e-title').value = '';
      document.getElementById('pc-e-venue').value = '';
      document.getElementById('pc-e-description').value = '';
      document.getElementById('pc-e-type').value = '';
      document.getElementById('pc-e-btn-text').innerHTML = '<i class="align-text-bottom me-1 ti ti-calendar-plus"></i> Add';
      document.querySelector('#pc_event_add').setAttribute('data-pc-action', 'add');

      calendaroffcanvas.show();
      calendar.unselect();
    },
    eventClick: function (info) {
      calendevent = info.event;
      var clickedevent = info.event;
      var e_title = clickedevent.title === undefined ? '' : clickedevent.title;
      var e_desc = clickedevent.extendedProps.description === undefined ? '' : clickedevent.extendedProps.description;
      var e_date_start = clickedevent.start === null ? '' : dateformat(clickedevent.start);
      var e_date_end = clickedevent.end === null ? '' : " <i class='text-sm'>to</i> " + dateformat(clickedevent.end);
      e_date_end = clickedevent.end === null ? '' : e_date_end;
      var e_venue = clickedevent.extendedProps.description === undefined ? '' : clickedevent.extendedProps.venue;

      document.querySelector('.calendar-modal-title').innerHTML = e_title;
      document.querySelector('.pc-event-title').innerHTML = e_title;
      document.querySelector('.pc-event-description').innerHTML = e_desc;
      document.querySelector('.pc-event-date').innerHTML = e_date_start + e_date_end;
      document.querySelector('.pc-event-venue').innerHTML = e_venue;
      document.querySelector('#pc_event_remove').setAttribute('data-id', clickedevent.id);

      calendarmodal.show();
    },
    events: function(fetchInfo, successCallback, failureCallback) {
      const filterType = document.getElementById('filterType').value;
      const idComplejo = document.getElementById('selectComplejo').value;
      const idCoordinador = document.getElementById('selectCoordinador').value;

      let url = '/admin/horarios?';

      if (filterType === 'complejo') {
        url += `idComplejo=${idComplejo}`;
      } else if (filterType === 'coordinador') {
        url += `idCoordinador=${idCoordinador}`;
      }

      fetch(url)
          .then(response => response.json())
          .then(data => {
            const eventos = data.map(horario => ({
              id: horario.idHorario,
              title: horario.nombreCoordinador,
              start: `${horario.fecha}T${horario.horaIngreso}`,
              end: `${horario.fecha}T${horario.horaSalida}`,
              extendedProps: {
                description: `Horario de ${horario.nombreCoordinador}`,
                venue: horario.nombreComplejo
              },
              className: 'event-primary'
            }));
            successCallback(eventos);
          })
          .catch(err => {
            console.error('Error al cargar eventos:', err);
            failureCallback(err);
          });
    }

  });

  calendar.render();
  document.getElementById('selectComplejo').addEventListener('change', function() {
    calendar.refetchEvents();
  });

  document.getElementById('selectCoordinador').addEventListener('change', function() {
    calendar.refetchEvents();
  });
  document.getElementById('filterType').addEventListener('change', function() {
    calendar.refetchEvents();
  });


  document.addEventListener('DOMContentLoaded', function () {
    var calbtn = document.querySelectorAll('.fc-toolbar-chunk');
    for (var t = 0; t < calbtn.length; t++) {
      var c = calbtn[t];
      c.children[0].classList.remove('btn-group');
      c.children[0].classList.add('d-inline-flex');
    }
  });

  var pc_event_remove = document.querySelector('#pc_event_remove');
  var pc_event_remove = document.querySelector('#pc_event_remove');
  if (pc_event_remove) {
    pc_event_remove.addEventListener('click', function () {
      calendarmodal.hide(); // Oculta temporalmente el modal
      const swalWithBootstrapButtons = Swal.mixin({
        customClass: {
          confirmButton: 'btn btn-light-success',
          cancelButton: 'btn btn-light-danger',
          popup: 'swal2-popup', // Añadimos la clase personalizada para el popup
        },
        buttonsStyling: false,
        // Asegúrate de agregar el z-index para asegurarte de que esté encima
        didOpen: () => {
          // Cambiar el z-index del popup de Swal
          document.querySelector('.swal2-popup').style.zIndex = '9999'; // Establecer un z-index mayor que el modal
        },
        buttonsStyling: false
      });

      // Confirmación antes de eliminar
      swalWithBootstrapButtons
          .fire({
            title: 'Estas seguro?',
            text: '¿Deseas borrar este evento?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Si, eliminar',
            cancelButtonText: 'No, cancelar',
            reverseButtons: true
          })
          .then((result) => {
            if (result.isConfirmed) {
              const eventId = calendevent.id;  // Recupera el ID del evento a eliminar

              // Enviar solicitud DELETE al backend
              fetch(`/admin/agenda/eliminarHorario/${eventId}`, {
                method: 'DELETE',
              })
                  .then(response => response.text())
                  .then(data => {
                    if (data === "Horario eliminado correctamente") {
                      calendevent.remove(); // Elimina el evento de FullCalendar
                      calendarmodal.hide(); // Cierra el modal
                      swalWithBootstrapButtons.fire('Eliminado', 'El horario fue borrado.', 'success');
                    }
                  })
                  .catch(err => {
                    console.error('Error al eliminar el evento:', err);
                    swalWithBootstrapButtons.fire('Error', 'Hubo un error al eliminar este evento.', 'error');
                  });
            } else if (result.dismiss === Swal.DismissReason.cancel) {
              swalWithBootstrapButtons.fire('Cancelado', 'La información de este evento esta segura.', 'error')
              .then(() => {
                // Mostrar modal después de que el usuario haga clic en OK
                calendarmodal.show();
              });
            }
          });
    });
  }


  var pc_event_add = document.querySelector('#pc_event_add');
  if (pc_event_add) {
    pc_event_add.addEventListener('click', function () {
      var day = true;
      var end = null;
      var e_date_start = document.getElementById('pc-e-sdate').value === null ? '' : document.getElementById('pc-e-sdate').value;
      var e_date_end = document.getElementById('pc-e-edate').value === null ? '' : document.getElementById('pc-e-edate').value;
      if (!e_date_end == '') {
        end = new Date(e_date_end);
      }
      calendar.addEvent({
        title: document.getElementById('pc-e-title').value,
        start: new Date(e_date_start),
        end: end,
        allDay: day,
        description: document.getElementById('pc-e-description').value,
        venue: document.getElementById('pc-e-venue').value,
        className: document.getElementById('pc-e-type').value
      });
      if (pc_event_add.getAttribute('data-pc-action') == 'add') {
        Swal.fire({
          customClass: {
            confirmButton: 'btn btn-light-primary'
          },
          buttonsStyling: false,
          icon: 'success',
          title: 'Success',
          text: 'Event added successfully'
        });
      } else {
        calendevent.remove();
        document.getElementById('pc-e-btn-text').innerHTML = '<i class="align-text-bottom me-1 ti ti-calendar-plus"></i> Add';
        document.querySelector('#pc_event_add').setAttribute('data-pc-action', 'add');
        Swal.fire({
          customClass: {
            confirmButton: 'btn btn-light-primary'
          },
          buttonsStyling: false,
          icon: 'success',
          title: 'Success',
          text: 'Event Updated successfully'
        });
      }
      calendaroffcanvas.hide();
    });
  }

  var pc_event_edit = document.querySelector('#pc_event_edit');
  if (pc_event_edit) {
    pc_event_edit.addEventListener('click', function () {
      var e_title = calendevent.title === undefined ? '' : calendevent.title;
      var e_desc = calendevent.extendedProps.description === undefined ? '' : calendevent.extendedProps.description;
      var e_date_start = calendevent.start === null ? '' : dateformat(calendevent.start);
      var e_date_end = calendevent.end === null ? '' : " <i class='text-sm'>to</i> " + dateformat(calendevent.end);
      e_date_end = calendevent.end === null ? '' : e_date_end;
      var e_venue = calendevent.extendedProps.description === undefined ? '' : calendevent.extendedProps.venue;
      var e_type = calendevent.classNames[0] === undefined ? '' : calendevent.classNames[0];

      document.getElementById('pc-e-title').value = e_title;
      document.getElementById('pc-e-venue').value = e_venue;
      document.getElementById('pc-e-description').value = e_desc;
      document.getElementById('pc-e-type').value = e_type;
      var sdt = new Date(e_date_start);
      var edt = new Date(e_date_end);
      document.getElementById('pc-e-sdate').value = sdt.getFullYear() + '-' + getRound(sdt.getMonth() + 1) + '-' + getRound(sdt.getDate());
      document.getElementById('pc-e-edate').value = edt.getFullYear() + '-' + getRound(edt.getMonth() + 1) + '-' + getRound(edt.getDate());
      document.getElementById('pc-e-btn-text').innerHTML = '<i class="align-text-bottom me-1 ti ti-calendar-stats"></i> Update';
      document.querySelector('#pc_event_add').setAttribute('data-pc-action', 'edit');
      calendarmodal.hide();
      calendaroffcanvas.show();
    });
  }
  //  get round value
  function getRound(vale) {
    var tmp = '';
    if (vale < 10) {
      tmp = '0' + vale;
    } else {
      tmp = vale;
    }
    return tmp;
  }

  //  get time
  function getTime(timeValue) {
    timeValue = new Date(timeValue);
    if (timeValue.getHours() != null) {
      var hour = timeValue.getHours();
      var minute = timeValue.getMinutes() ? timeValue.getMinutes() : 0;
      return hour + ':' + minute;
    }
  }

  //  get date
  function dateformat(dt) {
    var mn = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    var d = new Date(dt),
      month = '' + mn[d.getMonth()],
      day = '' + d.getDate(),
      year = d.getFullYear();
    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;
    return [day + ' ' + month, year].join(',');
  }

  //  get full date
  function timeformat(time) {
    var timeFormat = time.split(':');
    var hours = timeFormat[0];
    var minutes = timeFormat[1];
    var newformat = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12;
    hours = hours ? hours : 12;
    minutes = minutes < 10 ? '0' + minutes : minutes;
    return hours + ':' + minutes + ' ' + newformat;
  }
})();
