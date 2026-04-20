package com.grid07.grid07;

import com.grid07.grid07.entity.Bot;
import com.grid07.grid07.entity.User;
import com.grid07.grid07.repository.BotRepository;
import com.grid07.grid07.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BotRepository botRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User u1 = new User();
            u1.setUsername("alice");
            u1.setPremium(true);
            userRepository.save(u1);

            User u2 = new User();
            u2.setUsername("bob");
            u2.setPremium(false);
            userRepository.save(u2);

            System.out.println("Seeded 2 test users");
        }

        if (botRepository.count() == 0) {
            Bot b1 = new Bot();
            b1.setName("BotAlpha");
            b1.setPersonaDescription("A helpful assistant bot");
            botRepository.save(b1);

            Bot b2 = new Bot();
            b2.setName("BotBeta");
            b2.setPersonaDescription("A news summarizer bot");
            botRepository.save(b2);

            System.out.println("Seeded 2 test bots");
        }
    }
}