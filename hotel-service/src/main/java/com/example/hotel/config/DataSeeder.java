package com.example.hotel.config;

import com.example.hotel.entity.*;
import com.example.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            if (hotelRepository.count() > 0) return;
            Hotel h1 = hotelRepository.save(Hotel.builder().name("Grand Hotel").address("Main St 1").build());
            Hotel h2 = hotelRepository.save(Hotel.builder().name("Sea View").address("Beach Ave 5").build());
            roomRepository.saveAll(List.of(
                    Room.builder().hotel(h1).number("101").available(true).timesBooked(5).build(),
                    Room.builder().hotel(h1).number("102").available(true).timesBooked(2).build(),
                    Room.builder().hotel(h1).number("103").available(true).timesBooked(8).build(),
                    Room.builder().hotel(h2).number("201").available(true).timesBooked(0).build(),
                    Room.builder().hotel(h2).number("202").available(false).timesBooked(3).build()
            ));
        };
    }
}
