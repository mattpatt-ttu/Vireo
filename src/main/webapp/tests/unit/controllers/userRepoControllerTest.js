describe('controller: UserRepoController', function () {

    var controller, scope;

    beforeEach(function() {
        module('core');
        module('vireo');
        module('mock.modalService');
        module('mock.restApi');
        module('mock.storageService');
        module('mock.user');
        module('mock.userRepo');
        module('mock.userService');

        inject(function ($controller, $location, $route, $q, $rootScope, $timeout, $window, _ModalService_, _RestApi_, _StorageService_, _User_, _UserRepo_, _UserService_) {
            installPromiseMatchers();
            scope = $rootScope.$new();

            controller = $controller('UserRepoController', {
                $location: $location,
                $q: $q,
                $route: $route,
                $scope: scope,
                $timeout: $timeout,
                $window: $window,
                ModalService: _ModalService_,
                RestApi: _RestApi_,
                StorageService: _StorageService_,
                User: _User_,
                UserRepo: _UserRepo_,
                UserService: _UserService_
            });

            // ensure that the isReady() is called.
            scope.$digest();
        });
    });

    describe('Is the controller defined', function () {
        it('should be defined', function () {
            expect(controller).toBeDefined();
        });
    });

});
