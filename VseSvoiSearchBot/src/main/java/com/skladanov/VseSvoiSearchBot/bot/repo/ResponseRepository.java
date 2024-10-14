package com.skladanov.VseSvoiSearchBot.bot.repo;

import com.skladanov.VseSvoiSearchBot.bot.model.Response;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponseRepository extends JpaRepository<Response, Long> {
}