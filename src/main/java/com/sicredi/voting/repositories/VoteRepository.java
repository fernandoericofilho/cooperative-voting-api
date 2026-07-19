package com.sicredi.voting.repositories;

import com.sicredi.voting.dtos.VoteCount;
import com.sicredi.voting.models.Vote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("select v.vote as option, count(v) as total from Vote v where v.agendaId = :agendaId group by v.vote")
    List<VoteCount> countByAgenda(@Param("agendaId") Long agendaId);
}
