var xAuthTokenHeaderName = 'x-auth-token';

angular.module('reservations', [ 'ngRoute', 'ngSanitize', 'ngResource'  , /*, 'ngCookies' ,*/'mgcrea.ngStrap'  ])/*  ,   'ngAnimate' ,'mgcrea.ngStrap'  */
    .run(function ($rootScope/*, $http, $location, $cookieStore*/) {

    });

function BookingController($scope, $http) {

    $scope.bookings = [];
    
    $scope.booking = {};

    $scope.repeat = function (reservation) {
        reservation.dateAndTime = new Date(reservation.dateAndTime + ( 30 * 1000 ));
        console.log('scheduling for ' + JSON.stringify(reservation));
        $http.post('/bookings/' + reservation.id, reservation);
    };

    $scope.refresh = function () {
        $http.get('/bookings').success(function (reservations) {
            $scope.bookings = reservations;
            console.log('just refreshed\'d $scope.bookings');
        });
    };

    $scope.newBooking = function () {
        $http.post('/bookings', $scope.booking).success(function () {
            $scope.refresh();
        });
    };

    $scope.cancelBooking = function () {
        $scope.booking = { dateAndTime: new Date() };
    };

    $scope.bookingIsValid = function () {
        function isNumber(i) {
            return i != null && i instanceof Number;
        }
        function isDate(d) {
            return d != null && d instanceof Date;
        }
        var ok = $scope.booking != null &&
            $scope.booking.bookingName != '' &&
            isNumber($scope.booking.groupSize) &&
            isDate($scope.booking.dateAndTime);
        console.debug('OK? ' + ok);
        return ok;
    };

    $scope.deleteBooking = function (booking) {
        $http.delete('/bookings/' + booking.id )
            .success (function (){
                $scope.refresh();
            });
    };

    $scope.pastDue = function( r ){
        var now = new Date();
        var due = !!(r.dateAndTime != null && r.dateAndTime < now.getTime()) ;
        console.debug('is ' + r.id + ' due? ' + due);
        return due;
    };

    $scope.alarm = function (reservation) {
        console.log('now: ' + new Date() + '');
        alert(JSON.stringify(reservation));
        console.log(JSON.stringify(reservation));
    };

    var init = function () {

        $scope.cancelBooking();

        var notifications = '/notifications';
        var socket = new SockJS(notifications);
        var client = Stomp.over(socket);
        client.connect(
            {},
            function (frame) {
                console.log('Connected ' + frame);
                var username = frame.headers['user-name'];
                var refreshingHandler = function (message) {
                    $scope.refresh();
                } ;
                client.subscribe("/topic/alarms",refreshingHandler );
                client.subscribe("/topic/reservationEvents",refreshingHandler );
            },
            function (error) {
                console.log("STOMP protocol error " + error);
            }
        );

        $scope.refresh();

    };
    init();

}

