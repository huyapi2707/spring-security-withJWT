package com.example.demospringsercurity.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<JwtToken, Long> {
    Optional<JwtToken> findByToken(String token);
    @Query(value = """
      select t from JwtToken t inner join User u\s
      on t.referenceUser.userId = u.userId\s
      where u.userId = :id and (t.expired = false or t.revoked = false)\s
      """)
    List<JwtToken> findAllValidTokenByUser(Long id);
}
