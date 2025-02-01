package umk.jakuburb.mars.Teraformacja.Marsa.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import umk.jakuburb.mars.Teraformacja.Marsa.database.entity.CardSkills;

@Repository
public interface CardSkillsRepository extends JpaRepository<CardSkills, Long> {
}
