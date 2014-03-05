var xAuthTokenHeaderName = 'x-auth-token';

angular.module('reservations', ['ngRoute', 'ngResource' , 'ngCookies'   ])
    .run(function ($rootScope, $http, $location, $cookieStore) {

    });


function BookingController($scope, $http) {

    $scope.stompClient = null;
    $scope.bookings = [];

    $scope.repeat = function (reservation) {

        reservation.dateAndTime  =  new Date( reservation.dateAndTime  + ( 30 * 1000 ));
        console.log('scheduling for ' + JSON.stringify( reservation ) );
        $http.post('/bookings/' + reservation.id, reservation) ;
    };
    $scope.refresh = function () {
        $http.get('/bookings').success(function (reservations) {
            $scope.bookings = reservations;
            console.log('just refreshed\'d $scope.bookings');
        });
    };
    $scope.alarm = function (reservation) {

        $('#booking-row-' + reservation.id).delay(100).fadeOut().fadeIn('slow');

        window.alert( JSON.stringify(reservation));
    };

    var init = function () {
        //        var socket = new SockJS('/spring-websocket-portfolio/portfolio');
        var notifications = '/notifications';
        var socket = new SockJS(notifications);
        var client = Stomp.over(socket);


        client.connect({}, function (frame) {
            console.log('Connected ' + frame);
            var username = frame.headers['user-name'];
            client.subscribe("/topic/alarms", function (message) {

                $scope.refresh();

                $scope.alarm(JSON.parse(message.body));
            });
        }, function (error) {
            console.log("STOMP protocol error " + error);
        });

        $scope.refresh();

    };


    init();

}

