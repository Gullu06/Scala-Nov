cd BookingService
docker build -t hotel-booking-service:latest .

cd ..
cd RestaurantService
docker build -t hotel-restaurant-service:latest .

cd ..
cd RoomService
docker build -t hotel-room-service:latest .

cd ..
cd WifiService
docker build -t hotel-wifi-service:latest .

cd ..
docker network create kafka-network