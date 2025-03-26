$(document).ready(function() {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $('.complete-btn').on('click', function() {
        var reservationId = $(this).data('id');

        $.ajax({
            url: '/reservations/update-status/' + reservationId,
            type: 'POST',
            contentType: 'application/json',
            beforeSend: function(xhr) {
                xhr.setRequestHeader(header, token);
            },
            data: JSON.stringify({
                status: 'COMPLETED'
            }),
            success: function(response) {
                location.reload();
            },
            error: function(xhr) {
                console.log(xhr);
                alert('An error occurred while changing status: ' +
                    (xhr.responseJSON ? xhr.responseJSON.error : xhr.statusText));
            }
        });
    });
});
