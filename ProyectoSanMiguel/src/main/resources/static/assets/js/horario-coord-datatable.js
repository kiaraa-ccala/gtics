document.addEventListener('DOMContentLoaded', function() {
    if (typeof jQuery != 'undefined' && typeof $.fn.DataTable != 'undefined') {
        $('#pc-dt-simple').DataTable({
            dom: '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>><"table-responsive"t><"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
            language: {
                "decimal": "",
                "emptyTable": "No hay datos disponibles",
                "info": "Mostrando _START_ a _END_ de _TOTAL_ registros",
                "infoEmpty": "Mostrando 0 a 0 de 0 registros",
                "infoFiltered": "(filtrado de _MAX_ registros totales)",
                "infoPostFix": "",
                "thousands": ",",
                "lengthMenu": "Mostrar _MENU_ registros",
                "loadingRecords": "Cargando...",
                "processing": "Procesando...",
                "search": "Buscar:",
                "zeroRecords": "No se encontraron coincidencias",
                "paginate": {
                    "first": "Primero",
                    "last": "Último",
                    "next": "Siguiente",
                    "previous": "Anterior"
                }
            },
            scrollX: true,  // Mantener scroll horizontal
            scrollY: false, // Deshabilitar scroll vertical
            fixedHeader: false, // Desactivar fixedHeader
            scrollCollapse: false,
            lengthMenu: [[5, 10, 25, 50, -1], [5, 10, 25, 50, "Todos"]],
            pageLength: 10,
            initComplete: function() {
                // Ajustar columnas después de inicialización
                this.api().columns.adjust();

                // Redimensionar al cambiar tamaño de ventana
                $(window).on('resize', function() {
                    $('#pc-dt-simple').DataTable().columns.adjust();
                });
            }
        });
    }
});