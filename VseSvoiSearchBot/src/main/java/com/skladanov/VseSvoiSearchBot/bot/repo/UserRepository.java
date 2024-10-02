package com.skladanov.VseSvoiSearchBot.bot.repo;

import com.skladanov.VseSvoiSearchBot.bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
}