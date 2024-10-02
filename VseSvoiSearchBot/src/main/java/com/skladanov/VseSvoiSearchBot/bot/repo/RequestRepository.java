package com.skladanov.VseSvoiSearchBot.bot.repo;

import com.skladanov.VseSvoiSearchBot.bot.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Long> {
}